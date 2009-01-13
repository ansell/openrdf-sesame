/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.helpers;

import org.openrdf.sail.NotifyingSailConnection;
import org.openrdf.sail.SailConnectionListener;

/**
 * Auto begins and commits transactions.
 * 
 * @author James Leigh
 */
public class AutoBeginNotifyingConnection extends AutoBeginSailConnection implements NotifyingSailConnection {

	/*--------------*
	 * Constructors *
	 *--------------*/

	public AutoBeginNotifyingConnection(NotifyingSailConnection con) {
		super(con);
	}

	/*---------*
	 * Methods *
	 *---------*/

	@Override
	protected NotifyingSailConnection getWrappedConnection() {
		return (NotifyingSailConnection)super.getWrappedConnection();
	}

	public void addConnectionListener(SailConnectionListener listener) {
		getWrappedConnection().addConnectionListener(listener);
	}

	public void removeConnectionListener(SailConnectionListener listener) {
		getWrappedConnection().removeConnectionListener(listener);
	}
}
