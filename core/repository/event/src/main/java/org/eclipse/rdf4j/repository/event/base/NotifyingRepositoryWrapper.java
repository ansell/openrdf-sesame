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
import org.eclipse.rdf4j.repository.event.NotifyingRepository;
import org.eclipse.rdf4j.repository.event.NotifyingRepositoryConnection;
import org.eclipse.rdf4j.repository.event.RepositoryConnectionListener;
import org.eclipse.rdf4j.repository.event.RepositoryListener;

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
		NotifyingRepositoryConnection ncon = new NotifyingRepositoryConnectionWrapper(this,
				con, getDefaultReportDeltas());

		if (activated) {
			for (RepositoryListener listener : listeners) {
				listener.getConnection(this, ncon);
			}
		}
		for (RepositoryConnectionListener l : conListeners) {
			ncon.addRepositoryConnectionListener(l);
		}

		return ncon;
	}

	@Override
	public void initialize()
		throws RepositoryException
	{
		super.initialize();

		if (activated) {
			for (RepositoryListener listener : listeners) {
				listener.initialize(this);
			}
		}
	}

	@Override
	public void setDataDir(File dataDir)
	{
		super.setDataDir(dataDir);

		if (activated) {
			for (RepositoryListener listener : listeners) {
				listener.setDataDir(this, dataDir);
			}
		}
	}

	@Override
	public void shutDown()
		throws RepositoryException
	{
		super.shutDown();

		if (activated) {
			for (RepositoryListener listener : listeners) {
				listener.shutDown(this);
			}
		}
	}
}
