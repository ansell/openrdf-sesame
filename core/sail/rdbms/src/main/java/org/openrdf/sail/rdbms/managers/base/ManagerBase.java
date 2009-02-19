/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.managers.base;

import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openrdf.sail.rdbms.managers.helpers.BatchBlockingQueue;
import org.openrdf.sail.rdbms.schema.Batch;

public abstract class ManagerBase {

	public static int BATCH_SIZE = 8 * 1024;

	public static int MIN_QUEUE = 128;

	public static int MAX_QUEUE = 96 * 1024;

	private static final boolean USE_THREAD = true;

	volatile Exception exc;

	Logger logger = LoggerFactory.getLogger(ManagerBase.class);

	public final BatchBlockingQueue queue = new BatchBlockingQueue(MAX_QUEUE, this);

	final Object working = new Object();

	private volatile Batch wb;

	private Thread thread;

	public BatchBlockingQueue getQueue() {
		return queue;
	}

	public void close()
		throws SQLException
	{
		try {
			flush();
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
		throws SQLException, InterruptedException
	{
		throwException();
		synchronized (working) {
			throwException();
			for (Batch b = queue.poll(); isFlushable(b); b = queue.poll()) {
				flush(b);
			}
			if (wb != null) {
				flush(wb);
				wb = null;
			}
		}
	}

	public void clear() {
		queue.clear();
	}

	public void queueIncreased() {
		if (thread == null && queue.size() >= MIN_QUEUE && USE_THREAD) {
			String name = getClass().getSimpleName() + "-flusher";
			thread = new Thread(new Runnable() {

				public void run() {
					try {
						insertThread(working);
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

	protected void optimize()
		throws SQLException
	{
		// allow subclasses to optimise table
	}

	protected void flush(Batch batch)
		throws SQLException
	{
		batch.flush();
	}

	void insertThread(Object working)
		throws SQLException, InterruptedException
	{
		String name = Thread.currentThread().getName();
		logger.debug("Starting helper thread {}", name);
		int notReadyCount = 0;
		for (wb = queue.take(); isFlushable(wb); wb = queue.take()) {
			if (wb.isReady() || queue.size() <= notReadyCount) {
				synchronized (working) {
					if (wb != null) {
						flush(wb);
						wb = null;
					}
				}
				optimize();
				notReadyCount = 0;
			}
			else {
				queue.add(wb);
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
