/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.evaluation;

import org.openrdf.sail.rdbms.RdbmsValueFactory;

/**
 * Creates the SQL query building components.
 * 
 * @author James Leigh
 * 
 */
public class QueryBuilderFactory {
	private RdbmsValueFactory vf;

	public void setValueFactory(RdbmsValueFactory vf) {
		this.vf = vf;
	}

	public QueryBuilder createQueryBuilder() {
		QueryBuilder query = new QueryBuilder(createSqlQueryBuilder());
		query.setValueFactory(vf);
		return query;
	}

	public SqlQueryBuilder createSqlQueryBuilder() {
		return new SqlQueryBuilder(this);
	}

	public SqlExprBuilder createSqlExprBuilder() {
		return new SqlExprBuilder(this);
	}

	public SqlRegexBuilder createSqlRegexBuilder(SqlExprBuilder where) {
		return new SqlRegexBuilder(where, this);
	}

	public SqlBracketBuilder createSqlBracketBuilder(SqlExprBuilder where) {
		return new SqlBracketBuilder(where, this);
	}

	public SqlJoinBuilder createSqlJoinBuilder(String table, String alias) {
		return new SqlJoinBuilder(table, alias, this);
	}
}
