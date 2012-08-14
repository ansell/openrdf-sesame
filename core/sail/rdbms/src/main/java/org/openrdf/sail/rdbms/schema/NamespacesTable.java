/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.schema;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.openrdf.sail.rdbms.exceptions.RdbmsRuntimeException;

/**
 * Manages the namespace prefix in the database.
 * 
 * @author James Leigh
 * 
 */
public class NamespacesTable {

	private RdbmsTable table;

	private String INSERT;

	private String UPDATE;

	private String UPDATE_ALL_NULL;

	public NamespacesTable(RdbmsTable table) {
		this.table = table;
		StringBuilder sb = new StringBuilder();
		sb.append("INSERT INTO ").append(table.getName());
		sb.append(" (prefix, namespace) VALUES (?, ?)");
		INSERT = sb.toString();
		sb.delete(0, sb.length());
		sb.append("UPDATE ").append(table.getName()).append("\n");
		sb.append("SET prefix = ?\n");
		sb.append("WHERE namespace = ?");
		UPDATE = sb.toString();
		sb.delete(0, sb.length());
		sb.append("UPDATE ").append(table.getName()).append("\n");
		sb.append("SET prefix = NULL\n");
		sb.append("WHERE prefix IS NOT NULL");
		UPDATE_ALL_NULL = sb.toString();
	}

	public void initialize()
		throws SQLException
	{
		if (!table.isCreated()) {
			createTable();
		}
	}

	public void close()
		throws SQLException
	{
		table.close();
	}

	protected void createTable()
		throws SQLException
	{
		StringBuilder sb = new StringBuilder();
		sb.append("  prefix VARCHAR(127),\n");
		sb.append("  namespace TEXT NOT NULL\n");
		createTable(sb);
	}

	protected void createTable(CharSequence sb)
		throws SQLException
	{
		table.createTable(sb);
	}

	public void insert(String prefix, String namespace)
		throws SQLException
	{
		int result = table.executeUpdate(INSERT, prefix, namespace);
		if (result != 1 && result != Statement.SUCCESS_NO_INFO)
			throw new RdbmsRuntimeException("Namespace could not be created");
		table.modified(1, 0);
		table.optimize();
	}

	public void updatePrefix(String prefix, String namespace)
		throws SQLException
	{
		int result = table.executeUpdate(UPDATE, prefix, namespace);
		if (result != 1 && result != Statement.SUCCESS_NO_INFO)
			throw new RdbmsRuntimeException("Namespace prefix could not be changed, result: " + result);
	}

	public void clearPrefixes()
		throws SQLException
	{
		table.execute(UPDATE_ALL_NULL);
	}

	public List<Object[]> selectAll()
		throws SQLException
	{
		return table.select("prefix", "namespace");
	}

	@Override
	public String toString() {
		return table.getName();
	}

}
