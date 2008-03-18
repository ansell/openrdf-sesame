/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.postgresql;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.openrdf.sail.rdbms.schema.HashTable;
import org.openrdf.sail.rdbms.schema.ValueTable;


/**
 *
 * @author james
 */
public class PgSqlHashtable extends HashTable {
	private ValueTable table;
	private String execute;

	public PgSqlHashtable(ValueTable table) {
		super(table);
		this.table = table;
	}

	@Override
	public void close()
		throws SQLException
	{
		if (execute != null) {
			table.getRdbmsTable().execute("DEALLOCATE " + getName() + "_select");
		}
		super.close();
	}

	@Override
	protected PreparedStatement prepareSelect(String sql)
		throws SQLException
	{
		if (execute == null) {
			StringBuilder sb = new StringBuilder();
			sb.append("PREPARE ").append(getName()).append("_select (");
			for (int i = 0, n = getSelectChunkSize(); i < n; i++) {
				sb.append("bigint,");
			}
			sb.setCharAt(sb.length() - 1, ')');
			sb.append(" AS\n");
			sb.append("SELECT id, value\nFROM ").append(getName());
			sb.append("\nWHERE value IN (");
			for (int i = 0, n = getSelectChunkSize(); i < n; i++) {
				sb.append("$").append(i + 1).append(",");
			}
			sb.setCharAt(sb.length() - 1, ')');
			table.getRdbmsTable().execute(sb.toString());
			sb.delete(0, sb.length());
			sb.append("EXECUTE ").append(getName()).append("_select (");
			for (int i = 0, n = getSelectChunkSize(); i < n; i++) {
				sb.append("?,");
			}
			sb.setCharAt(sb.length() - 1, ')');
			execute = sb.toString();
		}
		return super.prepareSelect(execute);
	}

}
