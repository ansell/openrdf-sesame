/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.managers.base;

import java.sql.SQLException;

import info.aduna.collections.LRUMap;

import org.openrdf.sail.rdbms.model.RdbmsValue;

public abstract class ValueManagerBase<K, V extends RdbmsValue> extends ManagerBase {
	public static final boolean STORE_VALUES = true;
	private LRUMap<K, V> cache;

	public void init() {
		cache = new LRUMap<K, V>(getBatchSize());
	}

	public V findInCache(K key) {
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

	protected abstract int getIdVersion();

	protected abstract int getBatchSize();

	protected abstract void insert(long id, V value)
			throws SQLException, InterruptedException;

	protected abstract K key(V value);

	protected abstract long getMissingId(V value);

	protected abstract void optimize() throws SQLException;

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
			insert(id, value);
			super.queued();
		}
	}

	private boolean needsId(V value) {
		if (value.getVersion() == null)
			return true;
		return value.getVersion().intValue() != getIdVersion();
	}

}
