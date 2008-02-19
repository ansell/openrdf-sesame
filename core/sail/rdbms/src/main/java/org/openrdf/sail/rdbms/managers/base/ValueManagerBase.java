/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.managers.base;

import info.aduna.collections.LRUMap;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.openrdf.sail.rdbms.model.RdbmsValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ValueManagerBase<K, V extends RdbmsValue> {
	public static final long NIL_ID = 0;
	private Logger logger = LoggerFactory.getLogger(ValueManagerBase.class);
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
		if (exc != null) {
			throwException();
		}
		synchronized (needIds) {
			if (!needIds.isEmpty()) {
				loadIds(needIds);
				idsNoLongerNeeded(needIds);
				needIds.clear();
			}
		}
		insertNewValues();
		flushTable();
		synchronized (needIds) {
			needIds.notify();
		}
	}

	public long getInternalId(V val) throws SQLException {
		if (exc != null) {
			throwException();
		}
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
				lookup(value);
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

	protected abstract void flushTable() throws SQLException;

	protected abstract int getIdVersion();

	protected abstract int getBatchSize();

	protected abstract int getSelectChunkSize();

	protected abstract void insert(long internalId, V value)
			throws SQLException;

	protected abstract K key(V value);

	protected abstract void loadIds(final Map<K, V> needIds)
			throws SQLException;

	protected abstract long nextId(V value);

	protected abstract void optimize() throws SQLException;

	void lookupThread() throws SQLException, InterruptedException {
		logger.debug("Starting helper thread {}", Thread.currentThread()
				.getName());
		int chunkSize = getSelectChunkSize();
		int batchSize = getBatchSize();
		Map<K, V> values = new HashMap<K, V>(chunkSize * 2);
		while (!closed) {
			int size = 0;
			while (!closed && size < chunkSize) {
				synchronized (needIds) {
					needIds.wait();
					size = needIds.size();
					Iterator<Entry<K, V>> iter = needIds.entrySet().iterator();
					for (int i = 0; i < batchSize && iter.hasNext(); i++) {
						Entry<K, V> next = iter.next();
						values.put(next.getKey(), next.getValue());
					}
				}
				if (size == 0) {
					optimize();
				}
			}
			if (!closed && !values.isEmpty()) {
				loadIds(values);
				synchronized (needIds) {
					idsNoLongerNeeded(values);
					for (K key : values.keySet()) {
						needIds.remove(key);
					}
					values.clear();
				}
				insertNewValues();
				flushTable();
				optimize();
			}
		}
		logger.debug("Closing helper thread {}", Thread.currentThread()
				.getName());
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

	private void lookup(V value) throws SQLException {
		int chunkSize = getSelectChunkSize();
		Map<K, V> values = new HashMap(chunkSize);
		values.put(key(value), value); // ensure this value gets in
		synchronized (needIds) {
			// lookup a few more at the same time
			Iterator<Entry<K, V>> iter = needIds.entrySet().iterator();
			for (int i = 1; i < chunkSize && iter.hasNext(); i++) {
				Entry<K, V> next = iter.next();
				values.put(next.getKey(), next.getValue());
			}
		}
		loadIds(values);
		synchronized (needIds) {
			idsNoLongerNeeded(values);
			for (K key : values.keySet()) {
				needIds.remove(key);
			}
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
