/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2009-2010.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.accesscontrol;

import org.openrdf.sail.RDFStoreTest;
import org.openrdf.sail.Sail;
import org.openrdf.sail.memory.MemoryStore;
import org.openrdf.store.StoreException;

/**
 * @author Jeen Broekstra
 */
public class AccessControlTest extends RDFStoreTest {

	public AccessControlTest(String name) {
		super(name);
	}

	@Override
	protected Sail createSail()
		throws StoreException
	{
		Sail sail = new AccessControlSail(new MemoryStore());
		sail.initialize();
		return sail;
	}
}
