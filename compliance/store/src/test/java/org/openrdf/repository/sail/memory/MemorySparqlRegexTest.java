package org.openrdf.repository.sail.memory;

import org.openrdf.repository.Repository;
import org.openrdf.repository.SparqlRegexTest;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.memory.MemoryStore;

public class MemorySparqlRegexTest extends SparqlRegexTest {

	protected Repository newRepository() {
		return new SailRepository(new MemoryStore());
	}
}
