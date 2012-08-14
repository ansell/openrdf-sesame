/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2011.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.postgresql;

import org.openrdf.sail.rdbms.evaluation.QueryBuilderFactory;
import org.openrdf.sail.rdbms.evaluation.SqlQueryBuilder;

/**
 * PostgreSQL-specific SQL query builder. 
 * Overrides default creation of LIMIT clause when creating SQL query string. 
 * 
 * @author Jeen Broekstra
 */
public class PgSqlQueryBuilder extends SqlQueryBuilder {

	/**
	 * Creates a new PgSQLQueryBuilder with the supplied factory.
	 * 
	 * @param factory
	 */
	public PgSqlQueryBuilder(QueryBuilderFactory factory) {
		super(factory);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT ");
		if (distinct) {
			sb.append("DISTINCT ");
		}
		if (select.isEmpty()) {
			sb.append("*");
		}
		else {
			sb.append(select.toSql());
		}
		if (from != null) {
			sb.append("\nFROM ").append(from.getFromClause());
			if (!from.on().isEmpty()) {
				sb.append("\nWHERE ");
				sb.append(from.on().toSql());
			}
		}
		sb.append(group);
		if (union != null && !union.isEmpty()) {
			sb.append("\nUNION ALL ");
			sb.append(union.toString());
		}
		if (!order.isEmpty()) {
			sb.append("\nORDER BY ").append(order.toSql());
		}
		if (limit != null) {
			// For some reason, PostgreSQL does not accept full Java long values as 
			// values for the limit clause (despite it being within BigInt value range).
			// Workaround is to use PgSql-specific "LIMIT ALL" syntax.
			if (Long.MAX_VALUE == limit) {
				sb.append("\nLIMIT ALL");
			}
			else {
				sb.append("\nLIMIT ").append(limit);
			}
		}
		if (offset != null) {
			sb.append("\nOFFSET ").append(offset);
		}
		return sb.toString();
	}
	
}
