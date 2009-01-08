/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.helpers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openrdf.cursor.Cursor;
import org.openrdf.cursor.DelegatingCursor;
import org.openrdf.store.StoreException;

/**
 * An iteration extension that keeps a reference to the
 * {@link TrackingSailConnection} from which it originates and signals when it
 * is closed.
 * 
 * @author jeen
 * @author James Leigh
 * @author Arjohn Kampman
 */
class TrackingSailCursor<T> extends DelegatingCursor<T> {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private final TrackingSailConnection connection;

	private final Throwable creatorTrace;

	private boolean closed = true;

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
	public TrackingSailCursor(Cursor<? extends T> iter, TrackingSailConnection connection) {
		super(iter);
		this.connection = connection;
		this.creatorTrace = SailUtil.isDebugEnabled() ? new Throwable() : null;
	}

	@Override
	public T next()
		throws StoreException
	{
		T next = super.next();
		if (next == null) {
			// auto-close when exhausted
			close();
		}
		return next;
	}

	@Override
	public void close()
		throws StoreException
	{
		closed = true;
		super.close();
		connection.iterationClosed(this);
	}

	@Override
	protected void finalize()
		throws Throwable
	{
		if (!closed) {
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
