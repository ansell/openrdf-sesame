/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.memory;

import org.openrdf.sail.Sail;
import org.openrdf.sail.SailConcurrencyTest;
import org.openrdf.sail.SailException;

/**
 * An extension of SailConcurrencyTest for testing the class {@link NativeStore}.
 */
public class MemoryStoreConcurrencyTest extends SailConcurrencyTest {

	/*--------------*
	 * Constructors *
	 *--------------*/

	public MemoryStoreConcurrencyTest(String name) {
		super(name);
	}

	/*---------*
	 * Methods *
	 *---------*/

	@Override
	protected Sail createSail()
		throws SailException
	{
		return new MemoryStore();
	}
}
