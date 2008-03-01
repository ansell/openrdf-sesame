/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.managers.base;

import java.sql.SQLException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.aduna.collections.LRUMap;

import org.openrdf.sail.rdbms.exceptions.RdbmsRuntimeException;
import org.openrdf.sail.rdbms.model.RdbmsValue;

public abstract class ValueManagerBase<K, V extends RdbmsValue> {
	public static final boolean STORE_VALUES = true;
	private Logger logger = LoggerFactory.getLogger(ValueManagerBase.class);
	Exception exc;
	private LRUMap<K, V> cache;
	public BlockingQueue<V> queue;
	private final Object working = new Object();
	private Thread thread;
	private V closedSignal;

	public void init() {
		cache = new LRUMap<K, V>(getBatchSize());
		queue = new ArrayBlockingQueue<V>(getBatchSize());
		thread = startThread(working);
		closedSignal = createClosedSignal();
	}

	public void close() throws SQLException {
		flush();
		try {
			queue.put(closedSignal);
			thread.join();
		}
		catch (InterruptedException e) {
			logger.warn(e.toString(), e);
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
		putInQueue(value);
		return value;
	}

	public void flush()
		throws SQLException
	{
		synchronized (working) {
			throwException();
			for (V taken = queue.poll(); taken != null; taken = queue.poll()) {
				insert(taken);
			}
			flushTable();
		}
	}

	public long getInternalId(V val) throws SQLException {
		if (needsId(val)) {
			putInQueue(val);
		}
		if (val.getInternalId() != null)
			return val.getInternalId();
		long id = getMissingId(val);
		val.setInternalId(id);
		return id;
	}

	protected abstract V createClosedSignal();

	protected abstract void flushTable() throws SQLException;

	protected abstract int getIdVersion();

	protected abstract int getBatchSize();

	protected abstract void insert(long internalId, V value)
			throws SQLException;

	protected abstract K key(V value);

	protected abstract long getMissingId(V value);

	protected abstract void optimize() throws SQLException;

	void lookupThread(Object working)
		throws SQLException, InterruptedException
	{
		String name = Thread.currentThread().getName();
		logger.debug("Starting helper thread {}", name);
		while (true) {
			V taken = queue.take();
			if (taken == closedSignal)
				break;
			synchronized (working) {
				insert(taken);
				if (queue.isEmpty()) {
					flushTable();
				}
			}
			optimize();
		}
		logger.debug("Closing helper thread {}", name);
	}

	private Thread startThread(final Object working) {
		String name = getClass().getSimpleName() + "-lookup";
		Thread lookupThread = new Thread(new Runnable() {
			public void run() {
				try {
					lookupThread(working);
				} catch (Exception e) {
					exc = e;
					logger.error(e.toString(), e);
				}
			}
		}, name);
		lookupThread.start();
		return lookupThread;
	}

	private void putInQueue(V value) {
		try {
			if (!value.isQueued()) {
				value.setQueued(true);
				queue.put(value);
			}
		}
		catch (InterruptedException e) {
			throw new RdbmsRuntimeException(e);
		}
	}

	private void insert(V value)
		throws SQLException
	{
		if (needsId(value)) {
			Long id = value.getInternalId();
			if (id == null) {
				id = getMissingId(value);
			}
			value.setInternalId(id);
			value.setVersion(getIdVersion());
			insert(id, value);
		}
		value.setQueued(false);
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
