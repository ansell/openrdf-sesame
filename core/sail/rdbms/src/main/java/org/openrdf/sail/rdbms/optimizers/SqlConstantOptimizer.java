/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.optimizers;

import static org.openrdf.sail.rdbms.algebra.base.SqlExprSupport.and;
import static org.openrdf.sail.rdbms.algebra.base.SqlExprSupport.isNull;
import static org.openrdf.sail.rdbms.algebra.base.SqlExprSupport.not;
import static org.openrdf.sail.rdbms.algebra.base.SqlExprSupport.or;
import static org.openrdf.sail.rdbms.algebra.base.SqlExprSupport.str;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.algebra.QueryModelNode;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.evaluation.QueryOptimizer;
import org.openrdf.sail.rdbms.algebra.FalseValue;
import org.openrdf.sail.rdbms.algebra.SelectQuery;
import org.openrdf.sail.rdbms.algebra.SqlAnd;
import org.openrdf.sail.rdbms.algebra.SqlCase;
import org.openrdf.sail.rdbms.algebra.SqlCompare;
import org.openrdf.sail.rdbms.algebra.SqlConcat;
import org.openrdf.sail.rdbms.algebra.SqlEq;
import org.openrdf.sail.rdbms.algebra.SqlIsNull;
import org.openrdf.sail.rdbms.algebra.SqlLowerCase;
import org.openrdf.sail.rdbms.algebra.SqlNot;
import org.openrdf.sail.rdbms.algebra.SqlNull;
import org.openrdf.sail.rdbms.algebra.SqlOr;
import org.openrdf.sail.rdbms.algebra.StringValue;
import org.openrdf.sail.rdbms.algebra.TrueValue;
import org.openrdf.sail.rdbms.algebra.SqlCase.Entry;
import org.openrdf.sail.rdbms.algebra.base.BinarySqlOperator;
import org.openrdf.sail.rdbms.algebra.base.FromItem;
import org.openrdf.sail.rdbms.algebra.base.RdbmsQueryModelVisitorBase;
import org.openrdf.sail.rdbms.algebra.base.SqlConstant;
import org.openrdf.sail.rdbms.algebra.base.SqlExpr;
import org.openrdf.sail.rdbms.algebra.base.UnarySqlOperator;

/**
 * Optimises SQL constants, include operations with static values and null
 * operations.
 * 
 * @author James Leigh
 * 
 */
public class SqlConstantOptimizer extends RdbmsQueryModelVisitorBase<RuntimeException> implements
		QueryOptimizer
{

	@Override
	public void meet(SelectQuery node)
		throws RuntimeException
	{
		super.meet(node);
		List<SqlExpr> filters = node.getFilters();
		for (int i = filters.size() - 1; i >= 0; i--) {
			if (filters.get(i) instanceof TrueValue) {
				node.removeFilter(filters.get(i));
			}
		}
	}

	@Override
	public void meet(SqlAnd node)
		throws RuntimeException
	{
		super.meet(node);
		for (SqlExpr arg : node.getArgs()) {
			if (arg instanceof FalseValue) {
				replace(node, new FalseValue());
				return;
			} else if (arg instanceof SqlNull) {
				replace(node, new SqlNull());
				return;
			} else if (arg instanceof TrueValue) {
				node.removeChildNode(arg);
			} else if (arg instanceof SqlNot) {
				SqlNot not = (SqlNot)arg;
				List<SqlExpr> args = Arrays.asList(node.getArgs());
				if (args.contains(not.getArg())) {
					replace(node, new FalseValue());
					return;
				}
			}
		}
		if (node.getNumberOfArguments() == 0) {
			replace(node, new TrueValue());
		}
	}

	@Override
	public void meet(SqlCase node)
		throws RuntimeException
	{
		super.meet(node);
		List<Entry> entries = node.getEntries();
		for (SqlCase.Entry e : entries) {
			if (e.getCondition() instanceof SqlNull) {
				node.removeEntry(e);
			}
			else if (e.getCondition() instanceof FalseValue) {
				node.removeEntry(e);
			}
			else if (e.getCondition() instanceof TrueValue) {
				node.truncateEntries(e);
				break;
			}
		}
		entries = node.getEntries();
		if (entries.isEmpty()) {
			replace(node, new SqlNull());
		}
		else if (entries.size() == 1) {
			Entry entry = entries.get(0);
			if (entry.getCondition() instanceof TrueValue) {
				replace(node, entry.getResult().clone());
			}
			else if (entry.getCondition() instanceof FalseValue) {
				replace(node, new SqlNull());
			}
			else if (entry.getCondition() instanceof SqlNot) {
				SqlNot not = (SqlNot)entry.getCondition();
				if (not.getArg() instanceof SqlIsNull) {
					SqlIsNull is = (SqlIsNull)not.getArg();
					if (is.getArg().equals(entry.getResult())) {
						replace(node, entry.getResult().clone());
					}
				}
			}
		}
	}

	@Override
	public void meet(SqlCompare node)
		throws RuntimeException
	{
		super.meet(node);
		SqlExpr left = node.getLeftArg();
		SqlExpr right = node.getRightArg();
		if (left instanceof SqlNull || right instanceof SqlNull) {
			replace(node, new SqlNull());
		}
	}

	@Override
	public void meet(SqlConcat node)
		throws RuntimeException
	{
		super.meet(node);
		SqlExpr left = node.getLeftArg();
		SqlExpr right = node.getRightArg();
		if (left instanceof StringValue && right instanceof StringValue) {
			StringValue l = (StringValue)left;
			StringValue r = (StringValue)right;
			replace(node, new StringValue(l.getValue() + r.getValue()));
		}
	}

	@Override
	public void meet(SqlEq node)
		throws RuntimeException
	{
		super.meet(node);
		SqlExpr left = node.getLeftArg();
		SqlExpr right = node.getRightArg();
		if (left instanceof SqlNull || right instanceof SqlNull) {
			replace(node, new SqlNull());
		}
		else if (left instanceof SqlConstant<?> && right instanceof SqlConstant<?>) {
			SqlConstant<?> l = (SqlConstant<?>)left;
			SqlConstant<?> r = (SqlConstant<?>)right;
			if (l.getValue().equals(r.getValue())) {
				replace(node, new TrueValue());
			}
			else {
				replace(node, new FalseValue());
			}
		}
	}

	@Override
	public void meet(SqlIsNull node)
		throws RuntimeException
	{
		super.meet(node);
		SqlExpr arg = node.getArg();
		if (arg instanceof SqlNull) {
			replace(node, new TrueValue());
		}
		else if (arg instanceof SqlConstant<?>) {
			replace(node, new FalseValue());
		}
		else if (arg instanceof SqlCase) {
			SqlExpr rep = null;
			SqlExpr prev = null;
			SqlCase scase = (SqlCase)arg;
			for (Entry entry : scase.getEntries()) {
				SqlExpr condition = entry.getCondition();
				if (rep == null) {
					rep = and(condition.clone(), isNull(entry.getResult().clone()));
					prev = not(condition.clone());
				}
				else {
					rep = or(rep, and(and(prev.clone(), condition.clone()), isNull(entry.getResult().clone())));
					prev = and(prev, not(condition.clone()));
				}
			}
			replace(node, or(rep, prev.clone()));
		}
	}

	@Override
	public void meet(SqlLowerCase node)
		throws RuntimeException
	{
		super.meet(node);
		if (node.getArg() instanceof SqlNull) {
			replace(node, new SqlNull());
		}
		else if (node.getArg() instanceof SqlConstant) {
			SqlConstant arg = (SqlConstant)node.getArg();
			String lower = arg.getValue().toString().toLowerCase(Locale.US);
			replace(node, str(lower));
		}
	}

	@Override
	public void meet(SqlNot node)
		throws RuntimeException
	{
		super.meet(node);
		SqlExpr arg = node.getArg();
		if (arg instanceof TrueValue) {
			replace(node, new FalseValue());
		}
		else if (arg instanceof FalseValue) {
			replace(node, new TrueValue());
		}
		else if (arg instanceof SqlNull) {
			replace(node, new SqlNull());
		}
		else if (arg instanceof SqlNot) {
			SqlNot not = (SqlNot)arg;
			replace(node, not.getArg().clone());
		}
		else if (arg instanceof SqlOr) {
			SqlOr or = (SqlOr)arg;
			SqlExpr[] nots = new SqlExpr[or.getNumberOfArguments()];
			for (int i = 0, n = or.getNumberOfArguments(); i < n; i++) {
				nots[i] = not(or.getArg(i).clone());
			}
			replace(node, and(nots));
		}
	}

	@Override
	public void meet(SqlOr node)
		throws RuntimeException
	{
		super.meet(node);
		boolean top = andAllTheWay(node);
		SqlExpr sqlNull = null;
		for (SqlExpr arg : node.getArgs()) {
			if (arg instanceof TrueValue) {
				replace(node, new TrueValue());
				return;
			} else if (arg instanceof FalseValue) {
				node.removeChildNode(arg);
			} else if (top && arg instanceof SqlNull) {
				node.removeChildNode(arg);
			} else if (sqlNull != null && arg instanceof SqlNull) {
				node.removeChildNode(arg);
			} else if (arg instanceof SqlNull) {
				sqlNull = (SqlNull) arg;
			}
		}
		if (node.getNumberOfArguments() == 0) {
			replace(node, new FalseValue());
		} else if (node.getNumberOfArguments() == 1) {
			replace(node, node.getArg(0));
		} else if (sqlNull != null) {
			for (SqlExpr arg : node.getArgs()) {
				if (arg instanceof SqlOr) {
					SqlOr nestedOr = (SqlOr) arg;
					for (SqlExpr nestedArg : nestedOr.getArgs()) {
						if (nestedArg instanceof SqlNull) {
							nestedOr.removeChildNode(nestedArg);
						}
					}
					if (nestedOr.getNumberOfArguments() == 0) {
						replace(nestedOr, new SqlNull());
					} else if (nestedOr.getNumberOfArguments() == 1) {
						replace(nestedOr, nestedOr.getArg(0));
					}
				} else if (arg instanceof SqlAnd) {
					// value IS NOT NULL AND value = ? OR NULL
					// -> value = ?
					SqlAnd and = (SqlAnd)arg;
					// search for the value IS NOT NULL expression
					for (SqlExpr isNotNull : and.getArgs()) {
						SqlExpr variable = arg(arg(isNotNull, SqlNot.class), SqlIsNull.class);
						if (variable == null)
							continue;
						// search for the value = ? expression
						for (SqlExpr eq : and.getArgs()) {
							SqlExpr constant = other(eq, variable, SqlEq.class);
							if (constant == null)
								continue;
							if (constant instanceof SqlConstant) {
								node.removeChildNode(sqlNull);
								and.removeChildNode(isNotNull);
								if (node.getNumberOfArguments() == 1) {
									replace(node, node.getArg(0));
								}
								if (and.getNumberOfArguments() == 1) {
									replace(and, and.getArg(0));
								}
								return;
							}
						}
					}
				}
			}
		}
	}

	public void optimize(SqlExpr sqlExpr) {
		sqlExpr.visit(this);
	}

	public void optimize(TupleExpr tupleExpr, Dataset dataset, BindingSet bindings) {
		tupleExpr.visit(this);
	}

	private boolean andAllTheWay(QueryModelNode node) {
		if (node.getParentNode() instanceof SelectQuery)
			return true;
		if (node.getParentNode() instanceof FromItem)
			return true;
		if (node.getParentNode() instanceof SqlAnd)
			return andAllTheWay(node.getParentNode());
		return false;
	}

	private SqlExpr arg(SqlExpr node, Class<? extends UnarySqlOperator> type) {
		if (type.isInstance(node))
			return type.cast(node).getArg();
		return null;
	}

	private SqlExpr other(SqlExpr node, SqlExpr compare, Class<? extends BinarySqlOperator> type) {
		if (type.isInstance(node)) {
			BinarySqlOperator cast = type.cast(node);
			SqlExpr left = cast.getLeftArg();
			SqlExpr right = cast.getRightArg();
			if (left.equals(compare))
				return right;
			if (right.equals(compare))
				return left;
		}
		return null;
	}

	private void replace(SqlExpr before, SqlExpr after) {
		before.replaceWith(after);
		after.visit(this);
	}
}
