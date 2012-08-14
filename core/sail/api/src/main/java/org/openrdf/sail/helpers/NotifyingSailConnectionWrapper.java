/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.helpers;

import org.openrdf.sail.NotifyingSailConnection;
import org.openrdf.sail.SailConnectionListener;

/**
 * An implementation of the Transaction interface that wraps another Transaction
 * object and forwards any method calls to the wrapped transaction.
 * 
 * @author jeen
 */
public class NotifyingSailConnectionWrapper extends SailConnectionWrapper implements NotifyingSailConnection {

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new TransactionWrapper object that wraps the supplied
	 * connection.
	 */
	public NotifyingSailConnectionWrapper(NotifyingSailConnection wrappedCon) {
		super(wrappedCon);
	}

	/*---------*
	 * Methods *
	 *---------*/

	@Override
	public NotifyingSailConnection getWrappedConnection() {
		return (NotifyingSailConnection)super.getWrappedConnection();
	}

	public void addConnectionListener(SailConnectionListener listener) {
		getWrappedConnection().addConnectionListener(listener);
	}

	public void removeConnectionListener(SailConnectionListener listener) {
		getWrappedConnection().addConnectionListener(listener);
	}
}
