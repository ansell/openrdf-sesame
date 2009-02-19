/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.helpers;

import org.openrdf.sail.NotifyingSail;
import org.openrdf.sail.NotifyingSailConnection;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailChangedListener;
import org.openrdf.store.StoreException;

/**
 * An implementation of the StackableSail interface that wraps another Sail
 * object and forwards any relevant calls to the wrapped Sail.
 * 
 * @author Arjohn Kampman
 * @author James Leigh
 */
public class NotifyingSailWrapper extends SailWrapper implements NotifyingSail {

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new SailWrapper. The base Sail for the created SailWrapper can
	 * be set later using {@link #setBaseSail}.
	 */
	public NotifyingSailWrapper() {
	}

	/**
	 * Creates a new SailWrapper that wraps the supplied Sail.
	 */
	public NotifyingSailWrapper(NotifyingSail baseSail) {
		setDelegate(baseSail);
	}

	/*---------*
	 * Methods *
	 *---------*/

	@Override
	public void setDelegate(Sail baseSail) {
		super.setDelegate((NotifyingSail)baseSail);
	}

	@Override
	public NotifyingSail getDelegate() {
		return (NotifyingSail)super.getDelegate();
	}

	@Override
	public NotifyingSailConnection getConnection()
		throws StoreException
	{
		return (NotifyingSailConnection)super.getConnection();
	}

	public void addSailChangedListener(SailChangedListener listener) {
		verifyBaseSailSet();
		getDelegate().addSailChangedListener(listener);
	}

	public void removeSailChangedListener(SailChangedListener listener) {
		verifyBaseSailSet();
		getDelegate().removeSailChangedListener(listener);
	}
}
