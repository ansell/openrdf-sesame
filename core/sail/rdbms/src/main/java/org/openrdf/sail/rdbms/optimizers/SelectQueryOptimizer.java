/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.optimizers;

import static org.openrdf.sail.rdbms.algebra.ColumnVar.createCtx;
import static org.openrdf.sail.rdbms.algebra.ColumnVar.createObj;
import static org.openrdf.sail.rdbms.algebra.ColumnVar.createPred;
import static org.openrdf.sail.rdbms.algebra.ColumnVar.createSubj;
import static org.openrdf.sail.rdbms.algebra.base.SqlExprSupport.coalesce;
import static org.openrdf.sail.rdbms.algebra.base.SqlExprSupport.eq;
import static org.openrdf.sail.rdbms.algebra.base.SqlExprSupport.isNull;
import static org.openrdf.sail.rdbms.algebra.base.SqlExprSupport.or;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.algebra.And;
import org.openrdf.query.algebra.Distinct;
import org.openrdf.query.algebra.Filter;
import org.openrdf.query.algebra.Join;
import org.openrdf.query.algebra.LeftJoin;
import org.openrdf.query.algebra.Order;
import org.openrdf.query.algebra.OrderElem;
import org.openrdf.query.algebra.Projection;
import org.openrdf.query.algebra.ProjectionElem;
import org.openrdf.query.algebra.ProjectionElemList;
import org.openrdf.query.algebra.QueryModel;
import org.openrdf.query.algebra.Slice;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.Union;
import org.openrdf.query.algebra.ValueExpr;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.algebra.StatementPattern.Scope;
import org.openrdf.query.algebra.evaluation.QueryOptimizer;
import org.openrdf.query.impl.DatasetImpl;
import org.openrdf.sail.rdbms.RdbmsValueFactory;
import org.openrdf.sail.rdbms.algebra.BNodeColumn;
import org.openrdf.sail.rdbms.algebra.ColumnVar;
import org.openrdf.sail.rdbms.algebra.DatatypeColumn;
import org.openrdf.sail.rdbms.algebra.IdColumn;
import org.openrdf.sail.rdbms.algebra.JoinItem;
import org.openrdf.sail.rdbms.algebra.LabelColumn;
import org.openrdf.sail.rdbms.algebra.LanguageColumn;
import org.openrdf.sail.rdbms.algebra.LongLabelColumn;
import org.openrdf.sail.rdbms.algebra.LongURIColumn;
import org.openrdf.sail.rdbms.algebra.NumberValue;
import org.openrdf.sail.rdbms.algebra.RefIdColumn;
import org.openrdf.sail.rdbms.algebra.SelectProjection;
import org.openrdf.sail.rdbms.algebra.SelectQuery;
import org.openrdf.sail.rdbms.algebra.SqlEq;
import org.openrdf.sail.rdbms.algebra.SqlOr;
import org.openrdf.sail.rdbms.algebra.URIColumn;
import org.openrdf.sail.rdbms.algebra.UnionItem;
import org.openrdf.sail.rdbms.algebra.base.RdbmsQueryModelVisitorBase;
import org.openrdf.sail.rdbms.algebra.base.SqlExpr;
import org.openrdf.sail.rdbms.algebra.factories.SqlExprFactory;
import org.openrdf.sail.rdbms.exceptions.RdbmsException;
import org.openrdf.sail.rdbms.exceptions.UnsupportedRdbmsOperatorException;
import org.openrdf.sail.rdbms.managers.TransTableManager;
import org.openrdf.sail.rdbms.model.RdbmsResource;

/**
 * Rewrites the core algebra model with a relation optimised model, using SQL.
 * 
 * @author James Leigh
 * 
 */
public class SelectQueryOptimizer extends RdbmsQueryModelVisitorBase<RdbmsException> implements
		QueryOptimizer
{

	private static final String ALIAS = "t";

	private SqlExprFactory sql;

	private int aliasCount = 0;

	private BindingSet bindings;

	private Dataset dataset;

	private RdbmsValueFactory vf;

	private TransTableManager tables;

	public void setSqlExprFactory(SqlExprFactory sql) {
		this.sql = sql;
	}

	public void setValueFactory(RdbmsValueFactory vf) {
		this.vf = vf;
	}

	public void setTransTableManager(TransTableManager statements) {
		this.tables = statements;
	}

	public void optimize(QueryModel query, BindingSet bindings)
		throws RdbmsException
	{
		if (!query.getDefaultGraphs().isEmpty() || !query.getNamedGraphs().isEmpty()) {
			this.dataset = new DatasetImpl(query.getDefaultGraphs(), query.getNamedGraphs());
		}
		this.bindings = bindings;
		query.visit(this);
	}

	@Override
	public void meet(Distinct node)
		throws RdbmsException
	{
		super.meet(node);
		if (node.getArg() instanceof SelectQuery) {
			SelectQuery query = (SelectQuery)node.getArg();
			query.setDistinct(true);
			node.replaceWith(query);
		}
	}

	@Override
	public void meet(Union node)
		throws RdbmsException
	{
		super.meet(node);
		for (TupleExpr arg : node.getArgs()) {
			if (!(arg instanceof SelectQuery))
				return;
			SelectQuery sq = (SelectQuery) arg;
			if (sq.isComplex())
				return;
		}
		assert node.getNumberOfArguments() > 0;
		UnionItem union = new UnionItem("u" + aliasCount++);
		SelectQuery query = new SelectQuery();
		query.setFrom(union);
		for (TupleExpr arg : node.getArgs()) {
			SelectQuery sub = (SelectQuery) arg.clone();
			union.addUnion(sub.getFrom().clone());
			mergeSelectClause(query, sub);
		}
		addProjectionsFromUnion(query, union);
		node.replaceWith(query);
	}

	/**
	 * This happens when both sides of the union have the same variable name with
	 * an implied value.
	 */
	private void addProjectionsFromUnion(SelectQuery query, UnionItem union) {
		for (ColumnVar var : union.getSelectColumns()) {
			if (!query.hasSqlSelectVarName(var.getName())) {
				SelectProjection proj = new SelectProjection();
				proj.setVar(var);
				proj.setId(new RefIdColumn(var));
				proj.setStringValue(coalesce(new URIColumn(var), new BNodeColumn(var), new LabelColumn(var),
						new LongLabelColumn(var), new LongURIColumn(var)));
				proj.setDatatype(new DatatypeColumn(var));
				proj.setLanguage(new LanguageColumn(var));
				query.addSqlSelectVar(proj);
			}
		}
	}

	@Override
	public void meet(Join node)
		throws RdbmsException
	{
		super.meet(node);
		for (TupleExpr arg : node.getArgs()) {
			if (!(arg instanceof SelectQuery))
				return;
			SelectQuery sq = (SelectQuery) arg;
			if (sq.isComplex())
				return;
		}
		assert node.getNumberOfArguments() > 0;
		SelectQuery left = null;
		for (TupleExpr arg : node.getArgs()) {
			if (left == null) {
				left = (SelectQuery) arg.clone();
			} else {
				SelectQuery right = (SelectQuery) arg.clone();
				filterOn(left, right);
				mergeSelectClause(left, right);
				left.addJoin(right);
			}
		}
		node.replaceWith(left);
	}

	@Override
	public void meet(LeftJoin node)
		throws RdbmsException
	{
		super.meet(node);
		TupleExpr l = node.getLeftArg();
		TupleExpr r = node.getRightArg();
		if (!(l instanceof SelectQuery && r instanceof SelectQuery))
			return;
		SelectQuery left = (SelectQuery)l;
		SelectQuery right = (SelectQuery)r;
		if (left.isComplex() || right.isComplex())
			return;
		Set<String> names = new HashSet<String>(left.getBindingNames());
		names.retainAll(right.getBindingNames());
		if (names.isEmpty())
			return;
		left = left.clone();
		right = right.clone();
		filterOn(left, right);
		mergeSelectClause(left, right);
		left.addLeftJoin(right);
		List<SqlExpr> filters = new ArrayList<SqlExpr>();
		if (node.getCondition() != null) {
			for (ValueExpr expr : flatten(node.getCondition())) {
				try {
					filters.add(sql.createBooleanExpr(expr));
				}
				catch (UnsupportedRdbmsOperatorException e) {
					return;
				}
			}
		}
		for (SqlExpr filter : filters) {
			right.addFilter(filter);
		}
		node.replaceWith(left);
	}

	@Override
	public void meet(StatementPattern sp)
		throws RdbmsException
	{
		super.meet(sp);
		Var subjVar = sp.getSubjectVar();
		Var predVar = sp.getPredicateVar();
		Var objVar = sp.getObjectVar();
		Var ctxVar = sp.getContextVar();

		Value subjValue = getVarValue(subjVar, bindings);
		Value predValue = getVarValue(predVar, bindings);
		Value objValue = getVarValue(objVar, bindings);
		Value ctxValue = getVarValue(ctxVar, bindings);

		if (subjValue instanceof Literal || predValue instanceof Literal || predValue instanceof BNode
				|| ctxValue instanceof Literal)
		{
			// subj and ctx must be a Resource and pred must be a URI
			return;
		}

		Resource[] contexts = getContexts(sp, ctxValue);
		if (contexts == null)
			return;

		String alias = getTableAlias(predValue) + aliasCount++;
		Number predId = getInternalId(predValue);
		String tableName;
		boolean present;
		try {
			tableName = tables.getTableName(predId);
			present = tables.isPredColumnPresent(predId);
		}
		catch (SQLException e) {
			throw new RdbmsException(e);
		}
		JoinItem from = new JoinItem(alias, tableName, predId);

		ColumnVar s = createSubj(alias, subjVar, (Resource)subjValue);
		ColumnVar p = createPred(alias, predVar, (URI)predValue, !present);
		ColumnVar o = createObj(alias, objVar, objValue);
		ColumnVar c = createCtx(alias, ctxVar, (Resource)ctxValue);

		s.setTypes(tables.getSubjTypes(predId));
		o.setTypes(tables.getObjTypes(predId));

		SelectQuery query = new SelectQuery();
		query.setFrom(from);
		Map<String, ColumnVar> vars = new HashMap<String, ColumnVar>(4);
		for (ColumnVar var : new ColumnVar[] { s, p, o, c }) {
			from.addVar(var);
			Value value = var.getValue();
			if (vars.containsKey(var.getName())) {
				IdColumn existing = new IdColumn(vars.get(var.getName()));
				from.addFilter(new SqlEq(new IdColumn(var), existing));
			}
			else if (value != null && !var.isImplied()) {
				try {
					NumberValue vc = new NumberValue(vf.getInternalId(value));
					from.addFilter(new SqlEq(new RefIdColumn(var), vc));
				}
				catch (RdbmsException e) {
					throw new RdbmsException(e);
				}
			}
			else {
				vars.put(var.getName(), var);
			}
			if (!var.isHiddenOrConstant() && value == null) {
				SelectProjection proj = new SelectProjection();
				proj.setVar(var);
				proj.setId(new RefIdColumn(var));
				proj.setStringValue(coalesce(new URIColumn(var), new BNodeColumn(var), new LabelColumn(var),
						new LongLabelColumn(var), new LongURIColumn(var)));
				proj.setDatatype(new DatatypeColumn(var));
				proj.setLanguage(new LanguageColumn(var));
				query.addSqlSelectVar(proj);
			}
		}
		if (contexts.length > 0) {
			RdbmsResource[] ids = vf.asRdbmsResource(contexts);
			RefIdColumn var = new RefIdColumn(c);
			SqlExpr in = null;
			for (RdbmsResource id : ids) {
				NumberValue longValue;
				try {
					longValue = new NumberValue(vf.getInternalId(id));
				}
				catch (RdbmsException e) {
					throw new RdbmsException(e);
				}
				SqlEq eq = new SqlEq(var.clone(), longValue);
				if (in == null) {
					in = eq;
				}
				else {
					in = new SqlOr(in, eq);
				}
			}
			from.addFilter(in);
		}
		sp.replaceWith(query);
	}

	@Override
	public void meet(Filter node)
		throws RdbmsException
	{
		super.meet(node);
		if (node.getArg() instanceof SelectQuery) {
			SelectQuery query = (SelectQuery)node.getArg();
			ValueExpr condition = null;
			for (ValueExpr expr : flatten(node.getCondition())) {
				try {
					query.addFilter(sql.createBooleanExpr(expr));
				}
				catch (UnsupportedRdbmsOperatorException e) {
					if (condition == null) {
						condition = expr;
					}
					else {
						condition = new And(condition, expr);
					}
				}
			}
			if (condition == null) {
				node.replaceWith(node.getArg());
			}
			else {
				node.setCondition(condition);
			}
		}
	}

	@Override
	public void meet(Projection node)
		throws RdbmsException
	{
		super.meet(node);
		if (node.getArg() instanceof SelectQuery) {
			SelectQuery query = (SelectQuery)node.getArg();
			Map<String, String> bindingVars = new HashMap<String, String>();
			List<SelectProjection> selection = new ArrayList<SelectProjection>();
			ProjectionElemList list = node.getProjectionElemList();
			for (ProjectionElem e : list.getElements()) {
				String source = e.getSourceName();
				String target = e.getTargetName();
				bindingVars.put(target, source);
				SelectProjection s = query.getSelectProjection(source);
				if (s != null) {
					selection.add(s);
				}
			}
			query.setBindingVars(bindingVars);
			query.setSqlSelectVar(selection);
			node.replaceWith(query);
		}
	}

	@Override
	public void meet(Slice node)
		throws RdbmsException
	{
		super.meet(node);
		if (node.getArg() instanceof SelectQuery) {
			SelectQuery query = (SelectQuery)node.getArg();
			if (node.getOffset() > 0) {
				query.setOffset(node.getOffset());
			}
			if (node.getLimit() >= 0) {
				query.setLimit(node.getLimit());
			}
			node.replaceWith(query);
		}
	}

	@Override
	public void meet(Order node)
		throws RdbmsException
	{
		super.meet(node);
		if (!(node.getArg() instanceof SelectQuery))
			return;
		SelectQuery query = (SelectQuery)node.getArg();
		try {
			for (OrderElem e : node.getElements()) {
				ValueExpr expr = e.getExpr();
				boolean asc = e.isAscending();
				query.addOrder(sql.createBNodeExpr(expr), asc);
				query.addOrder(sql.createUriExpr(expr), asc);
				query.addOrder(sql.createNumericExpr(expr), asc);
				query.addOrder(sql.createDatatypeExpr(expr), asc);
				query.addOrder(sql.createTimeExpr(expr), asc);
				query.addOrder(sql.createLanguageExpr(expr), asc);
				query.addOrder(sql.createLabelExpr(expr), asc);
			}
			node.replaceWith(query);
		}
		catch (UnsupportedRdbmsOperatorException e) {
			// unsupported
		}
	}

	private void filterOn(SelectQuery left, SelectQuery right) {
		Map<String, ColumnVar> lvars = left.getVarMap();
		Map<String, ColumnVar> rvars = right.getVarMap();
		Set<String> names = new HashSet<String>(rvars.keySet());
		names.retainAll(lvars.keySet());
		for (String name : names) {
			ColumnVar l = lvars.get(name);
			ColumnVar r = rvars.get(name);
			if (!l.isImplied() && !r.isImplied()) {
				IdColumn rid = new IdColumn(r);
				SqlExpr filter = eq(rid, new IdColumn(l));
				if (r.isNullable()) {
					filter = or(isNull(rid), filter);
				}
				right.addFilter(filter);
			}
		}
	}

	private Number getInternalId(Value predValue)
		throws RdbmsException
	{
		try {
			return vf.getInternalId(predValue);
		}
		catch (RdbmsException e) {
			throw new RdbmsException(e);
		}
	}

	private Resource[] getContexts(StatementPattern sp, Value ctxValue) {
		if (dataset == null) {
			if (ctxValue != null)
				return new Resource[] { (Resource)ctxValue };
			return new Resource[0];
		}
		Set<URI> graphs = getGraphs(sp);
		if (graphs.isEmpty())
			return null; // Search zero contexts
		if (ctxValue == null)
			return graphs.toArray(new Resource[graphs.size()]);

		if (graphs.contains(ctxValue))
			return new Resource[] { (Resource)ctxValue };
		// pattern specifies a context that is not part of the dataset
		return null;
	}

	private Set<URI> getGraphs(StatementPattern sp) {
		if (sp.getScope() == Scope.DEFAULT_CONTEXTS)
			return dataset.getDefaultGraphs();
		return dataset.getNamedGraphs();
	}

	private String getTableAlias(Value predValue) {
		if (predValue != null) {
			String localName = ((URI)predValue).getLocalName();
			if (localName.length() >= 1) {
				String alias = localName.substring(0, 1);
				if (isLetters(alias)) {
					return alias;
				}
			}
		}
		return ALIAS;
	}

	private Value getVarValue(Var var, BindingSet bindings) {
		if (var == null) {
			return null;
		}
		else if (var.hasValue()) {
			return var.getValue();
		}
		else {
			return bindings.getValue(var.getName());
		}
	}

	private boolean isLetters(String alias) {
		for (int i = 0, n = alias.length(); i < n; i++) {
			if (!Character.isLetter(alias.charAt(i)))
				return false;
		}
		return true;
	}

	private void mergeSelectClause(SelectQuery left, SelectQuery right) {
		for (SelectProjection proj : right.getSqlSelectVar()) {
			if (!left.hasSqlSelectVar(proj)) {
				proj = proj.clone();
				ColumnVar var = proj.getVar();
				String name = var.getName();
				ColumnVar existing = left.getVar(name);
				if (existing != null) {
					proj.setVar(existing);
				}
				left.addSqlSelectVar(proj);
			}
		}
	}

	private List<ValueExpr> flatten(ValueExpr condition) {
		return flatten(condition, new ArrayList<ValueExpr>());
	}

	private List<ValueExpr> flatten(ValueExpr condition, List<ValueExpr> conditions) {
		if (condition instanceof And) {
			And and = (And)condition;
			for (ValueExpr arg : and.getArgs()) {
				flatten(arg, conditions);
			}
		}
		else {
			conditions.add(condition);
		}
		return conditions;
	}
}
