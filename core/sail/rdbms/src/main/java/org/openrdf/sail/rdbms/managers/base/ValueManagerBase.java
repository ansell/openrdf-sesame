/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.managers.base;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.Lock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.aduna.collections.LRUMap;

import org.openrdf.sail.rdbms.model.RdbmsValue;

public abstract class ValueManagerBase<K, V extends RdbmsValue> {
	public static final boolean STORE_VALUES = true;
	private Logger logger = LoggerFactory.getLogger(ValueManagerBase.class);
	boolean closed;
	Exception exc;
	private LRUMap<K, V> cache = new LRUMap<K, V>(256);
	private Thread lookupThread;
	public Map<K, V> needIds = new HashMap<K, V>();
	public Map<K, V> newValues = new HashMap<K, V>();
	private Lock idLock;

	public ValueManagerBase(Lock idLock) {
		super();
		this.idLock = idLock;
	}

	public V cache(V value) throws SQLException {
		if (!STORE_VALUES)
			return value;
		if (!needsId(value))
			return value;
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
		synchronized (cache) {
			cache.put(key, value);
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
		if (!STORE_VALUES)
			return null;
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
				idLock.lock();
				try {
					loadIds(needIds);
					idsNoLongerNeeded(needIds);
					needIds.clear();
				} finally {
					idLock.unlock();
				}
			}
		}
		insertNewValues(0);
		synchronized (needIds) {
			needIds.notify();
		}
	}

	public long getInternalId(V val) throws SQLException {
		if (val.getInternalId() != null)
			return val.getInternalId();
		long id = getMissingId(val);
		val.setInternalId(id);
		return id;
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
		}, getClass().getSimpleName() + "-lookup");
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

	protected abstract long getMissingId(V value);

	protected abstract void optimize() throws SQLException;

	void lookupThread() throws SQLException, InterruptedException {
		logger.debug("Starting helper thread {}", Thread.currentThread()
				.getName());
		int batchSize = getBatchSize();
		Map<K, V> values = new HashMap<K, V>(batchSize);
		while (!closed) {
			int size;
			synchronized (needIds) {
				needIds.wait();
				Iterator<Entry<K, V>> iter = needIds.entrySet().iterator();
				for (int i = 0; i < batchSize && iter.hasNext(); i++) {
					Entry<K, V> next = iter.next();
					values.put(next.getKey(), next.getValue());
				}
				size = needIds.size();
			}
			if (!values.isEmpty()) {
				idLock.lock();
				try {
					loadIds(values);
					synchronized (needIds) {
						idsNoLongerNeeded(values);
						for (K key : values.keySet()) {
							needIds.remove(key);
						}
						values.clear();
						size = needIds.size();
					}
				} finally {
					idLock.unlock();
				}
			}
			if (insertNewValues(size / 2)) {
				optimize();
			}
		}
		logger.debug("Closing helper thread {}", Thread.currentThread()
				.getName());
	}

	private void idsNoLongerNeeded(Map<K, V> needIds) throws SQLException {
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
							value.setInternalId(getInternalId(value));
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

	private boolean insertNewValues(int minSize) throws SQLException {
		synchronized (newValues) {
			if (newValues.size() <= minSize)
				return false;
			for (V resource : newValues.values()) {
				insert(resource.getInternalId(), resource);
			}
			newValues.clear();
		}
		flushTable();
		return true;
	}

	private boolean needsId(V value) {
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
