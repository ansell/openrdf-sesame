/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.evaluation;

import java.util.ArrayList;
import java.util.List;

import org.openrdf.model.Value;
import org.openrdf.sail.rdbms.RdbmsValueFactory;
import org.openrdf.sail.rdbms.algebra.BNodeColumn;
import org.openrdf.sail.rdbms.algebra.ColumnVar;
import org.openrdf.sail.rdbms.algebra.DatatypeColumn;
import org.openrdf.sail.rdbms.algebra.DateTimeColumn;
import org.openrdf.sail.rdbms.algebra.DoubleValue;
import org.openrdf.sail.rdbms.algebra.FalseValue;
import org.openrdf.sail.rdbms.algebra.HashColumn;
import org.openrdf.sail.rdbms.algebra.IdColumn;
import org.openrdf.sail.rdbms.algebra.JoinItem;
import org.openrdf.sail.rdbms.algebra.LabelColumn;
import org.openrdf.sail.rdbms.algebra.LanguageColumn;
import org.openrdf.sail.rdbms.algebra.LongLabelColumn;
import org.openrdf.sail.rdbms.algebra.LongURIColumn;
import org.openrdf.sail.rdbms.algebra.LongValue;
import org.openrdf.sail.rdbms.algebra.NumericColumn;
import org.openrdf.sail.rdbms.algebra.RefIdColumn;
import org.openrdf.sail.rdbms.algebra.SqlAbs;
import org.openrdf.sail.rdbms.algebra.SqlAnd;
import org.openrdf.sail.rdbms.algebra.SqlCase;
import org.openrdf.sail.rdbms.algebra.SqlCompare;
import org.openrdf.sail.rdbms.algebra.SqlConcat;
import org.openrdf.sail.rdbms.algebra.SqlEq;
import org.openrdf.sail.rdbms.algebra.SqlIsNull;
import org.openrdf.sail.rdbms.algebra.SqlLike;
import org.openrdf.sail.rdbms.algebra.SqlLowerCase;
import org.openrdf.sail.rdbms.algebra.SqlMathExpr;
import org.openrdf.sail.rdbms.algebra.SqlNot;
import org.openrdf.sail.rdbms.algebra.SqlNull;
import org.openrdf.sail.rdbms.algebra.SqlOr;
import org.openrdf.sail.rdbms.algebra.SqlRegex;
import org.openrdf.sail.rdbms.algebra.SqlShift;
import org.openrdf.sail.rdbms.algebra.StringValue;
import org.openrdf.sail.rdbms.algebra.TrueValue;
import org.openrdf.sail.rdbms.algebra.URIColumn;
import org.openrdf.sail.rdbms.algebra.UnionItem;
import org.openrdf.sail.rdbms.algebra.base.BinarySqlOperator;
import org.openrdf.sail.rdbms.algebra.base.FromItem;
import org.openrdf.sail.rdbms.algebra.base.SqlConstant;
import org.openrdf.sail.rdbms.algebra.base.SqlExpr;
import org.openrdf.sail.rdbms.algebra.base.UnarySqlOperator;
import org.openrdf.sail.rdbms.algebra.base.ValueColumnBase;
import org.openrdf.sail.rdbms.exceptions.RdbmsException;
import org.openrdf.sail.rdbms.exceptions.UnsupportedRdbmsOperatorException;

/**
 * Constructs an SQL query from {@link SqlExpr}s and {@link FromItem}s.
 * 
 * @author James Leigh
 * 
 */
public class QueryBuilder {
	private SqlQueryBuilder query;
	private RdbmsValueFactory vf;
	private boolean usingHashTable;

	public QueryBuilder(SqlQueryBuilder builder) {
		super();
		this.query = builder;
	}

	public void setValueFactory(RdbmsValueFactory vf) {
		this.vf = vf;
	}

	public void setUsingHashTable(boolean usingHashTable) {
		this.usingHashTable = usingHashTable;
	}

	public void distinct() {
		query.distinct();
	}

	public QueryBuilder filter(ColumnVar var, Value val) throws RdbmsException {
		String alias = var.getAlias();
		String column = var.getColumn();
		query.filter().and().columnEquals(alias, column, vf.getInternalId(val));
		return this;
	}

	public void from(FromItem from) throws RdbmsException,
			UnsupportedRdbmsOperatorException {
		from(query, from);
	}

	public List<?> getParameters() {
		return query.findParameters(new ArrayList<Object>());
	}

	public void limit(Integer limit) {
		query.limit(limit);
	}

	public void offset(Integer offset) {
		query.offset(offset);
	}

	public void orderBy(SqlExpr expr, boolean isAscending)
			throws UnsupportedRdbmsOperatorException {
		SqlExprBuilder orderBy = query.orderBy();
		dispatch(expr, orderBy);
		if (!isAscending) {
			orderBy.append(" DESC");
		}
	}

	public QueryBuilder select(SqlExpr expr)
			throws UnsupportedRdbmsOperatorException {
		dispatch(expr, query.select());
		return this;
	}

	@Override
	public String toString() {
		return query.toString();
	}

	private void append(BNodeColumn var, SqlExprBuilder filter) {
		String alias = getBNodeAlias(var.getRdbmsVar());
		filter.column(alias, "value");
	}

	private void append(DatatypeColumn var, SqlExprBuilder filter) {
		if (var.getRdbmsVar().isResource()) {
			filter.appendNull();
		} else {
			String alias = getDatatypeAlias(var.getRdbmsVar());
			filter.column(alias, "value");
		}
	}

	private void append(DateTimeColumn var, SqlExprBuilder filter) {
		if (var.getRdbmsVar().isResource()) {
			filter.appendNull();
		} else {
			String alias = getDateTimeAlias(var.getRdbmsVar());
			filter.column(alias, "value");
		}
	}

	private void append(DoubleValue expr, SqlExprBuilder filter) {
		filter.appendNumeric(expr.getValue());
	}

	private void append(FalseValue expr, SqlExprBuilder filter) {
		filter.appendBoolean(false);
	}

	private void append(HashColumn var, SqlExprBuilder filter) {
		if (usingHashTable) {
			String alias = getHashAlias(var.getRdbmsVar());
			filter.column(alias, "value");
		} else {
			filter.column(var.getAlias(), var.getColumn());
		}
	}

	private void append(IdColumn expr, SqlExprBuilder filter) {
		filter.column(expr.getAlias(), expr.getColumn());
	}

	private void append(LabelColumn var, SqlExprBuilder filter) {
		if (var.getRdbmsVar().isResource()) {
			filter.appendNull();
		} else {
			String alias = getLabelAlias(var.getRdbmsVar());
			filter.column(alias, "value");
		}
	}

	private void append(LongLabelColumn var, SqlExprBuilder filter) {
		if (var.getRdbmsVar().isResource()) {
			filter.appendNull();
		} else {
			String alias = getLongLabelAlias(var.getRdbmsVar());
			filter.column(alias, "value");
		}
	}

	private void append(LanguageColumn var, SqlExprBuilder filter) {
		if (var.getRdbmsVar().isResource()) {
			filter.appendNull();
		} else {
			String alias = getLanguageAlias(var.getRdbmsVar());
			filter.column(alias, "value");
		}
	}

	private void append(LongURIColumn uri, SqlExprBuilder filter) {
		ColumnVar var = uri.getRdbmsVar();
		String alias = getLongURIAlias(var);
		filter.column(alias, "value");
	}

	private void append(LongValue expr, SqlExprBuilder filter) {
		filter.number(expr.getValue());
	}

	private void append(NumericColumn var, SqlExprBuilder filter) {
		if (var.getRdbmsVar().isResource()) {
			filter.appendNull();
		} else {
			String alias = getNumericAlias(var.getRdbmsVar());
			filter.column(alias, "value");
		}
	}

	private void append(RefIdColumn expr, SqlExprBuilder filter) {
		filter.column(expr.getAlias(), expr.getColumn());
	}

	private void append(SqlAbs expr, SqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException {
		SqlBracketBuilder abs = filter.abs();
		dispatch(expr.getArg(), abs);
		abs.close();
	}

	private void append(SqlAnd expr, SqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException {
		dispatch(expr.getLeftArg(), filter);
		filter.and();
		dispatch(expr.getRightArg(), filter);
	}

	private void append(SqlCase expr, SqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException {
		SqlCaseBuilder caseExpr = filter.caseBegin();
		for (SqlCase.Entry e : expr.getEntries()) {
			caseExpr.when();
			dispatch(e.getCondition(), filter);
			caseExpr.then();
			dispatch(e.getResult(), filter);
		}
		caseExpr.end();
	}

	private void append(SqlCompare expr, SqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException {
		dispatch(expr.getLeftArg(), filter);
		filter.appendOperator(expr.getOperator());
		dispatch(expr.getRightArg(), filter);
	}

	private void append(SqlConcat expr, SqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException {
		SqlBracketBuilder open = filter.open();
		dispatch(expr.getLeftArg(), open);
		open.concat();
		dispatch(expr.getRightArg(), open);
		open.close();
	}

	private void append(SqlEq expr, SqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException {
		dispatch(expr.getLeftArg(), filter);
		filter.eq();
		dispatch(expr.getRightArg(), filter);
	}

	private void append(SqlIsNull expr, SqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException {
		dispatch(expr.getArg(), filter);
		filter.isNull();
	}

	private void append(SqlLike expr, SqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException {
		dispatch(expr.getLeftArg(), filter);
		filter.like();
		dispatch(expr.getRightArg(), filter);
	}

	private void append(SqlLowerCase expr, SqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException {
		SqlBracketBuilder lower = filter.lowerCase();
		dispatch(expr.getArg(), lower);
		lower.close();
	}

	private void append(SqlMathExpr expr, SqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException {
		dispatch(expr.getLeftArg(), filter);
		filter.math(expr.getOperator());
		dispatch(expr.getRightArg(), filter);
	}

	private void append(SqlNot expr, SqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException {
		if (expr.getArg() instanceof SqlIsNull) {
			SqlIsNull arg = (SqlIsNull) expr.getArg();
			dispatch(arg.getArg(), filter);
			filter.isNotNull();
		} else {
			SqlBracketBuilder open = filter.not();
			dispatch(expr.getArg(), open);
			open.close();
		}
	}

	private void append(SqlNull expr, SqlExprBuilder filter) {
		filter.appendNull();
	}

	private void append(SqlOr expr, SqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException {
		SqlBracketBuilder open = filter.open();
		dispatch(expr.getLeftArg(), open);
		open.or();
		dispatch(expr.getRightArg(), open);
		open.close();
	}

	private void append(SqlRegex expr, SqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException {
		SqlRegexBuilder regex = filter.regex();
		dispatch(expr.getArg(), regex.value());
		dispatch(expr.getPatternArg(), regex.pattern());
		SqlExpr flags = expr.getFlagsArg();
		if (flags != null) {
			dispatch(flags, regex.flags());
		}
		regex.close();
	}

	private void append(SqlShift expr, SqlExprBuilder filter) throws UnsupportedRdbmsOperatorException {
		SqlBracketBuilder mod = filter.mod(expr.getRange());
		SqlBracketBuilder open = mod.open();
		dispatch(expr.getArg(), open);
		open.rightShift(expr.getRightShift());
		open.close();
		mod.plus(expr.getRange());
		mod.close();
	}

	private void append(StringValue expr, SqlExprBuilder filter) {
		filter.varchar(expr.getValue());
	}

	private void append(TrueValue expr, SqlExprBuilder filter) {
		filter.appendBoolean(true);
	}

	private void append(URIColumn uri, SqlExprBuilder filter) {
		ColumnVar var = uri.getRdbmsVar();
		String alias = getURIAlias(var);
		filter.column(alias, "value");
	}

	private void dispatch(SqlExpr expr, SqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException {
		if (expr instanceof ValueColumnBase) {
			dispatchValueColumnBase((ValueColumnBase) expr, filter);
		} else if (expr instanceof IdColumn) {
			append((IdColumn) expr, filter);
		} else if (expr instanceof SqlConstant<?>) {
			dispatchSqlConstant((SqlConstant<?>) expr, filter);
		} else if (expr instanceof UnarySqlOperator) {
			dispatchUnarySqlOperator((UnarySqlOperator) expr, filter);
		} else if (expr instanceof BinarySqlOperator) {
			dispatchBinarySqlOperator((BinarySqlOperator) expr, filter);
		} else {
			dispatchOther(expr, filter);
		}
	}

	private void dispatchBinarySqlOperator(BinarySqlOperator expr,
			SqlExprBuilder filter) throws UnsupportedRdbmsOperatorException {
		if (expr instanceof SqlAnd) {
			append((SqlAnd) expr, filter);
		} else if (expr instanceof SqlEq) {
			append((SqlEq) expr, filter);
		} else if (expr instanceof SqlOr) {
			append((SqlOr) expr, filter);
		} else if (expr instanceof SqlCompare) {
			append((SqlCompare) expr, filter);
		} else if (expr instanceof SqlRegex) {
			append((SqlRegex) expr, filter);
		} else if (expr instanceof SqlConcat) {
			append((SqlConcat) expr, filter);
		} else if (expr instanceof SqlMathExpr) {
			append((SqlMathExpr) expr, filter);
		} else if (expr instanceof SqlLike) {
			append((SqlLike) expr, filter);
		} else {
			throw unsupported(expr);
		}
	}

	private void dispatchOther(SqlExpr expr, SqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException {
		if (expr instanceof SqlCase) {
			append((SqlCase) expr, filter);
		} else {
			throw unsupported(expr);
		}
	}

	private void dispatchSqlConstant(SqlConstant<?> expr, SqlExprBuilder filter)
			throws UnsupportedRdbmsOperatorException {
		if (expr instanceof DoubleValue) {
			append((DoubleValue) expr, filter);
		} else if (expr instanceof FalseValue) {
			append((FalseValue) expr, filter);
		} else if (expr instanceof TrueValue) {
			append((TrueValue) expr, filter);
		} else if (expr instanceof LongValue) {
			append((LongValue) expr, filter);
		} else if (expr instanceof SqlNull) {
			append((SqlNull) expr, filter);
		} else if (expr instanceof StringValue) {
			append((StringValue) expr, filter);
		} else {
			throw unsupported(expr);
		}
	}

	private void dispatchUnarySqlOperator(UnarySqlOperator expr,
			SqlExprBuilder filter) throws UnsupportedRdbmsOperatorException {
		if (expr instanceof SqlAbs) {
			append((SqlAbs) expr, filter);
		} else if (expr instanceof SqlIsNull) {
			append((SqlIsNull) expr, filter);
		} else if (expr instanceof SqlNot) {
			append((SqlNot) expr, filter);
		} else if (expr instanceof SqlShift) {
			append((SqlShift) expr, filter);
		} else if (expr instanceof SqlLowerCase) {
			append((SqlLowerCase) expr, filter);
		} else {
			throw unsupported(expr);
		}
	}

	private void dispatchValueColumnBase(ValueColumnBase expr,
			SqlExprBuilder filter) throws UnsupportedRdbmsOperatorException {
		if (expr instanceof BNodeColumn) {
			append((BNodeColumn) expr, filter);
		} else if (expr instanceof DatatypeColumn) {
			append((DatatypeColumn) expr, filter);
		} else if (expr instanceof HashColumn) {
			append((HashColumn) expr, filter);
		} else if (expr instanceof DateTimeColumn) {
			append((DateTimeColumn) expr, filter);
		} else if (expr instanceof LabelColumn) {
			append((LabelColumn) expr, filter);
		} else if (expr instanceof LongLabelColumn) {
			append((LongLabelColumn) expr, filter);
		} else if (expr instanceof LongURIColumn) {
			append((LongURIColumn) expr, filter);
		} else if (expr instanceof LanguageColumn) {
			append((LanguageColumn) expr, filter);
		} else if (expr instanceof NumericColumn) {
			append((NumericColumn) expr, filter);
		} else if (expr instanceof URIColumn) {
			append((URIColumn) expr, filter);
		} else if (expr instanceof RefIdColumn) {
			append((RefIdColumn) expr, filter);
		} else {
			throw unsupported(expr);
		}
	}

	private void from(SqlQueryBuilder subquery, FromItem item)
			throws RdbmsException, UnsupportedRdbmsOperatorException {
		assert !item.isLeft() : item;
		String alias = item.getAlias();
		if (item instanceof JoinItem) {
			String tableName = ((JoinItem) item).getTableName();
			subJoinAndFilter(subquery.from(tableName, alias), item);
		} else {
			subJoinAndFilter(subquery.from(alias), item);
		}
	}

	private String getBNodeAlias(ColumnVar var) {
		return "b" + getDBName(var);
	}

	private String getDatatypeAlias(ColumnVar var) {
		return "d" + getDBName(var);
	}

	private String getDateTimeAlias(ColumnVar var) {
		return "t" + getDBName(var);
	}

	private String getDBName(ColumnVar var) {
		String name = var.getName();
		if (name.indexOf('-') >= 0)
			return name.replace('-', '_');
		return "_" + name; // might be a keyword otherwise
	}

	private String getHashAlias(ColumnVar var) {
		return "h" + getDBName(var);
	}

	private String getLabelAlias(ColumnVar var) {
		return "l" + getDBName(var);
	}

	private String getLongLabelAlias(ColumnVar var) {
		return "ll" + getDBName(var);
	}

	private String getLongURIAlias(ColumnVar var) {
		return "lu" + getDBName(var);
	}

	private String getLanguageAlias(ColumnVar var) {
		return "g" + getDBName(var);
	}

	private String getNumericAlias(ColumnVar var) {
		return "n" + getDBName(var);
	}

	private String getURIAlias(ColumnVar var) {
		return "u" + getDBName(var);
	}

	private void join(SqlJoinBuilder query, FromItem join)
			throws RdbmsException, UnsupportedRdbmsOperatorException {
		String alias = join.getAlias();
		if (join instanceof JoinItem) {
			String tableName = ((JoinItem) join).getTableName();
			if (join.isLeft()) {
				subJoinAndFilter(query.leftjoin(tableName, alias), join);
			} else {
				subJoinAndFilter(query.join(tableName, alias), join);
			}
		} else {
			if (join.isLeft()) {
				subJoinAndFilter(query.leftjoin(alias), join);
			} else {
				subJoinAndFilter(query.join(alias), join);
			}
		}
	}

	private SqlJoinBuilder subJoinAndFilter(SqlJoinBuilder query, FromItem from)
			throws RdbmsException, UnsupportedRdbmsOperatorException {
		if (from instanceof UnionItem) {
			UnionItem union = (UnionItem) from;
			List<String> names = union.getSelectVarNames();
			List<ColumnVar> vars = union.appendVars(new ArrayList<ColumnVar>());
			SqlQueryBuilder subquery = query.subquery();
			for (FromItem item : union.getUnion()) {
				for (int i = 0, n = names.size(); i < n; i++) {
					ColumnVar var = item.getVar(names.get(i));
					SqlExprBuilder select = subquery.select();
					if (var == null) {
						select.appendNull();
					} else {
						select.column(var.getAlias(), var.getColumn());
					}
					select.as(vars.get(i).getColumn());
				}
				from(subquery, item);
				subquery = subquery.union();
			}
		}
		for (FromItem join : from.getJoins()) {
			join(query, join);
		}
		for (SqlExpr expr : from.getFilters()) {
			dispatch(expr, query.on().and());
		}
		return query;
	}

	private UnsupportedRdbmsOperatorException unsupported(Object object)
			throws UnsupportedRdbmsOperatorException {
		return new UnsupportedRdbmsOperatorException(object.toString());
	}

}
