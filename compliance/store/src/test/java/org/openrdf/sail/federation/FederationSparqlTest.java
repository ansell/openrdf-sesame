/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Open Software License version 3.0.
 */
package org.openrdf.sail.federation;

import junit.framework.Test;

import org.openrdf.query.Dataset;
import org.openrdf.query.parser.sparql.ManifestTest;
import org.openrdf.query.parser.sparql.SPARQLQueryTest;
import org.openrdf.repository.Repository;
import org.openrdf.repository.dataset.DatasetRepository;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.federation.Federation;
import org.openrdf.sail.memory.MemoryStore;

public class FederationSparqlTest extends SPARQLQueryTest {

	public static Test suite()
		throws Exception
	{
		return ManifestTest.suite(new Factory() {

			public SPARQLQueryTest createSPARQLQueryTest(String testURI, String name, String queryFileURL,
					String resultFileURL, Dataset dataSet)
			{
				return new FederationSparqlTest(testURI, name, queryFileURL, resultFileURL, dataSet);
			}
		});
	}

	public FederationSparqlTest(String testURI, String name, String queryFileURL, String resultFileURL,
			Dataset dataSet)
	{
		super(testURI, name, queryFileURL, resultFileURL, dataSet);
	}

	@Override
	protected Repository newRepository() {
		Federation sail = new Federation();
		sail.addMember(new SailRepository(new MemoryStore()));
		sail.addMember(new SailRepository(new MemoryStore()));
		sail.addMember(new SailRepository(new MemoryStore()));
		return new DatasetRepository(new SailRepository(sail));
	}
}
