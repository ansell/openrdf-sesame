/*
 * Copyright James Leigh (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.event.base;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.base.RepositoryConnectionWrapper;
import org.openrdf.repository.event.NotifyingRepositoryConnection;
import org.openrdf.repository.event.RepositoryConnectionListener;
import org.openrdf.result.ModelResult;
import org.openrdf.result.NamespaceResult;
import org.openrdf.store.StoreException;

/**
 * This broadcaster is used by the RepositoryBroadcaster to wrap the delegate
 * repository connection. There are two types of listeners for the repository
 * connection, {@link RepositoryConnectionStateListener} and
 * {@link RepositoryConnectionListener}. Listeners are notified of changes
 * after they have occurred.
 * 
 * @author James Leigh
 * @author Herko ter Horst
 */
public class NotifyingRepositoryConnectionWrapper extends RepositoryConnectionWrapper implements
		NotifyingRepositoryConnection
{

	/*-----------*
	 * Variables *
	 *-----------*/

	private boolean activated = false;

	private boolean reportDeltas = false;

	private Set<RepositoryConnectionListener> listeners = new CopyOnWriteArraySet<RepositoryConnectionListener>();

	/*--------------*
	 * Constructors *
	 *--------------*/

	public NotifyingRepositoryConnectionWrapper(Repository repository, RepositoryConnection connection) {
		super(repository, connection);
	}

	public NotifyingRepositoryConnectionWrapper(Repository repository, RepositoryConnection connection,
			boolean reportDeltas)
	{
		this(repository, connection);
		setReportDeltas(reportDeltas);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public boolean reportDeltas() {
		return reportDeltas;
	}

	public void setReportDeltas(boolean reportDeltas) {
		this.reportDeltas = reportDeltas;
	}

	/**
	 * Registers a <tt>RepositoryConnectionListener</tt> that will receive
	 * notifications of operations that are performed on this connection.
	 */
	public void addRepositoryConnectionListener(RepositoryConnectionListener listener) {
		listeners.add(listener);
		activated = true;
	}

	/**
	 * Removes a registered <tt>RepositoryConnectionListener</tt> from this
	 * connection.
	 */
	public void removeRepositoryConnectionListener(RepositoryConnectionListener listener) {
		listeners.remove(listener);
		activated = !listeners.isEmpty();
	}

	@Override
	protected boolean isDelegatingAdd()
	{
		return !activated;
	}

	@Override
	protected boolean isDelegatingRemove()
	{
		return !activated;
	}

	@Override
	public void add(Resource subject, URI predicate, Value object, Resource... contexts)
		throws StoreException
	{
		boolean reportEvent = activated;

		if (reportEvent && reportDeltas()) {
			// Only report if the stament is not present yet
			reportEvent = !getDelegate().hasMatch(subject, predicate, object, false, contexts);
		}

		getDelegate().add(subject, predicate, object, contexts);

		if (reportEvent) {
			for (RepositoryConnectionListener listener : listeners) {
				listener.add(this, subject, predicate, object, contexts);
			}
		}
	}

	@Override
	public void clear(Resource... contexts)
		throws StoreException
	{
		if (activated && reportDeltas()) {
			removeMatch(null, null, null, contexts);
		} else if (activated) {
			getDelegate().clear(contexts);
			for (RepositoryConnectionListener listener : listeners) {
				listener.clear(this, contexts);
			}
		} else {
			getDelegate().clear(contexts);
		}
	}

	@Override
	public void close()
		throws StoreException
	{
		super.close();

		if (activated) {
			for (RepositoryConnectionListener listener : listeners) {
				listener.close(this);
			}
		}
	}

	@Override
	public void commit()
		throws StoreException
	{
		getDelegate().commit();

		if (activated) {
			for (RepositoryConnectionListener listener : listeners) {
				listener.commit(this);
			}
		}
	}

	@Override
	public void removeMatch(Resource subj, URI pred, Value obj, Resource... ctx)
		throws StoreException
	{
		if (activated && reportDeltas()) {
			ModelResult stmts;
			stmts = getDelegate().match(subj, pred, obj, false, ctx);
			List<Statement> list = new ArrayList<Statement>();
			try {
				while (stmts.hasNext()) {
					list.add(stmts.next());
				}
			} finally {
				stmts.close();
			}
			getDelegate().removeMatch(subj, pred, obj, ctx);
			for (RepositoryConnectionListener listener : listeners) {
				for (Statement stmt : list) {
					Resource s = stmt.getSubject();
					URI p = stmt.getPredicate();
					Value o = stmt.getObject();
					Resource c = stmt.getContext();
					listener.remove(this, s, p, o, c);
				}
			}
		} else if (activated) {
			getDelegate().removeMatch(subj, pred, obj, ctx);
			for (RepositoryConnectionListener listener : listeners) {
				listener.remove(this, subj, pred, obj, ctx);
			}
		} else {
			getDelegate().removeMatch(subj, pred, obj, ctx);
		}
	}

	@Override
	public void removeNamespace(String prefix)
		throws StoreException
	{
		getDelegate().removeNamespace(prefix);

		if (activated) {
			for (RepositoryConnectionListener listener : listeners) {
				listener.removeNamespace(this, prefix);
			}
		}
	}

	@Override
	public void clearNamespaces()
		throws StoreException
	{
		if (activated && reportDeltas()) {
			NamespaceResult namespaces;
			namespaces = getDelegate().getNamespaces();
			List<String> prefix = new ArrayList<String>();
			try {
				while (namespaces.hasNext()) {
					Namespace ns = namespaces.next();
					prefix.add(ns.getPrefix());
				}
			} finally {
				namespaces.close();
			}
			getDelegate().clearNamespaces();
			for (String p : prefix) {
				removeNamespace(p);
			}
		} else if (activated) {
			getDelegate().clearNamespaces();
			for (RepositoryConnectionListener listener : listeners) {
				listener.clearNamespaces(this);
			}
		} else {
			getDelegate().clearNamespaces();
		}
	}

	@Override
	public void rollback()
		throws StoreException
	{
		getDelegate().rollback();

		if (activated) {
			for (RepositoryConnectionListener listener : listeners) {
				listener.rollback(this);
			}
		}
	}

	@Override
	public void setAutoCommit(boolean autoCommit)
		throws StoreException
	{
		boolean wasAutoCommit = isAutoCommit();
		getDelegate().setAutoCommit(autoCommit);

		if (activated && wasAutoCommit != autoCommit) {
			for (RepositoryConnectionListener listener : listeners) {
				listener.setAutoCommit(this, autoCommit);
			}
			if (autoCommit) {
				for (RepositoryConnectionListener listener : listeners) {
					listener.commit(this);
				}
			}
		}
	}

	@Override
	public void setNamespace(String prefix, String name)
		throws StoreException
	{
		getDelegate().setNamespace(prefix, name);

		if (activated) {
			for (RepositoryConnectionListener listener : listeners) {
				listener.setNamespace(this, prefix, name);
			}
		}
	}
}
