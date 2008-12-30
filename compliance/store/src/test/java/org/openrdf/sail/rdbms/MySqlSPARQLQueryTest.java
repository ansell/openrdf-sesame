/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms;

import junit.framework.Test;

import org.openrdf.query.Dataset;
import org.openrdf.query.parser.sparql.ManifestTest;
import org.openrdf.query.parser.sparql.SPARQLQueryTest;
import org.openrdf.repository.Repository;
import org.openrdf.repository.dataset.DatasetRepository;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.rdbms.mysql.MySqlStore;

public class MySqlSPARQLQueryTest extends SPARQLQueryTest {

	public static Test suite() throws Exception {
		return ManifestTest.suite(new Factory() {
			public MySqlSPARQLQueryTest createSPARQLQueryTest(String testURI,
					String name, String queryFileURL, String resultFileURL,
					Dataset dataSet) {
				return new MySqlSPARQLQueryTest(testURI, name, queryFileURL,
						resultFileURL, dataSet);
			}
		});
	}

	protected MySqlSPARQLQueryTest(String testURI, String name,
			String queryFileURL, String resultFileURL, Dataset dataSet) {
		super(testURI, name, queryFileURL, resultFileURL, dataSet);
	}

	protected Repository newRepository() {
		MySqlStore sail = new MySqlStore("sesame_test");
		sail.setUser("sesame");
		sail.setPassword("opensesame");
		return new DatasetRepository(new SailRepository(sail));
	}
}
