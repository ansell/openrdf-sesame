/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2011.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.sparql;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.parser.sparql.FOAF;
import org.openrdf.query.parser.sparql.SPARQLUpdateTest;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.http.HTTPMemServer;
import org.openrdf.repository.http.HTTPRepository;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.sail.memory.MemoryStore;

/**
 * Test suite for evaluation of SPARQL queries involving SERVICE clauses. The
 * test suite starts up an embedded Jetty server running Sesame, which functions
 * as the SPARQL endpoint to test against.
 * 
 * @author Jeen Broekstra
 */
public class SPARQLServiceEvaluationTest {

	static final Logger logger = LoggerFactory.getLogger(SPARQLServiceEvaluationTest.class);

	private HTTPMemServer server;

	private HTTPRepository remoteRepository;

	private SailRepository localRepository;

	private ValueFactory f;

	private URI bob;

	private URI alice;

	private URI william;

	protected static final String EX_NS = "http://example.org/";

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp()
		throws Exception
	{
		server = new HTTPMemServer();

		try {
			server.start();

			remoteRepository = new HTTPRepository(HTTPMemServer.REPOSITORY_URL);
			remoteRepository.initialize();
			loadDataSet(remoteRepository, "/testdata-query/graph1.ttl");
			loadDataSet(remoteRepository, "/testdata-query/graph2.ttl");

			localRepository = new SailRepository(new MemoryStore());
			localRepository.initialize();

			loadDataSet(localRepository, "/testdata-query/defaultgraph.ttl");
			
			f = localRepository.getValueFactory();

			bob = f.createURI(EX_NS, "bob");
			alice = f.createURI(EX_NS, "alice");
			william = f.createURI(EX_NS, "william");
			
		}
		catch (Exception e) {
			server.stop();
			throw e;
		}
	}

	protected void loadDataSet(Repository rep, String datasetFile)
		throws RDFParseException, RepositoryException, IOException
	{
		logger.debug("loading dataset...");
		InputStream dataset = SPARQLServiceEvaluationTest.class.getResourceAsStream(datasetFile);

		RepositoryConnection con = rep.getConnection();
		try {
			con.add(dataset, "", RDFFormat.forFileName(datasetFile));
		}
		finally {
			dataset.close();
			con.close();
		}
		logger.debug("dataset loaded.");
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown()
		throws Exception
	{
		try {
			localRepository.shutDown();
		}
		finally {
			server.stop();
		}
	}

	@Test
	public void testSimpleServiceQuery()
		throws RepositoryException
	{
		StringBuilder qb = new StringBuilder();
		qb.append(" SELECT * \n"); 
		qb.append(" WHERE { \n");
		qb.append("     SERVICE <" + HTTPMemServer.REPOSITORY_URL + "> { \n");
		qb.append("             ?X <"	+ FOAF.NAME + "> ?Y \n ");
		qb.append("     } \n ");
		qb.append("     ?X a <" + FOAF.PERSON + "> . \n");
		qb.append(" } \n");

		RepositoryConnection conn = localRepository.getConnection();
		try {
			TupleQuery tq = conn.prepareTupleQuery(QueryLanguage.SPARQL, qb.toString());

			TupleQueryResult tqr = tq.evaluate();

			assertNotNull(tqr);
			assertTrue(tqr.hasNext());

			int count = 0;
			while (tqr.hasNext()) {
				BindingSet bs = tqr.next();
				count++;

				Value x = bs.getValue("X");
				Value y = bs.getValue("Y");

				assertFalse(william.equals(x));

				assertTrue(bob.equals(x) || alice.equals(x));
				if (bob.equals(x)) {
					f.createLiteral("Bob").equals(y);
				}
				else if (alice.equals(x)) {
					f.createLiteral("Alice").equals(y);
				}
			}

			assertEquals(2, count);

		}
		catch (MalformedQueryException e) {
			fail(e.getMessage());
		}
		catch (QueryEvaluationException e) {
			fail(e.getMessage());
		}
		finally {
			conn.close();
		}
	}
}
