/*
 * Copyright (c) 2011 3 Round Stones Inc., Some rights reserved.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.sail.memory;

import org.openrdf.repository.Repository;
import org.openrdf.repository.SparqlAggregatesTest;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.memory.MemoryStore;

public class MemorySparqlAggregatesTest extends SparqlAggregatesTest {

	protected Repository newRepository() {
		return new SailRepository(new MemoryStore());
	}
}
