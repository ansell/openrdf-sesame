/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2008.
 *
 * Licensed under the Open Software License version 3.0.
 */
package org.openrdf.repository.sail.federation;

import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.RDFStoreTest;
import org.openrdf.sail.Sail;
import org.openrdf.sail.federation.Federation;
import org.openrdf.sail.memory.MemoryStore;
import org.openrdf.store.StoreException;

/**
 * An extension of RDFStoreTest for testing the class {@link MulgaraStore}.
 */
public class FederationTest extends RDFStoreTest {

	/*--------------*
	 * Constructors *
	 *--------------*/

	public FederationTest(String name) {
		super(name);
	}

	/*---------*
	 * Methods *
	 *---------*/

	@Override
	protected Sail createSail()
		throws StoreException
	{
		Federation sail = new Federation();
		sail.addMember(new SailRepository(new MemoryStore()));
		sail.addMember(new SailRepository(new MemoryStore()));
		sail.addMember(new SailRepository(new MemoryStore()));		sail.setWritable(true);
		sail.initialize();
		return sail;
	}
}
