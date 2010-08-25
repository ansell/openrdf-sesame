/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2009-2010.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.accesscontrol;

import org.openrdf.sail.Sail;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.helpers.SailWrapper;
import org.openrdf.store.StoreException;

/**
 * @author Jeen Broekstra
 */
public class AccessControlSail extends SailWrapper {

	public AccessControlSail(Sail sail) {
		super(sail);
	}

	public AccessControlSail() {
		super();
	}

	@Override
	public SailConnection getConnection()
		throws StoreException
	{
		return new AccessControlConnection(super.getConnection());
	}
}
