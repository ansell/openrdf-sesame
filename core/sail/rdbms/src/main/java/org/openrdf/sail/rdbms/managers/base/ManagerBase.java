/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.managers.base;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.SQLException;
import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openrdf.sail.rdbms.managers.helpers.BatchBlockingQueue;
import org.openrdf.sail.rdbms.schema.Batch;

public abstract class ManagerBase {
	public static int BATCH_SIZE = 8 * 1024;

	public static int MIN_QUEUE = 128;

	public static int MAX_QUEUE = 96 * 1024;

	Exception exc;

	private Logger logger = LoggerFactory.getLogger(ManagerBase.class);

	public final BlockingQueue<Batch> queue = new BatchBlockingQueue(MAX_QUEUE);

	private final Object working = new Object();

	private Thread thread;

	private int count;

	@SuppressWarnings("unchecked")
	public BlockingQueue<Batch> getQueue() {
		ClassLoader cl = getClass().getClassLoader();
		Class<?>[] classes = new Class[] { BlockingQueue.class };
		InvocationHandler h = new InvocationHandler() {

			public Object invoke(Object proxy, Method method, Object[] args)
				throws Throwable
			{
				Object result = method.invoke(queue, args);
				checkQueueSize();
				return result;
			}
		};
		Object proxy = Proxy.newProxyInstance(cl, classes, h);
		return (BlockingQueue<Batch>) proxy;
	}

	public void close()
		throws SQLException
	{
		flush();
		try {
			if (thread != null) {
				queue.put(Batch.CLOSED_SIGNAL);
				thread.join();
			}
		}
		catch (InterruptedException e) {
			logger.warn(e.toString(), e);
		}
		throwException();
	}

	public void flush()
		throws SQLException
	{
		throwException();
		synchronized (working) {
			throwException();
			for (Batch b = queue.poll(); isFlushable(b); b = queue.poll()) {
				flush(b);
			}
			count = 0;
		}
	}

	public void clear() {
		queue.clear();
	}

	protected void optimize()
		throws SQLException
	{
		// allow subclasses to optimise table
	}

	void checkQueueSize() {
		if (++count >= MIN_QUEUE && thread == null) {
			String name = getClass().getSimpleName() + "-flusher";
			thread = new Thread(new Runnable() {

				public void run() {
					try {
						lookupThread(working);
					}
					catch (Exception e) {
						exc = e;
						logger.error(e.toString(), e);
					}
				}
			}, name);
			thread.start();
		}
	}

	protected void flush(Batch batch)
		throws SQLException
	{
		batch.flush();
	}

	void lookupThread(Object working)
		throws SQLException, InterruptedException
	{
		String name = Thread.currentThread().getName();
		logger.debug("Starting helper thread {}", name);
		int notReadyCount = 0;
		for (Batch b = queue.take(); isFlushable(b); b = queue.take()) {
			if (b.isReady() || queue.size() <= notReadyCount) {
				synchronized (working) {
					flush(b);
				}
				optimize();
				notReadyCount = 0;
			}
			else {
				queue.add(b);
				notReadyCount++;
			}
		}
		logger.debug("Closing helper thread {}", name);
	}

	private boolean isFlushable(Batch batch) {
		return batch != null && batch != Batch.CLOSED_SIGNAL;
	}

	private void throwException()
		throws SQLException
	{
		if (exc instanceof SQLException) {
			SQLException e = (SQLException)exc;
			exc = null;
			throw e;
		}
		else if (exc instanceof RuntimeException) {
			RuntimeException e = (RuntimeException)exc;
			exc = null;
			throw e;
		}
	}

}
