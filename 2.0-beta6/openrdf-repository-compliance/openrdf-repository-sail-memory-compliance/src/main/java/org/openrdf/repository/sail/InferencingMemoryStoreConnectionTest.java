/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.sail;

import org.openrdf.repository.InferencingRepositoryConnectionTest;
import org.openrdf.repository.Repository;
import org.openrdf.sail.memory.MemoryStore;
import org.openrdf.sail.memory.MemoryStoreRDFSInferencer;

public class InferencingMemoryStoreConnectionTest extends InferencingRepositoryConnectionTest {

	public InferencingMemoryStoreConnectionTest(String name) {
		super(name);
	}

	protected Repository createRepository() {
		return new SailRepository(new MemoryStoreRDFSInferencer(new MemoryStore()));
	}
}
