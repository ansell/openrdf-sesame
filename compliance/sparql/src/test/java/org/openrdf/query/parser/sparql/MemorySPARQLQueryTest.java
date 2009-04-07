/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.parser.sparql;

import junit.framework.Test;

import org.openrdf.query.Dataset;
import org.openrdf.repository.Repository;
import org.openrdf.repository.dataset.DatasetRepository;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.memory.MemoryStore;

public class MemorySPARQLQueryTest extends SPARQLQueryTest {

	public static Test suite()
		throws Exception
	{
		return ManifestTest.suite(new Factory() {

			public MemorySPARQLQueryTest createSPARQLQueryTest(String testURI, String name, String queryFileURL,
					String resultFileURL, Dataset dataSet, boolean laxCardinality)
			{
				return new MemorySPARQLQueryTest(testURI, name, queryFileURL, resultFileURL, dataSet, laxCardinality);
			}
		});
	}

	protected MemorySPARQLQueryTest(String testURI, String name, String queryFileURL, String resultFileURL,
			Dataset dataSet, boolean laxCardinality)
	{
		super(testURI, name, queryFileURL, resultFileURL, dataSet, laxCardinality);
	}

	protected Repository newRepository() {
		return new DatasetRepository(new SailRepository(new MemoryStore()));
	}
}
