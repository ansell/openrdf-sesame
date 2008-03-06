/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.event.base;

import java.io.File;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.base.RepositoryWrapper;
import org.openrdf.repository.event.InterceptingRepository;
import org.openrdf.repository.event.InterceptingRepositoryConnection;
import org.openrdf.repository.event.RepositoryInterceptor;

/**
 * Wrapper that notifies interceptors of events on Repositories before they
 * happen. Any interceptor can block the operation by returning true from the
 * relevant notification method. To do so will also cause the notification
 * process to stop, i.e. no other interceptors will be notified. The order in
 * which interceptors are notified is unspecified.
 * 
 * @author Herko ter Horst
 * @see InterceptingRepositoryConnectionWrapper
 */
public class InterceptingRepositoryWrapper extends RepositoryWrapper implements InterceptingRepository {

	/*-----------*
	 * Variables *
	 *-----------*/

	private boolean activated;

	private Set<RepositoryInterceptor> interceptors = new CopyOnWriteArraySet<RepositoryInterceptor>();

	/*--------------*
	 * Constructors *
	 *--------------*/

	public InterceptingRepositoryWrapper() {
		super();
	}

	public InterceptingRepositoryWrapper(Repository delegate) {
		super(delegate);
	}

	/*---------*
	 * Methods *
	 *---------*/

	/**
	 * Registers a <tt>RepositoryInterceptor</tt> that will receive
	 * notifications of operations that are performed on this repository.
	 */
	public void addRepositoryInterceptor(RepositoryInterceptor interceptor) {
		interceptors.add(interceptor);
		activated = true;
	}

	/**
	 * Removes a registered <tt>RepositoryInterceptor</tt> from this
	 * repository.
	 */
	public void removeRepositoryInterceptor(RepositoryInterceptor interceptor) {
		interceptors.remove(interceptor);
		activated = !interceptors.isEmpty();
	}

	@Override
	public InterceptingRepositoryConnection getConnection()
		throws RepositoryException
	{
		InterceptingRepositoryConnection conn = new InterceptingRepositoryConnectionWrapper(this,
				super.getConnection());

		if (activated) {
			boolean denied = false;

			for (RepositoryInterceptor interceptor : interceptors) {
				denied = interceptor.getConnection(this, conn);
				if (!denied) {
					break;
				}
			}
			if (!denied) {
				conn = null;
			}
		}

		return conn;
	}

	@Override
	public void initialize()
		throws RepositoryException
	{
		boolean denied = false;
		if (activated) {
			for (RepositoryInterceptor interceptor : interceptors) {
				denied = interceptor.initialize(this);
				if (denied) {
					break;
				}
			}
		}
		if (!denied) {
			super.initialize();
		}
	}

	@Override
	public void setDataDir(File dataDir)
	{
		boolean denied = false;
		if (activated) {
			for (RepositoryInterceptor interceptor : interceptors) {
				denied = interceptor.setDataDir(this, dataDir);
				if (denied) {
					break;
				}
			}
		}
		if (!denied) {
			super.setDataDir(dataDir);
		}
	}

	@Override
	public void shutDown()
		throws RepositoryException
	{
		boolean denied = false;
		if (activated) {
			for (RepositoryInterceptor interceptor : interceptors) {
				denied = interceptor.shutDown(this);
				if (denied) {
					break;
				}
			}
		}
		if (!denied) {
			super.shutDown();
		}
	}
}
