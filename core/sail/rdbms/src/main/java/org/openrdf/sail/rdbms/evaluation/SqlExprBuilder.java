/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.evaluation;

import java.util.ArrayList;
import java.util.List;

import org.openrdf.query.algebra.MathExpr;
import org.openrdf.sail.rdbms.algebra.SqlCompare;

/**
 * Assemblies an SQL expression.
 * 
 * @author James Leigh
 */
public class SqlExprBuilder {

	protected class Mark {

		int length;

		int size;

		Mark(int length, int size) {
			this.length = length;
			this.size = size;
		}
	}

	private static final String NULL = " NULL ";

	private QueryBuilderFactory factory;

	private List<Object> parameters = new ArrayList<Object>();

	private StringBuilder where = new StringBuilder();

	public SqlExprBuilder(QueryBuilderFactory factory) {
		super();
		this.factory = factory;
	}

	public SqlBracketBuilder abs() {
		where.append(" ABS");
		return open();
	}

	public SqlExprBuilder and() {
		if (!isEmpty()) {
			where.append("\n AND ");
		}
		return this;
	}

	public SqlExprBuilder append(CharSequence sql) {
		where.append(sql);
		return this;
	}

	public void appendBoolean(boolean booleanValue) {
		if (booleanValue) {
			where.append(" 1=1 ");
		}
		else {
			where.append(" 0=1 ");
		}
	}

	public SqlExprBuilder appendNull() {
		where.append(getSqlNull());
		return this;
	}

	public SqlExprBuilder appendNumeric(Number doubleValue) {
		where.append(" ? ");
		parameters.add(doubleValue);
		return this;
	}

	public void appendOperator(SqlCompare.Operator op) {
		switch (op) {
			case GE:
				where.append(" >= ");
				break;
			case GT:
				where.append(" > ");
				break;
			case LE:
				where.append(" <= ");
				break;
			case LT:
				where.append(" < ");
				break;
		}
	}

	public void as(String column) {
		where.append(" AS ").append(column);
	}

	public SqlExprBuilder number(Number time) {
		where.append(" ? ");
		parameters.add(time);
		return this;
	}

	public SqlCaseBuilder caseBegin() {
		return new SqlCaseBuilder(this);
	}

	public SqlCastBuilder cast(int jdbcType) {
		return factory.createSqlCastBuilder(this, jdbcType);
	}

	public SqlExprBuilder column(String alias, String column) {
		where.append(alias).append(".").append(column);
		return this;
	}

	public SqlExprBuilder columnEquals(String alias, String column, Number id) {
		return column(alias, column).eq().number(id);
	}

	public SqlExprBuilder columnEquals(String alias, String column, String label) {
		return column(alias, column).eq().varchar(label);
	}

	public SqlExprBuilder columnIn(String alias, String column, Number[] ids) {
		if (ids.length == 1) {
			return columnEquals(alias, column, ids[0]);
		}
		SqlBracketBuilder open = open();
		for (int i = 0; i < ids.length; i++) {
			if (i > 0) {
				open.or();
			}
			open.column(alias, column);
			open.eq();
			open.number(ids[i]);
		}
		open.close();
		return this;
	}

	public SqlExprBuilder columnsEqual(String al1, String col1, String al2, String col2) {
		return column(al1, col1).eq().column(al2, col2);
	}

	public void concat() {
		append(" || ");
	}

	public SqlExprBuilder eq() {
		where.append(" = ");
		return this;
	}

	public List<Object> getParameters() {
		return parameters;
	}

	public boolean isEmpty() {
		return where.length() == 0;
	}

	public SqlExprBuilder isNotNull() {
		where.append(" IS NOT NULL ");
		return this;
	}

	public SqlExprBuilder isNull() {
		where.append(" IS NULL ");
		return this;
	}

	public void like() {
		where.append(" LIKE ");
	}

	public SqlBracketBuilder lowerCase() {
		where.append(" lower");
		return open();
	}

	public void math(MathExpr.MathOp op) {
		append(" ").append(op.getSymbol()).append(" ");
	}

	public SqlBracketBuilder mod(int value) {
		where.append(" MOD");
		SqlBracketBuilder open = open();
		open.setClosing("," + value + open.getClosing());
		return open;
	}

	public SqlBracketBuilder not() {
		where.append(" NOT");
		return open();
	}

	public SqlExprBuilder notEqual() {
		where.append(" <> ");
		return this;
	}

	public SqlBracketBuilder open() {
		return factory.createSqlBracketBuilder(this);
	}

	public SqlExprBuilder or() {
		append(" OR ");
		return this;
	}

	public void plus(int range) {
		where.append(" + " + range);
	}

	public SqlRegexBuilder regex() {
		return factory.createSqlRegexBuilder(this);
	}

	public void rightShift(int rightShift) {
		where.append(" >> " + rightShift);
	}

	public CharSequence toSql() {
		return where;
	}

	@Override
	public String toString() {
		return where.toString();
	}

	public SqlExprBuilder varchar(String stringValue) {
		if (stringValue == null) {
			appendNull();
		}
		else {
			where.append(" ? ");
			parameters.add(stringValue);
		}
		return this;
	}

	protected void addParameters(List<Object> params) {
		parameters.addAll(params);
	}

	protected String getSqlNull() {
		return NULL;
	}

	protected Mark mark() {
		return new Mark(where.length(), parameters.size());
	}

	protected void reset(Mark mark) {
		where.delete(mark.length, where.length());
		for (int i = parameters.size() - 1; i >= mark.size; i--) {
			parameters.remove(i);
		}
	}

}
