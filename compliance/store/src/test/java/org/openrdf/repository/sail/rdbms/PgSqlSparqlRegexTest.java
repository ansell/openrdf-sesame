package org.openrdf.repository.sail.rdbms;

import org.openrdf.repository.Repository;
import org.openrdf.repository.SparqlRegexTest;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.rdbms.postgresql.PgSqlStore;

public class PgSqlSparqlRegexTest extends SparqlRegexTest {

	protected Repository newRepository() {
		return new SailRepository(new PgSqlStore("sesame_test"));
	}
}
