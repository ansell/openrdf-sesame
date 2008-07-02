package org.openrdf.repository.sail.memory;

import org.openrdf.repository.Repository;
import org.openrdf.repository.SparqlOrderByTest;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.memory.MemoryStore;

public class MemorySparqlOrderByTest extends SparqlOrderByTest {

	protected Repository newRepository() {
		return new SailRepository(new MemoryStore());
	}
}
