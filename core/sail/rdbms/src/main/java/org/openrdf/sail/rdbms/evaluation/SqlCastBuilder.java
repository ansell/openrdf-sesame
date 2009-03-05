/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.evaluation;

import java.sql.Types;

/**
 * Prints round brackets in an SQL query.
 * 
 * @author James Leigh
 */
public class SqlCastBuilder extends SqlExprBuilder {

	private SqlExprBuilder where;

	private int jdbcType;

	public SqlCastBuilder(SqlExprBuilder where, QueryBuilderFactory factory, int jdbcType) {
		super(factory);
		this.where = where;
		this.jdbcType = jdbcType;
		append(" CAST(");
	}

	public SqlExprBuilder close() {
		append(" AS ");
		append(getSqlType(jdbcType));
		append(")");
		where.append(toSql());
		where.addParameters(getParameters());
		return where;
	}

	protected CharSequence getSqlType(int type) {
		switch (type) {
			case Types.VARCHAR:
				return "VARCHAR";
			default:
				throw new AssertionError(type);
		}
	}
}
