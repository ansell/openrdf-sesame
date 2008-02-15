/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.postgresql;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

import org.openrdf.sail.rdbms.schema.RdbmsTable;
import org.openrdf.sail.rdbms.schema.ValueTable;

/**
 * Optimises prepared insert statements for PostgreSQL and overrides the DOUBLE
 * column type.
 * 
 * @author James Leigh
 * 
 */
public class PgSqlValueTable extends ValueTable {
	private RdbmsTable table;
	private String PREPARE_INSERT;
	private String EXECUTE_INSERT;

	public PgSqlValueTable(RdbmsTable table, int sqlType, int length) {
		super(table, sqlType, length);
		this.table = table;
		StringBuilder sb = new StringBuilder();
		sb.append("PREPARE ").append(table.getName());
		sb.append("_insert (bigint, ");
		sb.append(getDeclaredSqlType(sqlType, length)
				.replaceAll("\\(.*\\)", ""));
		sb.append(") AS\n");
		sb.append("INSERT INTO ").append(table.getName());
		sb.append(" VALUES ($1, $2)");
		PREPARE_INSERT = sb.toString();
		sb.delete(0, sb.length());
		sb.append("EXECUTE ").append(table.getName());
		sb.append("_insert(?, ?)");
		EXECUTE_INSERT = sb.toString();
	}

	@Override
	public void initialize() throws SQLException {
		super.initialize();
		table.execute(PREPARE_INSERT);
	}

	@Override
	public PreparedStatement prepareInsert() throws SQLException {
		return table.prepareStatement(EXECUTE_INSERT);
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
