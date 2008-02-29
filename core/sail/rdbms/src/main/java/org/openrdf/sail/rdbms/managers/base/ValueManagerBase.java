/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.managers.base;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.aduna.collections.LRUMap;

import org.openrdf.sail.rdbms.model.RdbmsValue;

public abstract class ValueManagerBase<K, V extends RdbmsValue> {
	public static final boolean STORE_VALUES = true;
	public static int MAX_QUEUE = Integer.MAX_VALUE;//64;
	private Logger logger = LoggerFactory.getLogger(ValueManagerBase.class);
	boolean closed;
	Exception exc;
	private LRUMap<K, V> cache;
	public LinkedList<Map<K, V>> queue = new LinkedList<Map<K, V>>();
	private List<Object> threadFlush = new CopyOnWriteArrayList<Object>();
	private List<Object> threadClose = new CopyOnWriteArrayList<Object>();

	public void close() throws SQLException {
		flush();
		closed = true;
		synchronized (queue) {
			queue.notify();
		}
		for (Object lock : threadClose) {
			synchronized (lock) {
				// wait for thread
			}
		}
		throwException();
	}

	public V findInCache(K key) {
		if (!STORE_VALUES)
			return null;
		synchronized (cache) {
			if (cache.containsKey(key))
				return cache.get(key);
		}
		synchronized (queue) {
			for (Map<K,V> map : queue) {
				if (map.containsKey(key))
					return map.get(key);
			}
		}
		return null;
	}

	public V cache(V value) throws SQLException {
		if (!STORE_VALUES)
			return value;
		if (!needsId(value))
			return value;
		synchronized (cache) {
			cache.put(key(value), value);
		}
		lookup(value);
		return value;
	}

	public void flush()
		throws SQLException
	{
		throwException();
		synchronized (queue) {
			if (!queue.isEmpty()) {
				while (!queue.isEmpty()) {
					insert(queue.pop());
				}
			}
			queue.notify();
		}
		for (Object lock : threadFlush) {
			synchronized (lock) {
				// wait for thread
			}
		}
		callFlushTable();
	}

	public long getInternalId(V val) throws SQLException {
		if (needsId(val)) {
			lookup(val);
		}
		if (val.getInternalId() != null)
			return val.getInternalId();
		long id = getMissingId(val);
		val.setInternalId(id);
		return id;
	}

	public void init() {
		cache = new LRUMap<K, V>(getBatchSize() * 2);
		startThread();
	}

	protected abstract void flushTable() throws SQLException;

	protected abstract int getIdVersion();

	protected abstract int getBatchSize();

	protected abstract void insert(long internalId, V value)
			throws SQLException;

	protected abstract K key(V value);

	protected abstract long getMissingId(V value);

	protected abstract void optimize() throws SQLException;

	void lookupThread(Object flushLock, Object closeLock)
		throws SQLException, InterruptedException
	{
		logger.debug("Starting helper thread {}", Thread.currentThread().getName());
		while (!closed) {
			Map<K, V> values = null;
			synchronized (queue) {
				if (queue.isEmpty()) {
					queue.wait();
				}
				if (closed)
					return;
			}
			synchronized (flushLock) {
				synchronized (queue) {
					if (!queue.isEmpty()) {
						values = queue.pop();
					}
				}
				if (values != null) {
					insert(values);
				}
				int size;
				synchronized (queue) {
					size = queue.size();
				}
				if (size < 2) {
					callFlushTable();
				}
			}
			synchronized (closeLock) {
				if (!closed && values != null) {
					values = null;
					optimize();
				}
			}
		}
		logger.debug("Closing helper thread {}", Thread.currentThread().getName());
	}

	private void startThread() {
		final Object flushLock = new Object();
		final Object closeLock = new Object();
		threadFlush.add(flushLock);
		threadClose.add(closeLock);
		String name = getClass().getSimpleName() + "-lookup-" + threadFlush.size();
		Thread lookupThread = new Thread(new Runnable() {
			public void run() {
				try {
					lookupThread(flushLock, closeLock);
				} catch (Exception e) {
					exc = e;
					logger.error(e.toString(), e);
				}
			}
		}, name);
		lookupThread.start();
	}

	private void lookup(V value) throws SQLException {
		Map<K, V> values = null;
		synchronized (queue) {
			if (queue.isEmpty() || queue.getLast().size() >= getBatchSize()) {
				queue.add(new HashMap<K, V>(getBatchSize()));
			}
			queue.getLast().put(key(value), value);
			queue.notify();
			if (queue.size() > MAX_QUEUE) {
				values = queue.pop();
			}
		}
		if (values != null) {
			insert(values);
		}
	}

	private void insert(Map<K, V> values)
		throws SQLException
	{
		for (V value : values.values()) {
			insert(getInternalId(value), value);
			value.setVersion(getIdVersion());
		}
	}

	private void callFlushTable()
		throws SQLException
	{
		flushTable();
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
