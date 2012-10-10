/*
 * Copyright James Leigh (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.event.base;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.Update;
import org.openrdf.query.UpdateExecutionException;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.base.RepositoryConnectionWrapper;
import org.openrdf.repository.event.InterceptingRepositoryConnection;
import org.openrdf.repository.event.RepositoryConnectionInterceptor;

/**
 * Wrapper that notifies interceptors of events on RepositoryConnections before
 * they happen. Any interceptor can block the operation by returning true from
 * the relevant notification method. To do so will also cause the notification
 * process to stop, i.e. no other interceptors will be notified. The order in
 * which interceptors are notified is unspecified.
 * 
 * @author Herko ter Horst
 * @see InterceptingRepositoryWrapper
 */
public class InterceptingRepositoryConnectionWrapper extends RepositoryConnectionWrapper implements InterceptingRepositoryConnection {

	/*-----------*
	 * Variables *
	 *-----------*/

	private boolean activated;

	private Set<RepositoryConnectionInterceptor> interceptors = new CopyOnWriteArraySet<RepositoryConnectionInterceptor>();

	/*--------------*
	 * Construcotrs *
	 *--------------*/

	public InterceptingRepositoryConnectionWrapper(Repository repository, RepositoryConnection connection) {
		super(repository, connection);
	}

	/*---------*
	 * Methods *
	 *---------*/

	/**
	 * Registers a <tt>RepositoryConnectionInterceptor</tt> that will receive
	 * notifications of operations that are performed on this connection.
	 */
	public void addRepositoryConnectionInterceptor(RepositoryConnectionInterceptor interceptor) {
		interceptors.add(interceptor);
		activated = true;
	}

	/**
	 * Removes a registered <tt>RepositoryConnectionInterceptor</tt> from this
	 * connection.
	 */
	public void removeRepositoryConnectionInterceptor(RepositoryConnectionInterceptor interceptor) {
		interceptors.remove(interceptor);
		activated = !interceptors.isEmpty();
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
	public void addWithoutCommit(Resource subject, URI predicate, Value object, Resource... contexts)
		throws RepositoryException
	{
		boolean denied = false;
		if (activated) {
			for (RepositoryConnectionInterceptor interceptor : interceptors) {
				denied = interceptor.add(getDelegate(), subject, predicate, object, contexts);
				if (denied) {
					break;
				}
			}
		}
		if (!denied) {
			getDelegate().add(subject, predicate, object, contexts);
		}
	}

	@Override
	public void clear(Resource... contexts)
		throws RepositoryException
	{
		boolean denied = false;
		if (activated) {
			for (RepositoryConnectionInterceptor interceptor : interceptors) {
				denied = interceptor.clear(getDelegate(), contexts);
				if (denied) {
					break;
				}
			}
		}
		if (!denied) {
			getDelegate().clear(contexts);
		}
	}
	
	@Override
	public void begin()
		throws RepositoryException
	{
		boolean denied = false;
		if (activated) {
			for (RepositoryConnectionInterceptor interceptor : interceptors) {
				denied = interceptor.begin(getDelegate());
				if (denied) {
					break;
				}
			}
		}
		if (!denied) {
			super.begin();
		}
	}

	@Override
	public void close()
		throws RepositoryException
	{
		boolean denied = false;
		if (activated) {
			for (RepositoryConnectionInterceptor interceptor : interceptors) {
				denied = interceptor.close(getDelegate());
				if (denied) {
					break;
				}
			}
		}
		if (!denied) {
			super.close();
		}
	}

	@Override
	public void commit()
		throws RepositoryException
	{
		boolean denied = false;
		if (activated) {
			for (RepositoryConnectionInterceptor interceptor : interceptors) {
				denied = interceptor.commit(getDelegate());
				if (denied) {
					break;
				}
			}
		}
		if (!denied) {
			getDelegate().commit();
		}
	}

	@Override
	public void removeWithoutCommit(Resource subject, URI predicate, Value object, Resource... contexts)
		throws RepositoryException
	{
		boolean denied = false;
		if (activated) {
			for (RepositoryConnectionInterceptor interceptor : interceptors) {
				denied = interceptor.remove(getDelegate(), subject, predicate, object, contexts);
				if (denied) {
					break;
				}
			}
		}
		if (!denied) {
			getDelegate().remove(subject, predicate, object, contexts);

		}
	}

	@Override
	public void removeNamespace(String prefix)
		throws RepositoryException
	{
		boolean denied = false;
		if (activated) {
			for (RepositoryConnectionInterceptor interceptor : interceptors) {
				denied = interceptor.removeNamespace(getDelegate(), prefix);
				if (denied) {
					break;
				}
			}
		}
		if (!denied) {
			getDelegate().removeNamespace(prefix);
		}
	}

	@Override
	public void clearNamespaces()
		throws RepositoryException
	{
		boolean denied = false;
		if (activated) {
			for (RepositoryConnectionInterceptor interceptor : interceptors) {
				denied = interceptor.clearNamespaces(getDelegate());
				if (denied) {
					break;
				}
			}
		}
		if (!denied) {
			getDelegate().clearNamespaces();
		}
	}

	@Override
	public void rollback()
		throws RepositoryException
	{
		boolean denied = false;
		if (activated) {
			for (RepositoryConnectionInterceptor interceptor : interceptors) {
				denied = interceptor.rollback(getDelegate());
				if (denied) {
					break;
				}
			}
		}
		if (!denied) {
			getDelegate().rollback();
		}
	}

	@Override
	@Deprecated
	public void setAutoCommit(boolean autoCommit)
		throws RepositoryException
	{
		boolean denied = false;
		boolean wasAutoCommit = isAutoCommit();
		if (activated && wasAutoCommit != autoCommit) {
			for (RepositoryConnectionInterceptor interceptor : interceptors) {
				denied = interceptor.setAutoCommit(getDelegate(), autoCommit);
				if (denied) {
					break;
				}
			}
		}
		if (!denied) {
			getDelegate().setAutoCommit(autoCommit);

		}
	}

	@Override
	public void setNamespace(String prefix, String name)
		throws RepositoryException
	{
		boolean denied = false;
		if (activated) {
			for (RepositoryConnectionInterceptor interceptor : interceptors) {
				denied = interceptor.setNamespace(getDelegate(), prefix, name);
				if (denied) {
					break;
				}
			}
		}
		if (!denied) {
			getDelegate().setNamespace(prefix, name);
		}
	}

	@Override
	public Update prepareUpdate(final QueryLanguage ql, final String update, final String baseURI)
		throws MalformedQueryException, RepositoryException
	{
		if (activated) {
			return new Update() {

				private final RepositoryConnection conn = getDelegate();

				private final Update delegate = conn.prepareUpdate(ql, update, baseURI);

				public void execute()
					throws UpdateExecutionException
				{
					boolean denied = false;
					if (activated) {
						for (RepositoryConnectionInterceptor interceptor : interceptors) {
							denied = interceptor.execute(conn, ql, update, baseURI, delegate);
							if (denied) {
								break;
							}
						}
					}
					if (!denied) {
						delegate.execute();
					}
				}

				public void setBinding(String name, Value value) {
					delegate.setBinding(name, value);
				}

				public void removeBinding(String name) {
					delegate.removeBinding(name);
				}

				public void clearBindings() {
					delegate.clearBindings();
				}

				public BindingSet getBindings() {
					return delegate.getBindings();
				}

				public void setDataset(Dataset dataset) {
					delegate.setDataset(dataset);
				}

				public Dataset getDataset() {
					return delegate.getDataset();
				}

				public void setIncludeInferred(boolean includeInferred) {
					delegate.setIncludeInferred(includeInferred);
				}

				public boolean getIncludeInferred() {
					return delegate.getIncludeInferred();
				}
			};
		}
		else {
			return getDelegate().prepareUpdate(ql, update, baseURI);
		}
	}
}
