/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.schema;

import java.sql.SQLException;

/**
 * Manages the rows in the URI table.
 * 
 * @author James Leigh
 * 
 */
public class URITable {
	private ValueTable shorter;
	private ValueTable longer;
	private HashTable hashTable;
	private int version;

	public URITable(ValueTable shorter, ValueTable longer, HashTable hash) {
		super();
		this.shorter = shorter;
		this.longer = longer;
		this.hashTable = hash;
	}

	public void close() throws SQLException {
		shorter.close();
		longer.close();
	}

	public int getBatchSize() {
		return shorter.getBatchSize();
	}

	public int getIdVersion() {
		return version;
	}

	public String getShortTableName() {
		return shorter.getName();
	}

	public String getLongTableName() {
		return longer.getName();
	}

	public void insertShort(long id, long hash, String value) throws SQLException, InterruptedException {
		hashTable.insert(id, hash);
		shorter.insert(id, value);
	}

	public void insertLong(long id, long hash, String value) throws SQLException, InterruptedException {
		hashTable.insert(id, hash);
		longer.insert(id, value);
	}

	public void removedStatements(int count, String condition)
			throws SQLException {
		hashTable.expungeRemovedStatements(count, condition);
		if (shorter.expungeRemovedStatements(count, condition)) {
			version++;
		}
		if (longer.expungeRemovedStatements(count, condition)) {
			version++;
		}
	}

	@Override
	public String toString() {
		return shorter.getName() + " UNION ALL " + longer.getName();
	}

	public void optimize() throws SQLException {
		shorter.optimize();
		longer.optimize();
		hashTable.optimize();
	}
}
