/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.evaluation;

/**
 * Prints round brackets in an SQL query.
 * 
 * @author James Leigh
 * 
 */
public class SqlBracketBuilder extends SqlExprBuilder {

	private SqlExprBuilder where;

	private String closing = ")";

	public SqlBracketBuilder(SqlExprBuilder where, QueryBuilderFactory factory) {
		super(factory);
		this.where = where;
		append("(");
	}

	public String getClosing() {
		return closing;
	}

	public void setClosing(String closing) {
		this.closing = closing;
	}

	public SqlExprBuilder close() {
		append(closing);
		where.append(toSql());
		where.addParameters(getParameters());
		return where;
	}
}
