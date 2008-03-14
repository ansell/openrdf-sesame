/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.managers;


import static org.openrdf.sail.rdbms.algebra.factories.HashExprFactory.hashOf;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.openrdf.sail.rdbms.managers.base.ManagerBase;
import org.openrdf.sail.rdbms.model.RdbmsValue;
import org.openrdf.sail.rdbms.schema.Batch;
import org.openrdf.sail.rdbms.schema.HashBatch;
import org.openrdf.sail.rdbms.schema.HashTable;

/**
 * 
 * @author James Leigh
 */
public class HashManager extends ManagerBase {

	public static HashManager instance;

	private HashTable table;

	private Map<Long, Long> ids;

	public HashManager() {
		instance = this;
	}

	public void setHashTable(HashTable table) {
		this.table = table;
		ids = new HashMap<Long, Long>(table.getBatchSize());
	}

	@Override
	public void close()
		throws SQLException
	{
		super.close();
		table.close();
	}

	public int getIdVersion() {
		return table.getIdVersion();
	}

	public void cache(RdbmsValue value) {
		long id = hashOf(value);
		synchronized (ids) {
			ids.put(hashOf(value), id);
		}
	}

	public long getInternalId(RdbmsValue value) {
		return hashOf(value);
	}

	public void insert(long id, RdbmsValue value)
		throws SQLException, InterruptedException
	{
		table.insert(id, hashOf(value));
	}

	public void optimize()
		throws SQLException
	{
		table.optimize();
	}

	public void removedStatements(int count, String condition) throws SQLException {
		table.removedStatements(count, condition);
	}

	@Override
	protected void flush(Batch batch)
		throws SQLException
	{
		super.flush(batch);
		synchronized (ids) {
			HashBatch hb = (HashBatch) batch;
			for (Long hash : hb.getHashes()) {
				ids.remove(hash);
			}
		}
	}

}
