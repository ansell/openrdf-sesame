/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms;

import org.openrdf.repository.Repository;
import org.openrdf.repository.SparqlOrderByTest;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.rdbms.mysql.MySqlStore;

public class MySqlSparqlOrderByTest extends SparqlOrderByTest {

	protected Repository newRepository() {
		MySqlStore sail = new MySqlStore("sesame_test");
		sail.setUser("sesame");
		sail.setPassword("opensesame");
		return new SailRepository(sail);
	}
}
