/* 
 * Licensed to Aduna under one or more contributor license agreements.  
 * See the NOTICE.txt file distributed with this work for additional 
 * information regarding copyright ownership. 
 *
 * Aduna licenses this file to you under the terms of the Aduna BSD 
 * License (the "License"); you may not use this file except in compliance 
 * with the License. See the LICENSE.txt file distributed with this work 
 * for the full License.
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package org.openrdf.sail.lucene;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.GEO;
import org.openrdf.model.vocabulary.GEOF;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.memory.MemoryStore;

public abstract class AbstractLuceneSailGeoSPARQLTest {

	public static final URI SUBJECT_1 = new URIImpl("urn:subject1");

	public static final URI SUBJECT_2 = new URIImpl("urn:subject2");

	public static final URI SUBJECT_3 = new URIImpl("urn:subject3");

	public static final URI SUBJECT_4 = new URIImpl("urn:subject4");

	public static final URI SUBJECT_5 = new URIImpl("urn:subject5");

	public static final URI CONTEXT_1 = new URIImpl("urn:context1");

	public static final URI CONTEXT_2 = new URIImpl("urn:context2");

	public static final URI CONTEXT_3 = new URIImpl("urn:context3");

	public static final Literal EIFFEL_TOWER = new LiteralImpl("POINT (2.2945 48.8582)", GEO.WKT_LITERAL);
	public static final Literal ARC_TRIOMPHE = new LiteralImpl("POINT (2.2950 48.8738)", GEO.WKT_LITERAL);
	public static final Literal NOTRE_DAME = new LiteralImpl("POINT (2.3465 48.8547)", GEO.WKT_LITERAL);

	private static final double ERROR = 1.0;

	protected LuceneSail sail;

	protected Repository repository;

	protected RepositoryConnection connection;

	protected abstract void configure(LuceneSail sail) throws IOException;

	@Before
	public void setUp()
		throws IOException, RepositoryException
	{
		// setup a LuceneSail
		MemoryStore memoryStore = new MemoryStore();
		// enable lock tracking
		info.aduna.concurrent.locks.Properties.setLockTrackingEnabled(true);
		sail = new LuceneSail();
		configure(sail);
		sail.setBaseSail(memoryStore);

		// create a Repository wrapping the LuceneSail
		repository = new SailRepository(sail);
		repository.initialize();

		// add some statements to it
		connection = repository.getConnection();
		connection.begin();
		connection.add(SUBJECT_1, GEO.AS_WKT, EIFFEL_TOWER);
		connection.add(SUBJECT_2, GEO.AS_WKT, ARC_TRIOMPHE);
		connection.add(SUBJECT_3, GEO.AS_WKT, NOTRE_DAME);
		connection.commit();
	}

	@After
	public void tearDown()
		throws IOException, RepositoryException
	{
		connection.close();
		repository.shutDown();
	}

	@Test
	public void testTriplesStored()
		throws Exception
	{
		// are the triples stored in the underlying sail?
		assertTrue(connection.hasStatement(SUBJECT_1, GEO.AS_WKT, EIFFEL_TOWER, false));
		assertTrue(connection.hasStatement(SUBJECT_2, GEO.AS_WKT, ARC_TRIOMPHE, false));
		assertTrue(connection.hasStatement(SUBJECT_3, GEO.AS_WKT, NOTRE_DAME, false));
	}

	@Test
	public void testDistanceQuery()
		throws RepositoryException, MalformedQueryException, QueryEvaluationException
	{
		String queryStr =
				 "prefix geo:  <"+GEO.NAMESPACE+">"
				+"prefix geof: <"+GEOF.NAMESPACE+">"
				+"select ?toUri ?to where { ?toUri geo:asWKT ?to. filter(geof:distance(?from, ?to, ?units) < ?range) }";
		TupleQuery query = connection.prepareTupleQuery(QueryLanguage.SPARQL, queryStr);
		query.setBinding("from", sail.getValueFactory().createLiteral("POINT (2.2871 48.8630)", GEO.WKT_LITERAL));
		query.setBinding("units", GEOF.UOM_METRE);
		query.setBinding("range", sail.getValueFactory().createLiteral(1500.0));

		TupleQueryResult result = query.evaluate();

		// check the results
		Map<URI,Literal> expected = new HashMap<URI,Literal>();
		expected.put(SUBJECT_1, EIFFEL_TOWER);
		expected.put(SUBJECT_2, ARC_TRIOMPHE);

		while(result.hasNext()) {
			BindingSet bindings = result.next();
			Literal location = expected.remove(bindings.getValue("toUri"));
			assertNotNull(location);
			assertEquals(location, bindings.getValue("to"));
		}
		assertTrue(expected.isEmpty());
		result.close();
	}

	@Test
	public void testComplexDistanceQuery()
		throws RepositoryException, MalformedQueryException, QueryEvaluationException
	{
		String queryStr =
				 "prefix geo:  <"+GEO.NAMESPACE+">"
				+"prefix geof: <"+GEOF.NAMESPACE+">"
				+"select ?toUri ?dist where { ?toUri geo:asWKT ?to."
				+ " bind(geof:distance(?from, ?to, ?units) as ?dist)"
				+ " filter(?dist < ?range)"
				+ " }";
		TupleQuery query = connection.prepareTupleQuery(QueryLanguage.SPARQL, queryStr);
		query.setBinding("from", sail.getValueFactory().createLiteral("POINT (2.2871 48.8630)", GEO.WKT_LITERAL));
		query.setBinding("units", GEOF.UOM_METRE);
		query.setBinding("range", sail.getValueFactory().createLiteral(1500.0));

		TupleQueryResult result = query.evaluate();

		// check the results
		Map<URI,Literal> expected = new HashMap<URI,Literal>();
		expected.put(SUBJECT_1, sail.getValueFactory().createLiteral(761.0));
		expected.put(SUBJECT_2, sail.getValueFactory().createLiteral(1334.0));

		while(result.hasNext()) {
			BindingSet bindings = result.next();
			Literal dist = expected.remove(bindings.getValue("toUri"));
			assertNotNull(dist);
			assertEquals(dist.doubleValue(), ((Literal)bindings.getValue("dist")).doubleValue(), ERROR);
		}
		assertTrue(expected.isEmpty());
		result.close();
	}
}
