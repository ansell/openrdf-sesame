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

import org.openrdf.StoreException;

/**
 * An iteration extension that keeps a reference to the
 * {@link TrackingSailConnection} from which it originates and signals when it
 * is closed.
 * 
 * @author jeen
 * @author James Leigh
 */
class TrackingSailIteration<T> extends IterationWrapper<T, StoreException> {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private TrackingSailConnection connection;

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
	public TrackingSailIteration(CloseableIteration<? extends T, StoreException> iter, TrackingSailConnection connection)
	{
		super(iter);
		this.connection = connection;

		if (SailUtil.isDebugEnabled()) {
			creatorTrace = new Throwable();
		}
	}

	@Override
	public boolean hasNext()
		throws StoreException
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
		throws StoreException
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
		throws StoreException
	{
		if (creatorTrace != null) {
			logger.warn("Forced closing of unclosed iteration that was created in:", creatorTrace);
		}

		close();
	}
}
