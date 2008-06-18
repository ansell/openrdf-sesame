/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.helpers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.IterationWrapper;

/**
 * An iteration extension that keeps a reference to the SailConnectionBase from
 * which it originates and signals when it is closed.
 * 
 * @author jeen
 */
class SailBaseIteration<T, E extends Exception> extends IterationWrapper<T, E> {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private SailConnectionBase connection;

	private Throwable creatorTrace;

	/**
	 * Creates a new memory-store specific iteration object.
	 * 
	 * @param lock
	 *        a query lock
	 * @param iter
	 *        the wrapped iteration over sail objects.
	 * @param connection
	 *        the connection from which this iteration originates.
	 */
	public SailBaseIteration(CloseableIteration<? extends T, ? extends E> iter, SailConnectionBase connection)
	{
		super(iter);
		this.connection = connection;

		if (SailBase.debugEnabled()) {
			creatorTrace = new Throwable();
		}
	}

	@Override
	public boolean hasNext()
		throws E
	{
		if (super.hasNext()) {
			return true;
		}
		else {
			// auto-close when exhausted
			close();
			return false;
		}
	}

	@Override
	protected void handleClose()
		throws E
	{
		super.handleClose();
			connection.iterationClosed(this);
		}

	@Override
	protected void finalize()
		throws Throwable
	{
		if (!isClosed()) {
			forceClose();
		}

		super.finalize();
	}

	protected void forceClose()
		throws E
	{
		if (creatorTrace != null) {
			logger.warn("Forced closing of unclosed iteration that was created in:", creatorTrace);
		}

		close();
	}
}
