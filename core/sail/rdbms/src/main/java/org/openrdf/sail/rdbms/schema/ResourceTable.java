/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.schema;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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
	private int version;

	public ResourceTable(ValueTable table) {
		super();
		this.table = table;
	}

	public void initialize() throws SQLException {
		table.initialize();
	}

	public int flush() throws SQLException {
		return table.flush();
	}

	public String getName() {
		return table.getName();
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
		table.optimize();
	}
}
