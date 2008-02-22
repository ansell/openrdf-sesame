/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.schema;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collection;

/**
 * Manages the rows in either the URI table or the BNode table.
 * 
 * @author James Leigh
 * 
 */
public class ResourceTable {
	public interface HandleIdValue {
		void handleIdValue(long id, String value);
	}

	private ValueTable table;
	private String selectByValue;
	private int version;

	public ResourceTable(ValueTable table) {
		super();
		this.table = table;
	}

	public void initialize() throws SQLException {
		table.initialize();
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT id, value\n");
		sb.append("FROM ").append(table.getName()).append("\n");
		sb.append("WHERE value IN (");
		for (int i = 0, n = getSelectChunkSize(); i < n; i++) {
			sb.append("?,");
		}
		sb.setCharAt(sb.length() - 1, ')');
		selectByValue = sb.toString();
	}

	public int flush() throws SQLException {
		return table.flush();
	}

	public String getName() {
		return table.getName();
	}

	public int getSelectChunkSize() {
		return table.getSelectChunkSize();
	}

	public int getBatchSize() {
		return table.getBatchSize();
	}

	public int getIdVersion() {
		return version;
	}

	public void insert(long id, String value) throws SQLException {
		table.insert(id, value);
	}

	public void load(Collection<String> values, HandleIdValue handler)
			throws SQLException {
		PreparedStatement stmt = prepareSelect();
		try {
			int p = 0;
			for (String value : values) {
				stmt.setString(++p, value);
				if (p < getSelectChunkSize())
					continue;
				importNeededIds(stmt, handler);
				p = 0;
			}
			if (p > 0) {
				while (p < getSelectChunkSize()) {
					stmt.setNull(++p, Types.VARCHAR);
				}
				importNeededIds(stmt, handler);
			}
		} finally {
			stmt.close();
		}
	}

	protected void importNeededIds(PreparedStatement stmt, HandleIdValue handler)
			throws SQLException {
		ResultSet rs = stmt.executeQuery();
		try {
			while (rs.next()) {
				long id = rs.getLong(1);
				String value = rs.getString(2);
				handler.handleIdValue(id, value);
			}
		} finally {
			rs.close();
		}
	}

	protected PreparedStatement prepareSelect() throws SQLException {
		return table.prepareStatement(selectByValue);
	}

	public void removedStatements(int count, String condition)
			throws SQLException {
		flush();
		if (table.expungeRemovedStatements(count, condition)) {
			version++;
		}
	}

	@Override
	public String toString() {
		return getName();
	}

	public void optimize() throws SQLException {
		flush();
		table.optimize();
	}
}
