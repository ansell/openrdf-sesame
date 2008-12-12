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
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.base.RepositoryConnectionWrapper;
import org.openrdf.repository.event.NotifyingRepositoryConnection;
import org.openrdf.repository.event.RepositoryConnectionListener;

/**
 * This broadcaster is used by the RepositoryBroadcaster to wrap the delegate
 * repository connection. Listeners are notified of changes after they have
 * occurred.
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
	protected boolean isDelegatingAdd() {
		return !activated;
	}

	@Override
	protected boolean isDelegatingRemove() {
		return !activated;
	}

	@Override
	public void addWithoutCommit(Resource subject, URI predicate, Value object, Resource... contexts)
		throws RepositoryException
	{
		boolean reportEvent = activated;

		if (reportEvent && reportDeltas()) {
			// Only report if the stament is not present yet
			reportEvent = !getDelegate().hasStatement(subject, predicate, object, false, contexts);
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
		throws RepositoryException
	{
		if (activated && reportDeltas()) {
			removeWithoutCommit(null, null, null, contexts);
		}
		else if (activated) {
			getDelegate().clear(contexts);
			for (RepositoryConnectionListener listener : listeners) {
				listener.clear(this, contexts);
			}
		}
		else {
			getDelegate().clear(contexts);
		}
	}

	@Override
	public void close()
		throws RepositoryException
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
		throws RepositoryException
	{
		getDelegate().commit();

		if (activated) {
			for (RepositoryConnectionListener listener : listeners) {
				listener.commit(this);
			}
		}
	}

	@Override
	public void removeWithoutCommit(Resource subj, URI pred, Value obj, Resource... ctx)
		throws RepositoryException
	{
		if (activated && reportDeltas()) {
			RepositoryResult<Statement> stmts;
			stmts = getDelegate().getStatements(subj, pred, obj, false, ctx);
			List<Statement> list = new ArrayList<Statement>();
			try {
				while (stmts.hasNext()) {
					list.add(stmts.next());
				}
			}
			finally {
				stmts.close();
			}
			getDelegate().remove(subj, pred, obj, ctx);
			for (RepositoryConnectionListener listener : listeners) {
				for (Statement stmt : list) {
					Resource s = stmt.getSubject();
					URI p = stmt.getPredicate();
					Value o = stmt.getObject();
					Resource c = stmt.getContext();
					listener.remove(this, s, p, o, c);
				}
			}
		}
		else if (activated) {
			getDelegate().remove(subj, pred, obj, ctx);
			for (RepositoryConnectionListener listener : listeners) {
				listener.remove(this, subj, pred, obj, ctx);
			}
		}
		else {
			getDelegate().remove(subj, pred, obj, ctx);
		}
	}

	@Override
	public void removeNamespace(String prefix)
		throws RepositoryException
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
		throws RepositoryException
	{
		if (activated && reportDeltas()) {
			RepositoryResult<Namespace> namespaces;
			namespaces = getDelegate().getNamespaces();
			List<String> prefix = new ArrayList<String>();
			try {
				while (namespaces.hasNext()) {
					Namespace ns = namespaces.next();
					prefix.add(ns.getPrefix());
				}
			}
			finally {
				namespaces.close();
			}
			getDelegate().clearNamespaces();
			for (String p : prefix) {
				removeNamespace(p);
			}
		}
		else if (activated) {
			getDelegate().clearNamespaces();
			for (RepositoryConnectionListener listener : listeners) {
				listener.clearNamespaces(this);
			}
		}
		else {
			getDelegate().clearNamespaces();
		}
	}

	@Override
	public void rollback()
		throws RepositoryException
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
		throws RepositoryException
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
		throws RepositoryException
	{
		getDelegate().setNamespace(prefix, name);

		if (activated) {
			for (RepositoryConnectionListener listener : listeners) {
				listener.setNamespace(this, prefix, name);
			}
		}
	}
}
