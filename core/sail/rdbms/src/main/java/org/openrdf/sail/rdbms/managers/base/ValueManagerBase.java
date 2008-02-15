/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.managers.base;

import info.aduna.collections.LRUMap;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.openrdf.sail.rdbms.model.RdbmsValue;

public abstract class ValueManagerBase<K, V extends RdbmsValue> {
	public static final long NIL_ID = 0;
	boolean closed;
	Exception exc;
	private LRUMap<K, V> cache = new LRUMap(256);
	private String label;
	private Thread lookupThread;
	private Map<K, V> needIds = new HashMap();
	private Map<K, V> newValues = new HashMap();

	public ValueManagerBase(String label) {
		super();
		this.label = label;
	}

	public V cache(V value) throws SQLException {
		K key = key(value);
		synchronized (needIds) {
			if (needIds.containsKey(key))
				return needIds.get(key);
			synchronized (newValues) {
				if (newValues.containsKey(key))
					return newValues.get(key);
			}
			needIds.put(key, value);
			needIds.notify();
		}
		return value;
	}

	public void close() {
		closed = true;
		synchronized (needIds) {
			needIds.notify();
		}
	}

	public V findInCache(K key) {
		synchronized (cache) {
			if (cache.containsKey(key))
				return cache.get(key);
		}
		synchronized (newValues) {
			if (newValues.containsKey(key))
				return newValues.get(key);
		}
		return null;
	}

	public void flush() throws SQLException {
		lookupIds();
		insertNewValues();
	}

	public long getInternalId(V val) throws SQLException {
		if (val == null)
			return NIL_ID;
		K key = key(val);
		V value = val;
		if (needsId(value)) {
			synchronized (needIds) {
				if (needsId(value)) {
					if (needIds.containsKey(key)) {
						value = needIds.get(key);
					} else {
						needIds.put(key, value);
						needIds.notify();
					}
				}
			}
			if (needsId(value)) {
				lookupIds();
			}
		}
		return value.getInternalId();
	}

	public void init() {
		lookupThread = new Thread(new Runnable() {
			public void run() {
				try {
					lookupThread();
				} catch (Exception e) {
					exc = e;
				}
			}
		}, label + "-lookup");
		lookupThread.start();
	}

	protected abstract int getIdVersion();

	protected abstract int getSelectChunkSize();

	protected abstract void insert(long internalId, V value)
			throws SQLException;

	protected abstract K key(V value);

	protected abstract void loadIds(final Map<K, V> needIds)
			throws SQLException;

	protected abstract long nextId(V value);

	void lookupThread() throws SQLException, InterruptedException {
		Map<K, V> values;
		values = new HashMap<K, V>();
		while (!closed) {
			synchronized (needIds) {
				while (!closed && needIds.size() < getSelectChunkSize()) {
					needIds.wait();
				}
				values.putAll(needIds);
			}
			if (!closed) {
				loadIds(values);
				synchronized (needIds) {
					idsNoLongerNeeded(values);
					for (K key : values.keySet()) {
						needIds.remove(key);
					}
					values.clear();
				}
				insertNewValues();
			}
		}
	}

	private void idsNoLongerNeeded(Map<K, V> needIds) {
		synchronized (newValues) {
			synchronized (cache) {
				for (V value : needIds.values()) {
					K key = key(value);
					if (needsId(value)) {
						if (newValues.containsKey(key)) {
							V res = newValues.get(key);
							value.setInternalId(res.getInternalId());
							value.setVersion(res.getVersion());
						} else {
							value.setInternalId(nextId(value));
							value.setVersion(getIdVersion());
							newValues.put(key, value);
						}
					} else {
						cache.put(key, value);
					}
				}
			}
		}
	}

	private void insertNewValues() throws SQLException {
		synchronized (newValues) {
			if (newValues.isEmpty())
				return;
			for (V resource : newValues.values()) {
				insert(resource.getInternalId(), resource);
			}
			newValues.clear();
		}
	}

	private void lookupIds() throws SQLException {
		if (exc != null) {
			throwException();
		}
		synchronized (needIds) {
			if (needIds.isEmpty())
				return;
			loadIds(needIds);
			idsNoLongerNeeded(needIds);
			needIds.clear();
		}
	}

	private boolean needsId(V value) {
		if (value.getInternalId() == null)
			return true;
		if (value.getVersion() == null)
			return true;
		return value.getVersion().intValue() != getIdVersion();
	}

	private void throwException() throws SQLException {
		if (exc instanceof SQLException) {
			SQLException e = (SQLException) exc;
			exc = null;
			throw e;
		} else if (exc instanceof RuntimeException) {
			RuntimeException e = (RuntimeException) exc;
			exc = null;
			throw e;
		}
	}

}
