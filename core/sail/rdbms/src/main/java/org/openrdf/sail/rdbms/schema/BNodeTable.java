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
	private HashTable hashTable;
	private int version;

	public BNodeTable(ValueTable table, HashTable hash) {
		super();
		this.table = table;
		this.hashTable = hash;
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

	public int getIdVersion() {
		return version;
	}

	public void insert(long id, long hash, String value) throws SQLException, InterruptedException {
		hashTable.insert(id, hash);
		table.insert(id, value);
	}

	public void removedStatements(int count, String condition)
			throws SQLException {
		hashTable.expungeRemovedStatements(count, condition);
		if (table.expungeRemovedStatements(count, condition)) {
			version++;
		}
	}

	@Override
	public String toString() {
		return getName();
	}

	public void optimize() throws SQLException {
		hashTable.optimize();
		table.optimize();
	}
}
