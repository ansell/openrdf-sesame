/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.evaluation;

/**
 * Facilitates the building of a regular expression in SQL.
 * 
 * @author James Leigh
 * 
 */
public class SqlRegexBuilder {

	private SqlExprBuilder where;

	private SqlExprBuilder value;

	private SqlExprBuilder pattern;

	private SqlExprBuilder flags;

	public SqlRegexBuilder(SqlExprBuilder where, QueryBuilderFactory factory) {
		super();
		this.where = where;
		value = factory.createSqlExprBuilder();
		pattern = factory.createSqlExprBuilder();
		flags = factory.createSqlExprBuilder();
	}

	public SqlExprBuilder value() {
		return value;
	}

	public SqlExprBuilder pattern() {
		return pattern;
	}

	public SqlExprBuilder flags() {
		return flags;
	}

	public SqlExprBuilder close() {
		appendRegExp(where);
		return where;
	}

	protected void appendRegExp(SqlExprBuilder where) {
		where.append("REGEXP(");
		appendValue(where);
		where.append(", ");
		appendPattern(where);
		where.append(", ");
		appendFlags(where);
		where.append(")");
	}

	protected SqlExprBuilder appendValue(SqlExprBuilder where) {
		where.append(value.toSql());
		where.addParameters(value.getParameters());
		return where;
	}

	protected SqlExprBuilder appendPattern(SqlExprBuilder where) {
		where.append(pattern.toSql());
		where.addParameters(pattern.getParameters());
		return where;
	}

	protected SqlExprBuilder appendFlags(SqlExprBuilder where) {
		where.append(flags.toSql());
		where.addParameters(flags.getParameters());
		return where;
	}
}
