package org.openrdf.query.algebra.geosparql;

import junit.framework.Test;

import org.openrdf.query.Dataset;
import org.openrdf.query.parser.sparql.manifest.SPARQLQueryTest;
import org.openrdf.repository.Repository;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.memory.MemoryStore;

public class MemoryGeoSPARQLQueryTest extends SPARQLQueryTest {
	public static Test suite() throws Exception
	{
		return GeoSPARQLManifestTest.suite(new Factory()
		{
			@Override
			public SPARQLQueryTest createSPARQLQueryTest(String testURI,
					String name, String queryFileURL, String resultFileURL,
					Dataset dataSet, boolean laxCardinality) {
				return createSPARQLQueryTest(testURI, name, queryFileURL, resultFileURL, dataSet, laxCardinality, false);
			}
	
			@Override
			public SPARQLQueryTest createSPARQLQueryTest(String testURI,
					String name, String queryFileURL, String resultFileURL,
					Dataset dataSet, boolean laxCardinality, boolean checkOrder) {
				return new MemoryGeoSPARQLQueryTest(testURI, name, queryFileURL, resultFileURL, dataSet, laxCardinality, checkOrder);
			}
			
		});
	}

	protected MemoryGeoSPARQLQueryTest(String testURI,
			String name, String queryFileURL, String resultFileURL,
			Dataset dataSet, boolean laxCardinality, boolean checkOrder)
	{
		super(testURI, name, queryFileURL, resultFileURL, dataSet, laxCardinality, checkOrder);
	}

	@Override
	protected Repository newRepository() throws Exception {
		return new SailRepository(new MemoryStore());
	}
}
