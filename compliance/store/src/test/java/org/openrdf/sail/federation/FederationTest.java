/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2008.
 *
 * Licensed under the Open Software License version 3.0.
 */
package org.openrdf.sail.federation;

import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.RDFStoreTest;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailException;
import org.openrdf.sail.memory.MemoryStore;

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
		throws SailException
	{
		Federation sail = new Federation();
		sail.addMember(new SailRepository(new MemoryStore()));
		sail.addMember(new SailRepository(new MemoryStore()));
		sail.addMember(new SailRepository(new MemoryStore()));
		sail.initialize();
		return sail;
	}
}
