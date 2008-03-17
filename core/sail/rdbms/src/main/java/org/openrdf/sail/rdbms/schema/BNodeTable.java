/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.schema;

import java.sql.SQLException;

/**
 * Manages the rows in the BNode table.
 * 
 * @author James Leigh
 * 
 */
public class BNodeTable {
	private ValueTable table;

	public BNodeTable(ValueTable table) {
		super();
		this.table = table;
	}

	public void close() throws SQLException {
		table.close();
	}

	public String getName() {
		return table.getName();
	}

	public int getBatchSize() {
		return table.getBatchSize();
	}

	public void insert(Number id, String value) throws SQLException, InterruptedException {
		table.insert(id, value);
	}

	public boolean expungeRemovedStatements(int count, String condition)
			throws SQLException {
		return table.expungeRemovedStatements(count, condition);
	}

	@Override
	public String toString() {
		return getName();
	}

	public void optimize() throws SQLException {
		table.optimize();
	}
}
