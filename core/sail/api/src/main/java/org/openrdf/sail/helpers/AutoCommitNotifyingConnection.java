/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008-2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.helpers;

import org.openrdf.sail.NotifyingSailConnection;
import org.openrdf.sail.SailConnectionListener;

/**
 * Adds auto-commit functionality to sail connections by wrapping updates with
 * calls to {@link #begin()} and {@link #commit()} when performed outside an
 * explicit transactions.
 * 
 * @author James Leigh
 * @author Arjohn Kampman
 */
public class AutoCommitNotifyingConnection extends AutoCommitSailConnection implements
		NotifyingSailConnection
{

	/*--------------*
	 * Constructors *
	 *--------------*/

	public AutoCommitNotifyingConnection(NotifyingSailConnection con) {
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
