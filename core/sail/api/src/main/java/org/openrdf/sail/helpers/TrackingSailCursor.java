/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007-2009.
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
 * An cursor that keeps a reference to the
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
	 * Creates a new tracking sail cursor.
	 * 
	 * @param iter
	 *        the wrapped curosr over sail objects.
	 * @param connection
	 *        the connection from which this cursor originates.
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
		connection.cursorClosed(this);
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
			logger.warn("Forced closing of unclosed cursor that was created in:", creatorTrace);
		}

		close();
	}
}
