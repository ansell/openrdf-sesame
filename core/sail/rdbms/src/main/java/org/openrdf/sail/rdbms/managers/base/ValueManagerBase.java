/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.managers.base;

import static org.openrdf.sail.rdbms.algebra.factories.HashExprFactory.hashOf;

import java.sql.SQLException;

import info.aduna.collections.LRUMap;

import org.openrdf.sail.rdbms.managers.HashManager;
import org.openrdf.sail.rdbms.model.RdbmsValue;

public abstract class ValueManagerBase<V extends RdbmsValue> extends ManagerBase {
	public static final boolean STORE_VALUES = true;
	private LRUMap<Object, V> cache;
	private HashManager hashes;

	public void setHashManager(HashManager hashes) {
		this.hashes = hashes;
	}

	public void init() {
		cache = new LRUMap<Object, V>(getBatchSize());
	}

	@Override
	public void flush()
		throws SQLException
	{
		super.flush();
		if (hashes != null) {
			hashes.flush();
		}
	}

	public V findInCache(Object key) {
		synchronized (cache) {
			if (cache.containsKey(key))
				return cache.get(key);
		}
		return null;
	}

	public void cache(V value) throws SQLException, InterruptedException {
		if (needsId(value)) {
			synchronized (cache) {
				cache.put(key(value), value);
			}
			if (hashes != null) {
				hashes.cache(value);
			}
		}
	}

	public long getInternalId(V val) throws SQLException, InterruptedException {
		if (val.getInternalId() == null) {
			val.setInternalId(getMissingId(val));
		}
		insert(val);
		return val.getInternalId();
	}

	public int getIdVersion() {
		return getTableVersion() + (hashes == null ? 0 : hashes.getIdVersion());
	}

	protected abstract int getTableVersion();

	protected abstract int getBatchSize();

	protected abstract void insert(long id, V value)
			throws SQLException, InterruptedException;

	protected abstract Object key(V value);

	@Override
	protected void optimize() throws SQLException {
		if (hashes != null) {
			hashes.optimize();
		}
	}

	private long getMissingId(V value) {
		if (hashes == null) {
			return hashOf(value);
		} else {
			return hashes.getInternalId(value);
		}
	}

	private void insert(V value)
		throws SQLException, InterruptedException
	{
		if (STORE_VALUES && needsId(value)) {
			Long id = value.getInternalId();
			value.setVersion(getIdVersion());
			insert(id, value);
			if (hashes != null) {
				hashes.insert(id, value);
			}
		}
	}

	private boolean needsId(V value) {
		if (value.getVersion() == null)
			return true;
		return value.getVersion().intValue() != getIdVersion();
	}

	public void removedStatements(int count, String condition)
		throws SQLException
	{
		if (hashes != null) {
			hashes.removedStatements(count, condition);
		}
	}

}
