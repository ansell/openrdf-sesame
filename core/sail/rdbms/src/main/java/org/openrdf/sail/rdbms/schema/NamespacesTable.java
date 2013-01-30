/* 
 * Licensed to Aduna under one or more contributor license agreements.  
 * See the NOTICE.txt file distributed with this work for additional 
 * information regarding copyright ownership. 
 *
 * Aduna licenses this file to you under the terms of the Aduna BSD 
 * License (the "License"); you may not use this file except in compliance 
 * with the License. See the LICENSE.txt file distributed with this work 
 * for the full License.
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing permissions
 * and limitations under the License.
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
