/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.accesscontrol;

import org.openrdf.sail.RDFStoreTest;
import org.openrdf.sail.Sail;
import org.openrdf.sail.memory.MemoryStore;
import org.openrdf.store.StoreException;


/**
 *
 * @author Jeen Broekstra
 */
public class AccessControlTest extends RDFStoreTest {

	/**
	 * @param name
	 */
	public AccessControlTest(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected Sail createSail()
		throws StoreException
	{
		Sail sail = new AccessControlSail((Sail)new MemoryStore());
		sail.initialize();
		
		// TODO add some data?
		
		return sail;
	}



}
