/*******************************************************************************
 * Copyright (c) 2015, Eclipse Foundation, Inc. and its licensors.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * - Neither the name of the Eclipse Foundation, Inc. nor the names of its
 *   contributors may be used to endorse or promote products derived from this
 *   software without specific prior written permission. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package org.eclipse.rdf4j.repository.event.base;

import java.io.File;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.base.RepositoryWrapper;
import org.eclipse.rdf4j.repository.event.InterceptingRepository;
import org.eclipse.rdf4j.repository.event.InterceptingRepositoryConnection;
import org.eclipse.rdf4j.repository.event.RepositoryConnectionInterceptor;
import org.eclipse.rdf4j.repository.event.RepositoryInterceptor;

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

	private Set<RepositoryConnectionInterceptor> conInterceptors = new CopyOnWriteArraySet<RepositoryConnectionInterceptor>();

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

	/**
	 * Registers a <tt>RepositoryConnectionInterceptor</tt> that will receive
	 * notifications of operations that are performed on any connections that are
	 * created by this repository.
	 */
	public void addRepositoryConnectionInterceptor(RepositoryConnectionInterceptor interceptor) {
		conInterceptors.add(interceptor);
	}

	/**
	 * Removes a registered <tt>RepositoryConnectionInterceptor</tt> from this
	 * repository.
	 */
	public void removeRepositoryConnectionInterceptor(RepositoryConnectionInterceptor interceptor) {
		conInterceptors.remove(interceptor);
	}

	@Override
	public InterceptingRepositoryConnection getConnection()
		throws RepositoryException
	{
		RepositoryConnection conn = getDelegate().getConnection();
		if (activated) {
			boolean denied = false;

			for (RepositoryInterceptor interceptor : interceptors) {
				denied = interceptor.getConnection(getDelegate(), conn);
				if (denied) {
					break;
				}
			}
			if (denied) {
				conn = null;
			}
		}
		if (conn == null)
			return null;

		InterceptingRepositoryConnection iconn = new InterceptingRepositoryConnectionWrapper(this, conn);
		for (RepositoryConnectionInterceptor conInterceptor : conInterceptors) {
			iconn.addRepositoryConnectionInterceptor(conInterceptor);
		}
		return iconn;
	}

	@Override
	public void initialize()
		throws RepositoryException
	{
		boolean denied = false;
		if (activated) {
			for (RepositoryInterceptor interceptor : interceptors) {
				denied = interceptor.initialize(getDelegate());
				if (denied) {
					break;
				}
			}
		}
		if (!denied) {
			getDelegate().initialize();
		}
	}

	@Override
	public void setDataDir(File dataDir) {
		boolean denied = false;
		if (activated) {
			for (RepositoryInterceptor interceptor : interceptors) {
				denied = interceptor.setDataDir(getDelegate(), dataDir);
				if (denied) {
					break;
				}
			}
		}
		if (!denied) {
			getDelegate().setDataDir(dataDir);
		}
	}

	@Override
	public void shutDown()
		throws RepositoryException
	{
		boolean denied = false;
		if (activated) {
			for (RepositoryInterceptor interceptor : interceptors) {
				denied = interceptor.shutDown(getDelegate());
				if (denied) {
					break;
				}
			}
		}
		if (!denied) {
			getDelegate().shutDown();
		}
	}
}
