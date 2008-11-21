/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.helpers;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.Cursor;
import org.openrdf.query.algebra.QueryModel;
import org.openrdf.sail.SailConnection;
import org.openrdf.store.StoreException;

/**
 * Tracks SailConnection iterations and verifies that the connection is open.
 * 
 * @author Arjohn Kampman
 * @author jeen
 * @author James Leigh
 */
public class TrackingSailConnection extends SailConnectionWrapper {

	protected final Logger logger = LoggerFactory.getLogger(TrackingSailConnection.class);

	/*-----------*
	 * Variables *
	 *-----------*/

	private boolean isOpen;

	private boolean txnActive;

	// FIXME: use weak references here?
	private List<TrackingSailCursor<?>> activeIterations = Collections.synchronizedList(new LinkedList<TrackingSailCursor<?>>());

	/*
	 * Stores a stack trace that indicates where this connection as created if
	 * debugging is enabled.
	 */
	private Throwable creatorTrace;

	private SailConnectionTracker tracker;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public TrackingSailConnection(SailConnection con, SailConnectionTracker tracker) {
		super(con);
		isOpen = true;
		txnActive = false;
		this.tracker = tracker;
		if (SailUtil.isDebugEnabled()) {
			creatorTrace = new Throwable();
		}
	}

	/*---------*
	 * Methods *
	 *---------*/

	@Override
	public final boolean isOpen()
		throws StoreException
	{
		return isOpen;
	}

	protected void verifyIsOpen()
		throws StoreException
	{
		if (!isOpen) {
			throw new IllegalStateException("Connection has been closed");
		}
	}

	@Override
	public final void close()
		throws StoreException
	{
		if (isOpen) {
			try {
				while (true) {
					TrackingSailCursor<?> ci = null;

					synchronized (activeIterations) {
						if (activeIterations.isEmpty()) {
							break;
						}
						else {
							ci = activeIterations.remove(0);
						}
					}

					try {
						ci.forceClose();
					}
					catch (StoreException e) {
						throw e;
					}
				}

				assert activeIterations.isEmpty();

				if (txnActive) {
					logger.warn("Rolling back transaction due to connection close", new Throwable());
					try {
						// Use internal method to avoid deadlock: the public
						// rollback method will try to obtain a connection lock
						super.rollback();
					}
					finally {
						txnActive = false;
					}
				}

				super.close();
			}
			finally {
				isOpen = false;
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
					logger.warn("Closing connection due to garbage collection, connection was create in:",
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
		verifyIsOpen();
		return registerCursor(super.evaluate(query, bindings, includeInferred));
	}

	@Override
	public final Cursor<? extends Resource> getContextIDs()
		throws StoreException
	{
		verifyIsOpen();
		return registerCursor(super.getContextIDs());
	}

	@Override
	public final Cursor<? extends Statement> getStatements(Resource subj, URI pred, Value obj,
			boolean includeInferred, Resource... contexts)
		throws StoreException
	{
		verifyIsOpen();
		return registerCursor(super.getStatements(subj, pred, obj, includeInferred, contexts));
	}

	@Override
	public final long size(Resource subj, URI pred, Value obj, boolean includeInferred, Resource... contexts)
		throws StoreException
	{
		verifyIsOpen();
		return super.size(subj, pred, obj, includeInferred, contexts);
	}

	protected void autoStartTransaction()
		throws StoreException
	{
		begin();
	}

	@Override
	public void begin()
		throws StoreException
	{
		if (!txnActive) {
			super.begin();
			txnActive = true;
		}
	}

	@Override
	public final void commit()
		throws StoreException
	{
		verifyIsOpen();
		if (txnActive) {
			super.commit();
			txnActive = false;
		}
	}

	@Override
	public final void rollback()
		throws StoreException
	{
		verifyIsOpen();
		if (txnActive) {
			try {
				super.rollback();
			}
			finally {
				txnActive = false;
			}
		}
	}

	@Override
	public final void addStatement(Resource subj, URI pred, Value obj, Resource... contexts)
		throws StoreException
	{
		verifyIsOpen();
		autoStartTransaction();
		super.addStatement(subj, pred, obj, contexts);
	}

	@Override
	public final void removeStatements(Resource subj, URI pred, Value obj, Resource... contexts)
		throws StoreException
	{
		verifyIsOpen();
		autoStartTransaction();
		super.removeStatements(subj, pred, obj, contexts);
	}

	@Override
	public final Cursor<? extends Namespace> getNamespaces()
		throws StoreException
	{
		verifyIsOpen();
		return registerCursor(super.getNamespaces());
	}

	@Override
	public final String getNamespace(String prefix)
		throws StoreException
	{
		verifyIsOpen();
		return super.getNamespace(prefix);
	}

	@Override
	public final void setNamespace(String prefix, String name)
		throws StoreException
	{
		verifyIsOpen();
		autoStartTransaction();
		super.setNamespace(prefix, name);
	}

	@Override
	public final void removeNamespace(String prefix)
		throws StoreException
	{
		verifyIsOpen();
		autoStartTransaction();
		super.removeNamespace(prefix);
	}

	@Override
	public final void clearNamespaces()
		throws StoreException
	{
		verifyIsOpen();
		autoStartTransaction();
		super.clearNamespaces();
	}

	/**
	 * Registers an iteration as active by wrapping it in a
	 * {@link TrackingSailIteration} object and adding it to the list of active
	 * iterations.
	 */
	protected <T> Cursor<T> registerCursor(Cursor<T> iter) {
		TrackingSailCursor<T> result = new TrackingSailCursor<T>(iter, this);
		activeIterations.add(result);
		return result;
	}

	/**
	 * Called by {@link TrackingSailIteration} to indicate that it has been
	 * closed.
	 */
	void iterationClosed(TrackingSailCursor<?> iter) {
		activeIterations.remove(iter);
	}
}
