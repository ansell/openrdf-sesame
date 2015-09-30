/*******************************************************************************
 * Copyright (c) 2015, Eclipse Foundation, Inc. and its licensors.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * - Neither the name of the Eclipse Foundation, Inc. nor the names of its
 *   contributors may be used to endorse or promote products derived from this
 *   software without specific prior written permission. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package org.openrdf.repository.sparql;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.openrdf.model.IRI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.FOAF;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.http.HTTPMemServer;
import org.openrdf.repository.http.HTTPRepository;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.Rio;
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

	private static HTTPMemServer server;

	private HTTPRepository remoteRepository;

	private SailRepository localRepository;

	private ValueFactory f;

	private IRI bob;

	private IRI alice;

	private IRI william;

	protected static final String EX_NS = "http://example.org/";

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void startServer()
		throws Exception
	{
		server = new HTTPMemServer();

		try {
			server.start();
		}
		catch (Exception e) {
			server.stop();
			throw e;
		}
	}

	@Before
	public void setUp()
		throws Exception
	{
		remoteRepository = new HTTPRepository(HTTPMemServer.REPOSITORY_URL);
		remoteRepository.initialize();
		loadDataSet(remoteRepository, "/testdata-query/graph1.ttl");
		loadDataSet(remoteRepository, "/testdata-query/graph2.ttl");

		localRepository = new SailRepository(new MemoryStore());
		localRepository.initialize();

		loadDataSet(localRepository, "/testdata-query/defaultgraph.ttl");

		f = localRepository.getValueFactory();

		bob = f.createIRI(EX_NS, "bob");
		alice = f.createIRI(EX_NS, "alice");
		william = f.createIRI(EX_NS, "william");
	}

	protected void loadDataSet(Repository rep, String datasetFile)
		throws RDFParseException, RepositoryException, IOException
	{
		logger.debug("loading dataset...");
		InputStream dataset = SPARQLServiceEvaluationTest.class.getResourceAsStream(datasetFile);

		RepositoryConnection con = rep.getConnection();
		try {
			con.add(dataset, "",
					Rio.getParserFormatForFileName(datasetFile).orElseThrow(Rio.unsupportedFormat(datasetFile)));
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
		localRepository.shutDown();
	}

	@AfterClass
	public static void stopServer()
		throws Exception
	{
		server.stop();
		server = null;
	}

	@Test
	public void testSimpleServiceQuery()
		throws RepositoryException
	{
		StringBuilder qb = new StringBuilder();
		qb.append(" SELECT * \n");
		qb.append(" WHERE { \n");
		qb.append("     SERVICE <" + HTTPMemServer.REPOSITORY_URL + "> { \n");
		qb.append("             ?X <" + FOAF.NAME + "> ?Y \n ");
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
