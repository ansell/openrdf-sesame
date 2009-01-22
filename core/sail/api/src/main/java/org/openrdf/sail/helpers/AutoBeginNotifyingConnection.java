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
	protected NotifyingSailConnection getDelegate() {
		return (NotifyingSailConnection)super.getDelegate();
	}

	public void addConnectionListener(SailConnectionListener listener) {
		getDelegate().addConnectionListener(listener);
	}

	public void removeConnectionListener(SailConnectionListener listener) {
		getDelegate().removeConnectionListener(listener);
	}
}
