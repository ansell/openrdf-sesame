/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.memory;

import org.openrdf.repository.RDFSchemaRepositoryConnectionTest;
import org.openrdf.repository.Repository;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.inferencer.fc.ForwardChainingRDFSInferencer;
import org.openrdf.sail.memory.MemoryStore;

public class RDFSchemaMemoryRepositoryConnectionTest extends RDFSchemaRepositoryConnectionTest {

	public RDFSchemaMemoryRepositoryConnectionTest(String name) {
		super(name);
	}

	@Override
	protected Repository createRepository() {
		return new SailRepository(new ForwardChainingRDFSInferencer(new MemoryStore()));
	}
}
