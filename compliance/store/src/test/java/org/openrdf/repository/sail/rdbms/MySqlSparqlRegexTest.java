package org.openrdf.repository.sail.rdbms;

import org.openrdf.repository.Repository;
import org.openrdf.repository.SparqlRegexTest;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.rdbms.mysql.MySqlStore;

public class MySqlSparqlRegexTest extends SparqlRegexTest {

	protected Repository newRepository() {
		return new SailRepository(new MySqlStore("sesame_test"));
	}
}
