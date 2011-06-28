/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2011.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.parser.sparql;

import org.openrdf.repository.Repository;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.memory.MemoryStore;


/**
 * Test SPARQL 1.1 Update functionality on an in-memory store.
 * 
 * @author Jeen Broekstra
 */
public class MemorySPARQLUpdateTest extends SPARQLUpdateTest {

	@Override
	protected Repository newRepository()
		throws Exception
	{
		return new SailRepository(new MemoryStore());
	}

}
