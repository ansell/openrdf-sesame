/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.memory;

import org.openrdf.sail.NotifyingSail;
import org.openrdf.sail.RDFNotifyingStoreTest;
import org.openrdf.store.StoreException;


/**
 * An extension of RDFStoreTest for testing the class
 * <tt>org.openrdf.sesame.sail.memory.MemoryStore</tt>.
 */
public class MemoryStoreTest extends RDFNotifyingStoreTest {

	/*--------------*
	 * Constructors *
	 *--------------*/

	public MemoryStoreTest(String name) {
		super(name);
	}

	/*---------*
	 * Methods *
	 *---------*/

	@Override
	protected NotifyingSail createSail()
		throws StoreException
	{
		NotifyingSail sail = new MemoryStore();
		sail.initialize();
		return sail;
	}
}
