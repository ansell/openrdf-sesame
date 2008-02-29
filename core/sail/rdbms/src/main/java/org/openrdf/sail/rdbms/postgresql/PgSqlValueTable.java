/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.postgresql;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

import org.openrdf.sail.rdbms.schema.ValueTable;

/**
 * Optimises prepared insert statements for PostgreSQL and overrides the DOUBLE
 * column type.
 * 
 * @author James Leigh
 * 
 */
public class PgSqlValueTable extends ValueTable {
	private String EXECUTE_INSERT;

	@Override
	public void initialize() throws SQLException {
		super.initialize();
		StringBuilder sb = new StringBuilder();
		sb.append("PREPARE ").append(getRdbmsTable().getName());
		sb.append("_insert (bigint, ");
		sb.append(getDeclaredSqlType(getSqlType(), getLength())
				.replaceAll("\\(.*\\)", ""));
		sb.append(") AS\n");
		sb.append("INSERT INTO ").append(getRdbmsTable().getName());
		sb.append(" VALUES ($1, $2)");
		getRdbmsTable().execute(sb.toString());
		
		sb.delete(0, sb.length());
		sb.append("EXECUTE ").append(getRdbmsTable().getName());
		sb.append("_insert(?, ?)");
		EXECUTE_INSERT = sb.toString();
	}

	@Override
	protected PreparedStatement prepareInsert() throws SQLException {
		return getRdbmsTable().prepareStatement(EXECUTE_INSERT);
	}

	@Override
	protected String getDeclaredSqlType(int type, int length) {
		switch (type) {
		case Types.DOUBLE:
			return "double precision";
		default:
			return super.getDeclaredSqlType(type, length);
		}
	}

}
