/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.schema;

import java.sql.SQLException;


/**
 *
 * @author James Leigh
 */
public class HashTable {
	private ValueTable table;
	private int version;

	public HashTable(ValueTable table) {
		super();
		this.table = table;
	}

	public String getName() {
		return table.getName();
	}

	public int getIdVersion() {
		return version;
	}

	public int getBatchSize() {
		return table.getBatchSize();
	}

	public void initialize()
		throws SQLException
	{
		table.initialize();
	}

	public void close()
		throws SQLException
	{
		table.close();
	}

	public void insert(long id, long hash)
		throws SQLException, InterruptedException
	{
		synchronized (table) {
			HashBatch batch = (HashBatch)table.getValueBatch();
			if (table.isExpired(batch)) {
				batch = newHashBatch();
				table.initBatch(batch);
			}
			batch.addBatch(id, hash);
			table.queue(batch);
		}
	}

	public void removedStatements(int count, String condition)
			throws SQLException {
		if (table.expungeRemovedStatements(count, condition)) {
			version++;
		}
	}

	public void optimize()
		throws SQLException
	{
		table.optimize();
	}

	public String toString() {
		return table.toString();
	}

	protected HashBatch newHashBatch() {
		return new HashBatch();
	}

}
