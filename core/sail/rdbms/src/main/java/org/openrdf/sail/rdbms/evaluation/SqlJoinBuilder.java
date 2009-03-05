/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.evaluation;

import java.util.ArrayList;
import java.util.List;

/**
 * Facilitates the building of a JOIN or FROM clause in SQL.
 * 
 * @author James Leigh
 */
public class SqlJoinBuilder {

	private boolean left;

	private SqlQueryBuilder subquery;

	private String table;

	private String alias;

	private QueryBuilderFactory factory;

	private List<SqlJoinBuilder> joins = new ArrayList<SqlJoinBuilder>();

	private SqlExprBuilder on;

	private List<SqlJoinBuilder> leftJoins = new ArrayList<SqlJoinBuilder>();

	public SqlJoinBuilder(String table, String alias, QueryBuilderFactory factory) {
		super();
		this.table = table;
		this.alias = alias;
		this.factory = factory;
		on = factory.createSqlExprBuilder();
	}

	public boolean isLeft() {
		return left;
	}

	public void setLeft(boolean left) {
		this.left = left;
	}

	public String getTable() {
		if (subquery == null) {
			return table;
		}
		return "( " + subquery.toString() + " )";
	}

	public String getAlias() {
		return alias;
	}

	public List<SqlJoinBuilder> getLeftJoins() {
		return leftJoins;
	}

	public SqlJoinBuilder on(String column, String expression) {
		on.and().column(alias, column).eq().append(expression);
		return this;
	}

	public SqlJoinBuilder findJoin(String alias) {
		if (alias.equals(this.alias)) {
			return this;
		}
		SqlJoinBuilder result;
		for (SqlJoinBuilder join : joins) {
			result = join.findJoin(alias);
			if (result != null) {
				return result;
			}
		}
		for (SqlJoinBuilder join : leftJoins) {
			result = join.findJoin(alias);
			if (result != null) {
				return result;
			}
		}
		return null;
	}

	public SqlJoinBuilder join(String table, String alias) {
		SqlJoinBuilder join = findJoin(alias);
		if (join != null) {
			return join;
		}
		join = factory.createSqlJoinBuilder(table, alias);
		joins.add(join);
		return join;
	}

	public SqlJoinBuilder leftjoin(String table, String alias) {
		SqlJoinBuilder join = findJoin(alias);
		if (join != null) {
			return join;
		}
		join = factory.createSqlJoinBuilder(table, alias);
		join.setLeft(true);
		leftJoins.add(join);
		return join;
	}

	public SqlJoinBuilder join(String alias) {
		SqlJoinBuilder join = findJoin(alias);
		if (join != null) {
			return join;
		}
		join = factory.createSqlJoinBuilder(null, alias);
		joins.add(join);
		return join;
	}

	public SqlJoinBuilder leftjoin(String alias) {
		SqlJoinBuilder join = findJoin(alias);
		if (join != null) {
			return join;
		}
		join = factory.createSqlJoinBuilder(null, alias);
		join.setLeft(true);
		leftJoins.add(join);
		return join;
	}

	public SqlQueryBuilder subquery() {
		assert table == null : table;
		assert subquery == null : subquery;
		return subquery = factory.createSqlQueryBuilder();
	}

	public SqlExprBuilder on() {
		return on;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (isLeft()) {
			sb.append("LEFT ");
		}
		sb.append("JOIN ").append(getJoinClause());
		sb.append(" ON (").append(on()).append(")");
		return sb.toString();
	}

	public CharSequence getFromClause() {
		if (joins.isEmpty() && leftJoins.isEmpty()) {
			return getTable() + " " + alias;
		}
		StringBuilder from = new StringBuilder();
		from.append(getTable()).append(" ").append(alias);
		for (SqlJoinBuilder join : joins) {
			appendJoin(from, join);
		}
		for (SqlJoinBuilder left : getLeftJoins()) {
			from.append("\n LEFT JOIN ").append(left.getJoinClause());
			from.append(" ON (").append(left.on()).append(")");
		}
		return from;
	}

	protected CharSequence getJoinClause() {
		if (joins.isEmpty() && leftJoins.isEmpty()) {
			return getTable() + " " + alias;
		}
		StringBuilder from = new StringBuilder();
		from.append("(");
		from.append(getFromClause());
		from.append(")");
		return from;
	}

	protected void appendJoin(StringBuilder from, SqlJoinBuilder join) {
		if (join.isLeft()) {
			from.append("\n LEFT JOIN ").append(join.getJoinClause());
		}
		else if (join.on().isEmpty()) {
			from.append(getSqlCrossJoin()).append(join.getJoinClause());
		}
		else {
			from.append("\n INNER JOIN ").append(join.getJoinClause());
		}
		if (!join.on().isEmpty()) {
			from.append(" ON (").append(join.on()).append(")");
		}
		else if (join.isLeft()) {
			from.append(" ON (").append(alias).append(".ctx = ").append(alias).append(".ctx)");
		}
	}

	protected String getSqlCrossJoin() {
		return "\n CROSS JOIN ";
	}

	public List<Object> findParameters(List<Object> parameters) {
		if (subquery != null) {
			subquery.findParameters(parameters);
		}
		for (SqlJoinBuilder join : joins) {
			join.findParameters(parameters);
		}
		for (SqlJoinBuilder join : leftJoins) {
			join.findParameters(parameters);
		}
		parameters.addAll(on.getParameters());
		return parameters;
	}
}
