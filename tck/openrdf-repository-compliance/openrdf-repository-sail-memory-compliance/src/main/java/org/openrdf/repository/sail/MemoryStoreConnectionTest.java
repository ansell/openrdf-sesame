/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.sail;

import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnectionTest;
import org.openrdf.sail.memory.MemoryStore;

public class MemoryStoreConnectionTest extends RepositoryConnectionTest {

	public MemoryStoreConnectionTest(String name) {
		super(name);
	}

	@Override
	protected Repository createRepository() {
		return new SailRepository(new MemoryStore());
	}
}
