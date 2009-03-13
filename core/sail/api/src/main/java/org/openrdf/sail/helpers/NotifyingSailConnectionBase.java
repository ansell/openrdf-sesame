/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.helpers;

import java.util.ArrayList;
import java.util.List;

import org.openrdf.model.Statement;
import org.openrdf.sail.NotifyingSailConnection;
import org.openrdf.sail.SailConnectionListener;

/**
 * This class extends {@link SailConnectionBase} adding support for
 * {@link NotifyingSailConnection}. implementations.
 * 
 * @author Arjohn Kampman
 * @author jeen
 * @author James Leigh
 */
public abstract class NotifyingSailConnectionBase extends SailConnectionBase implements
		NotifyingSailConnection
{

	/*-----------*
	 * Variables *
	 *-----------*/

	private final List<SailConnectionListener> listeners = new ArrayList<SailConnectionListener>(0);

	/*---------*
	 * Methods *
	 *---------*/

	public void addConnectionListener(SailConnectionListener listener) {
		synchronized (listeners) {
			listeners.add(listener);
		}
	}

	public void removeConnectionListener(SailConnectionListener listener) {
		synchronized (listeners) {
			listeners.remove(listener);
		}
	}

	protected boolean hasConnectionListeners() {
		synchronized (listeners) {
			return !listeners.isEmpty();
		}
	}

	protected void notifyStatementAdded(Statement st) {
		synchronized (listeners) {
			for (SailConnectionListener listener : listeners) {
				listener.statementAdded(st);
			}
		}
	}

	protected void notifyStatementRemoved(Statement st) {
		synchronized (listeners) {
			for (SailConnectionListener listener : listeners) {
				listener.statementRemoved(st);
			}
		}
	}
}
