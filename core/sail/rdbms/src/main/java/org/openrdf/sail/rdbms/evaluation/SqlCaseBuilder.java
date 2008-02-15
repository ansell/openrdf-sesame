/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.evaluation;

/**
 * Facilitates the creation of a CASE expression in SQL.
 * 
 * @author James Leigh
 * 
 */
public class SqlCaseBuilder {
	private SqlExprBuilder where;

	public SqlCaseBuilder(SqlExprBuilder where) {
		super();
		this.where = where;
		where.append("CASE ");
	}

	public SqlExprBuilder when() {
		where.append(" WHEN ");
		return where;
	}

	public SqlExprBuilder then() {
		where.append(" THEN ");
		return where;
	}

	public SqlExprBuilder end() {
		where.append(" END");
		return where;
	}
}
