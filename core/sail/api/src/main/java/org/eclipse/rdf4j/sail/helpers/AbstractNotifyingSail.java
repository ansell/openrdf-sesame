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
package org.eclipse.rdf4j.sail.helpers;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.rdf4j.sail.NotifyingSail;
import org.eclipse.rdf4j.sail.NotifyingSailConnection;
import org.eclipse.rdf4j.sail.SailChangedEvent;
import org.eclipse.rdf4j.sail.SailChangedListener;
import org.eclipse.rdf4j.sail.SailException;

/**
 * A base {@link NotifyingSail} implementation that takes care of common sail
 * tasks, including proper closing of active connections and a grace period for
 * active connections during shutdown of the store.
 * 
 * @author Herko ter Horst
 * @author jeen
 * @author Arjohn Kampman
 */
public abstract class AbstractNotifyingSail extends AbstractSail implements NotifyingSail {

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * Objects that should be notified of changes to the data in this Sail.
	 */
	private Set<SailChangedListener> sailChangedListeners = new HashSet<SailChangedListener>(0);

	/*---------*
	 * Methods *
	 *---------*/

	@Override
	public NotifyingSailConnection getConnection()
		throws SailException
	{
		return (NotifyingSailConnection)super.getConnection();
	}

	@Override
	protected abstract NotifyingSailConnection getConnectionInternal()
		throws SailException;

	@Override
	public void addSailChangedListener(SailChangedListener listener) {
		synchronized (sailChangedListeners) {
			sailChangedListeners.add(listener);
		}
	}

	@Override
	public void removeSailChangedListener(SailChangedListener listener) {
		synchronized (sailChangedListeners) {
			sailChangedListeners.remove(listener);
		}
	}

	/**
	 * Notifies all registered SailChangedListener's of changes to the contents
	 * of this Sail.
	 */
	public void notifySailChanged(SailChangedEvent event) {
		synchronized (sailChangedListeners) {
			for (SailChangedListener l : sailChangedListeners) {
				l.sailChanged(event);
			}
		}
	}
}
