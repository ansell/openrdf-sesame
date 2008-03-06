/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.memory;

import org.openrdf.sail.RDFStoreTest;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailException;


/**
 * An extension of RDFStoreTest for testing the class
 * <tt>org.openrdf.sesame.sail.memory.MemoryStore</tt>.
 */
public class MemoryStoreTest extends RDFStoreTest {

	/*--------------*
	 * Constructors *
	 *--------------*/

	public MemoryStoreTest(String name) {
		super(name);
	}

	/*---------*
	 * Methods *
	 *---------*/

	protected Sail createSail()
		throws SailException
	{
		Sail sail = new MemoryStore();
		sail.initialize();
		return sail;
	}
}
