/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.cursor;

import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openrdf.store.StoreException;

/**
 * @author Arjohn Kampman
 */
public abstract class TimeLimitCursor<E> extends DelegatingCursor<E> {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private Timer timer;

	private boolean isInterrupted;

	private boolean closed;

	public TimeLimitCursor(Cursor<? extends E> iter, long timeLimit) {
		super(iter);
		assert timeLimit > 0 : "time limit must be a positive number, is: " + timeLimit;

		startTimer(timeLimit);
	}

	@Override
	public E next()
		throws StoreException
	{
		checkInterrupted();
		E next = super.next();
		checkInterrupted();
		return next;
	}

	@Override
	public void close()
		throws StoreException
	{
		closed = true;
		timer.cancel();
		super.close();
	}

	private final void checkInterrupted()
		throws StoreException
	{
		if (isInterrupted) {
			throwInterruptedException();
		}
	}

	protected abstract void throwInterruptedException()
		throws StoreException;

	private void startTimer(long timeLimit) {
		TimerTask tt = new TimerTask() {

			@Override
			public void run() {
				interrupt();
			}
		};

		timer = new Timer("TimeLimitCursor", true);
		timer.schedule(tt, timeLimit);
	}

	void interrupt() {
		if (!closed) {
			isInterrupted = true;

			try {
				close();
			}
			catch (Exception e) {
				logger.warn("Failed to close cursor", e);
			}
		}
	}

}
