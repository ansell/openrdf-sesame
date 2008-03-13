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
	private int version;

	public URITable(ValueTable shorter, ValueTable longer) {
		super();
		this.shorter = shorter;
		this.longer = longer;
	}

	public void initialize() throws SQLException {
		shorter.initialize();
		longer.initialize();
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
		shorter.insert(id, value);
	}

	public void insertLong(long id, long hash, String value) throws SQLException, InterruptedException {
		longer.insert(id, value);
	}

	public void removedStatements(int count, String condition)
			throws SQLException {
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
	}
}
