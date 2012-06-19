/*
 * Copyright James Leigh (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.event.base;

import java.io.File;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.base.RepositoryWrapper;
import org.openrdf.repository.event.NotifyingRepository;
import org.openrdf.repository.event.NotifyingRepositoryConnection;
import org.openrdf.repository.event.RepositoryConnectionListener;
import org.openrdf.repository.event.RepositoryListener;

/**
 * This notifying decorator allows listeners to register with the repository or
 * connection and be notified when events occur.
 * 
 * @author James Leigh
 * @author Herko ter Horst
 * @author Arjohn Kampman
 * @see NotifyingRepositoryConnectionWrapper
 */
public class NotifyingRepositoryWrapper extends RepositoryWrapper implements NotifyingRepository {

	/*-----------*
	 * Variables *
	 *-----------*/

	private boolean activated;

	private boolean defaultReportDeltas = false;

	private Set<RepositoryListener> listeners = new CopyOnWriteArraySet<RepositoryListener>();

	private Set<RepositoryConnectionListener> conListeners = new CopyOnWriteArraySet<RepositoryConnectionListener>();

	/*--------------*
	 * Constructors *
	 *--------------*/

	public NotifyingRepositoryWrapper() {
		super();
	}

	public NotifyingRepositoryWrapper(Repository delegate) {
		super(delegate);
	}

	public NotifyingRepositoryWrapper(Repository delegate, boolean defaultReportDeltas) {
		this(delegate);
		setDefaultReportDeltas(defaultReportDeltas);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public boolean getDefaultReportDeltas() {
		return defaultReportDeltas;
	}

	public void setDefaultReportDeltas(boolean defaultReportDeltas) {
		this.defaultReportDeltas = defaultReportDeltas;
	}

	/**
	 * Registers a <tt>RepositoryListener</tt> that will receive notifications
	 * of operations that are performed on this repository.
	 */
	public void addRepositoryListener(RepositoryListener listener) {
		listeners.add(listener);
		activated = true;
	}

	/**
	 * Removes a registered <tt>RepositoryListener</tt> from this repository.
	 */
	public void removeRepositoryListener(RepositoryListener listener) {
		listeners.remove(listener);
		activated = !listeners.isEmpty();
	}

	/**
	 * Registers a <tt>RepositoryConnectionListener</tt> that will receive
	 * notifications of operations that are performed on any< connections that
	 * are created by this repository.
	 */
	public void addRepositoryConnectionListener(RepositoryConnectionListener listener) {
		conListeners.add(listener);
	}

	/**
	 * Removes a registered <tt>RepositoryConnectionListener</tt> from this
	 * repository.
	 */
	public void removeRepositoryConnectionListener(RepositoryConnectionListener listener) {
		conListeners.remove(listener);
	}

	@Override
	public NotifyingRepositoryConnection getConnection()
		throws RepositoryException
	{
		RepositoryConnection con = getDelegate().getConnection();

		if (activated) {
			for (RepositoryListener listener : listeners) {
				listener.getConnection(getDelegate(), con);
			}
		}
		NotifyingRepositoryConnection ncon = new NotifyingRepositoryConnectionWrapper(this,
				con, getDefaultReportDeltas());
		for (RepositoryConnectionListener l : conListeners) {
			ncon.addRepositoryConnectionListener(l);
		}

		return ncon;
	}

	@Override
	protected void initializeInternal()
		throws RepositoryException
	{
		super.initializeInternal();

		if (activated) {
			for (RepositoryListener listener : listeners) {
				listener.initialize(getDelegate());
			}
		}
	}

	@Override
	public void setDataDir(File dataDir)
	{
		super.setDataDir(dataDir);

		if (activated) {
			for (RepositoryListener listener : listeners) {
				listener.setDataDir(getDelegate(), dataDir);
			}
		}
	}

	@Override
	protected void shutDownInternal()
		throws RepositoryException
	{
		super.shutDownInternal();

		if (activated) {
			for (RepositoryListener listener : listeners) {
				listener.shutDown(getDelegate());
			}
		}
	}
}
