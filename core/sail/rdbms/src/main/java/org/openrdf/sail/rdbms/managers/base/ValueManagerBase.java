/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.managers.base;

import java.sql.SQLException;

import info.aduna.collections.LRUMap;

import org.openrdf.sail.rdbms.managers.HashManager;
import org.openrdf.sail.rdbms.model.RdbmsValue;
import org.openrdf.sail.rdbms.schema.IdCode;

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
		if (!STORE_VALUES)
			return null;
		synchronized (cache) {
			if (cache.containsKey(key))
				return cache.get(key);
		}
		return null;
	}

	public V cache(V value) throws SQLException, InterruptedException {
		if (!STORE_VALUES)
			return value;
		if (!needsId(value))
			return value;
		synchronized (cache) {
			cache.put(key(value), value);
		}
		insert(value);
		return value;
	}

	public long getInternalId(V val) throws SQLException, InterruptedException {
		insert(val);
		if (val.getInternalId() != null)
			return val.getInternalId();
		long id = getMissingId(val);
		val.setInternalId(id);
		return id;
	}

	public int getIdVersion() {
		return getTableVersion() + (hashes == null ? 0 : hashes.getIdVersion());
	}

	protected abstract int getTableVersion();

	protected abstract int getBatchSize();

	protected abstract void insert(long id, V value)
			throws SQLException, InterruptedException;

	protected abstract Object key(V value);

	protected abstract long getMissingId(V value);

	@Override
	protected void optimize() throws SQLException {
		if (hashes != null) {
			hashes.optimize();
		}
	}

	private void insert(V value)
		throws SQLException, InterruptedException
	{
		if (needsId(value)) {
			Long id = value.getInternalId();
			if (id == null) {
				id = getMissingId(value);
			}
			value.setInternalId(id);
			value.setVersion(getIdVersion());
			if (hashes != null) {
				hashes.insert(id, IdCode.valueOf(value).hash(value));
			}
			insert(id, value);
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
