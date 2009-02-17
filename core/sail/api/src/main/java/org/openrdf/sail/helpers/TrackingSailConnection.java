/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.helpers;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openrdf.cursor.Cursor;
import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.algebra.QueryModel;
import org.openrdf.sail.SailConnection;
import org.openrdf.store.StoreException;

/**
 * Keeps track of open cursors.
 * 
 * @author Arjohn Kampman
 * @author jeen
 * @author James Leigh
 */
public class TrackingSailConnection extends SailConnectionWrapper {

	/*-----------*
	 * Variables *
	 *-----------*/

	protected final Logger logger = LoggerFactory.getLogger(TrackingSailConnection.class);

	// FIXME: use weak references here?
	private final List<TrackingSailCursor<?>> trackedCursors = Collections.synchronizedList(new LinkedList<TrackingSailCursor<?>>());

	/*
	 * Stores a stack trace that indicates where this connection as created if
	 * debugging is enabled.
	 */
	private final Throwable creatorTrace;

	private final SailConnectionTracker tracker;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public TrackingSailConnection(SailConnection con, SailConnectionTracker tracker) {
		super(con);
		this.tracker = tracker;
		this.creatorTrace = SailUtil.isDebugEnabled() ? new Throwable() : null;
	}

	/*---------*
	 * Methods *
	 *---------*/

	@Override
	public final void close()
		throws StoreException
	{
		if (isOpen()) {
			try {
				while (true) {
					TrackingSailCursor<?> ci = null;

					synchronized (trackedCursors) {
						if (trackedCursors.isEmpty()) {
							break;
						}
						else {
							ci = trackedCursors.remove(0);
						}
					}

					try {
						ci.forceClose();
					}
					catch (StoreException e) {
						throw e;
					}
				}

				assert trackedCursors.isEmpty();

				if (!isAutoCommit()) {
					logger.warn("Rolling back transaction due to connection close", new Throwable());
				}

				super.close();
			}
			finally {
				tracker.closed(this);
			}
		}
	}

	@Override
	protected void finalize()
		throws Throwable
	{
		try {
			if (isOpen()) {
				if (creatorTrace != null) {
					logger.warn("Closing connection due to garbage collection, connection was created in: ",
							creatorTrace);
				}
				close();
			}
		}
		finally {
			super.finalize();
		}
	}

	@Override
	public final Cursor<? extends BindingSet> evaluate(QueryModel query, BindingSet bindings,
			boolean includeInferred)
		throws StoreException
	{
		return trackCursor(super.evaluate(query, bindings, includeInferred));
	}

	@Override
	public final Cursor<? extends Resource> getContextIDs()
		throws StoreException
	{
		return trackCursor(super.getContextIDs());
	}

	@Override
	public final Cursor<? extends Statement> getStatements(Resource subj, URI pred, Value obj,
			boolean includeInferred, Resource... contexts)
		throws StoreException
	{
		return trackCursor(super.getStatements(subj, pred, obj, includeInferred, contexts));
	}

	@Override
	public final Cursor<? extends Namespace> getNamespaces()
		throws StoreException
	{
		return trackCursor(super.getNamespaces());
	}

	/**
	 * Tracks a cursor by wrapping it in a {@link TrackingSailCursor} object and
	 * adding it to the list of {@link #trackedCursors tracked cursors}.
	 */
	protected <T> Cursor<T> trackCursor(Cursor<T> iter) {
		TrackingSailCursor<T> result = new TrackingSailCursor<T>(iter, this);
		trackedCursors.add(result);
		return result;
	}

	/**
	 * Called by {@link TrackingSailCursor} to indicate that it has been closed.
	 */
	void cursorClosed(TrackingSailCursor<?> iter) {
		trackedCursors.remove(iter);
	}
}
