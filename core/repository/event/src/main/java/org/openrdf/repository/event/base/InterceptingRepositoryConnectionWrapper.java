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
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.StoreException;
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
		throws StoreException
	{
		boolean denied = false;
		if (activated) {
			for (RepositoryConnectionInterceptor interceptor : interceptors) {
				denied = interceptor.add(this, subject, predicate, object, contexts);
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
		throws StoreException
	{
		boolean denied = false;
		if (activated) {
			for (RepositoryConnectionInterceptor interceptor : interceptors) {
				denied = interceptor.clear(this, contexts);
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
	public void close()
		throws StoreException
	{
		boolean denied = false;
		if (activated) {
			for (RepositoryConnectionInterceptor interceptor : interceptors) {
				denied = interceptor.close(this);
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
		throws StoreException
	{
		boolean denied = false;
		if (activated) {
			for (RepositoryConnectionInterceptor interceptor : interceptors) {
				denied = interceptor.commit(this);
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
		throws StoreException
	{
		boolean denied = false;
		if (activated) {
			for (RepositoryConnectionInterceptor interceptor : interceptors) {
				denied = interceptor.remove(this, subject, predicate, object, contexts);
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
		throws StoreException
	{
		boolean denied = false;
		if (activated) {
			for (RepositoryConnectionInterceptor interceptor : interceptors) {
				denied = interceptor.removeNamespace(this, prefix);
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
		throws StoreException
	{
		boolean denied = false;
		if (activated) {
			for (RepositoryConnectionInterceptor interceptor : interceptors) {
				denied = interceptor.clearNamespaces(this);
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
		throws StoreException
	{
		boolean denied = false;
		if (activated) {
			for (RepositoryConnectionInterceptor interceptor : interceptors) {
				denied = interceptor.rollback(this);
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
	public void setAutoCommit(boolean autoCommit)
		throws StoreException
	{
		boolean denied = false;
		boolean wasAutoCommit = isAutoCommit();
		if (activated && wasAutoCommit != autoCommit) {
			for (RepositoryConnectionInterceptor interceptor : interceptors) {
				denied = interceptor.setAutoCommit(this, autoCommit);
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
		throws StoreException
	{
		boolean denied = false;
		if (activated) {
			for (RepositoryConnectionInterceptor interceptor : interceptors) {
				denied = interceptor.setNamespace(this, prefix, name);
				if (denied) {
					break;
				}
			}
		}
		if (!denied) {
			getDelegate().setNamespace(prefix, name);
		}
	}
}
