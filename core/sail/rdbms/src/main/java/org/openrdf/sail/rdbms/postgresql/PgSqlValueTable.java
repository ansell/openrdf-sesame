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

	@Override
	public void initialize() throws SQLException {
		super.initialize();

		RdbmsTable tmp = getTemporaryTable();
		StringBuilder sb = new StringBuilder();
		sb.append("PREPARE ").append(tmp.getName());
		sb.append("_insert (bigint, ");
		sb.append(getDeclaredSqlType(getSqlType(), getLength())
				.replaceAll("\\(.*\\)", ""));
		sb.append(") AS\n");
		sb.append("INSERT INTO ").append(tmp.getName());
		sb.append(" VALUES ($1, $2)");
		tmp.execute(sb.toString());

		sb.delete(0, sb.length());
		sb.append("PREPARE ").append(tmp.getName());
		sb.append("_insert_select AS\n");
		sb.append("INSERT INTO ").append(getRdbmsTable().getName());
		sb.append(" (id, value) SELECT DISTINCT id, value FROM ");
		sb.append(tmp.getName()).append(" tmp\n");
		sb.append("WHERE NOT EXISTS (SELECT id FROM ").append(getRdbmsTable().getName());
		sb.append(" val WHERE val.id = tmp.id)");
		tmp.execute(sb.toString());
	}

	@Override
	public void close()
		throws SQLException
	{
		RdbmsTable tmp = getTemporaryTable();
		tmp.execute("DEALLOCATE " + tmp.getName() + "_insert");
		tmp.execute("DEALLOCATE " + tmp.getName() + "_insert_select");
		super.close();
	}

	@Override
	protected PreparedStatement prepareInsert(String sql)
		throws SQLException
	{
		StringBuilder sb = new StringBuilder();
		sb.append("EXECUTE ").append(getTemporaryTable().getName());
		sb.append("_insert(?, ?)");
		return super.prepareInsert(sb.toString());
	}

	@Override
	protected PreparedStatement prepareInsertSelect(String sql)
		throws SQLException
	{
		StringBuilder sb = new StringBuilder();
		sb.append("EXECUTE ").append(getTemporaryTable().getName());
		sb.append("_insert_select");
		return super.prepareInsertSelect(sb.toString());
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
