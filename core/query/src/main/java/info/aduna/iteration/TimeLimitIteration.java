/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008-2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package info.aduna.iteration;

import java.lang.ref.WeakReference;
import java.util.NoSuchElementException;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Arjohn Kampman
 */
public abstract class TimeLimitIteration<E, X extends Exception> extends IterationWrapper<E, X> {

	private static Timer timer = null;

	private static synchronized Timer getTimer() {
		if (timer == null) {
			timer = new Timer("TimeLimitIteration", true);
		}
		return timer;
	}

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private final WeakReference<TimerTask> taskReference;
	
	private volatile boolean isInterrupted = false;

	public TimeLimitIteration(Iteration<? extends E, ? extends X> iter, long timeLimit) {
		super(iter);

		assert timeLimit > 0 : "time limit must be a positive number, is: " + timeLimit;

		TimerTask interruptTask = new TimerTask() {

			@Override
			public void run() {
				interrupt();
			}
		};
		
		taskReference = new WeakReference<TimerTask>(interruptTask);

		getTimer().schedule(interruptTask, timeLimit);
	}

	@Override
	public boolean hasNext()
		throws X
	{
		checkInterrupted();
		boolean result = super.hasNext();
		checkInterrupted();
		return result;
	}

	@Override
	public E next()
		throws X
	{
		checkInterrupted();
		try {
			return super.next();
		}
		catch (NoSuchElementException e) {
			checkInterrupted();
			throw e;
		}
	}

	@Override
	public void remove()
		throws X
	{
		checkInterrupted();
		super.remove();
	}

	@Override
	protected void handleClose()
		throws X
	{
		taskReference.get().cancel();
		super.handleClose();
	}

	private final void checkInterrupted()
		throws X
	{
		if (isInterrupted) {
			throwInterruptedException();
		}
	}

	protected abstract void throwInterruptedException()
		throws X;

	private void interrupt() {
		if (!isClosed()) {
			isInterrupted = true;

			try {
				close();
			}
			catch (Exception e) {
				logger.warn("Failed to close iteration", e);
			}
		}
	}
}
