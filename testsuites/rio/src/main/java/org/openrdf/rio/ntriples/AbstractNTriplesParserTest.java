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
package org.openrdf.rio.ntriples;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.InputStream;
import java.io.StringReader;

import org.junit.Test;

import org.openrdf.model.Model;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.helpers.NTriplesParserSettings;
import org.openrdf.rio.helpers.StatementCollector;
import org.openrdf.sail.memory.MemoryStore;

/**
 * JUnit test for the N-Triples parser.
 * 
 * @author Arjohn Kampman
 */
public abstract class AbstractNTriplesParserTest {

	/*-----------*
	 * Constants *
	 *-----------*/

	private static String NTRIPLES_TEST_URL = "http://www.w3.org/2000/10/rdf-tests/rdfcore/ntriples/test.nt";

	private static String NTRIPLES_TEST_FILE = "/testcases/ntriples/test.nt";

	/*---------*
	 * Methods *
	 *---------*/

	@Test
	public void testNTriplesFile()
		throws Exception
	{
		RDFParser ntriplesParser = createRDFParser();
		ntriplesParser.setDatatypeHandling(RDFParser.DatatypeHandling.IGNORE);
		Model model = new LinkedHashModel();
		ntriplesParser.setRDFHandler(new StatementCollector(model));

		InputStream in = NTriplesParser.class.getResourceAsStream(NTRIPLES_TEST_FILE);
		try {
			ntriplesParser.parse(in, NTRIPLES_TEST_URL);
		}
		catch (RDFParseException e) {
			fail("Failed to parse N-Triples test document: " + e.getMessage());
		}
		finally {
			in.close();
		}

		assertEquals(30, model.size());
		assertEquals(28, model.subjects().size());
		assertEquals(1, model.predicates().size());
		assertEquals(23, model.objects().size());
	}

	@Test
	public void testExceptionHandlingWithDefaultSettings()
		throws Exception
	{
		String data = "invalid nt";

		RDFParser ntriplesParser = createRDFParser();
		ntriplesParser.setDatatypeHandling(RDFParser.DatatypeHandling.IGNORE);
		Model model = new LinkedHashModel();
		ntriplesParser.setRDFHandler(new StatementCollector(model));

		try {
			ntriplesParser.parse(new StringReader(data), NTRIPLES_TEST_URL);
			fail("expected RDFParseException due to invalid data");
		}
		catch (RDFParseException expected) {
			assertEquals(expected.getLineNumber(), 1);
		}
	}

	@Test
	public void testExceptionHandlingWithStopAtFirstError()
		throws Exception
	{
		String data = "invalid nt";

		RDFParser ntriplesParser = createRDFParser();
		ntriplesParser.getParserConfig().set(NTriplesParserSettings.FAIL_ON_NTRIPLES_INVALID_LINES,
				Boolean.FALSE);

		Model model = new LinkedHashModel();
		ntriplesParser.setRDFHandler(new StatementCollector(model));

		try {
			ntriplesParser.parse(new StringReader(data), NTRIPLES_TEST_URL);
			fail("expected RDFParseException due to invalid data");
		}
		catch (RDFParseException expected) {
			assertEquals(expected.getLineNumber(), 1);
		}
	}

	@Test
	public void testExceptionHandlingWithoutStopAtFirstError()
		throws Exception
	{
		String data = "invalid nt";

		RDFParser ntriplesParser = createRDFParser();
		ntriplesParser.getParserConfig().addNonFatalError(NTriplesParserSettings.FAIL_ON_NTRIPLES_INVALID_LINES);
		ntriplesParser.getParserConfig().set(NTriplesParserSettings.FAIL_ON_NTRIPLES_INVALID_LINES,
				Boolean.TRUE);

		Model model = new LinkedHashModel();
		ntriplesParser.setRDFHandler(new StatementCollector(model));

		ntriplesParser.parse(new StringReader(data), NTRIPLES_TEST_URL);

		assertEquals(0, model.size());
		assertEquals(0, model.subjects().size());
		assertEquals(0, model.predicates().size());
		assertEquals(0, model.objects().size());
	}

	@Test
	public void testExceptionHandlingParsingNTriplesIntoMemoryStore()
		throws Exception
	{
		Repository repo = new SailRepository(new MemoryStore());
		repo.initialize();
		RepositoryConnection conn = repo.getConnection();
		try {
			// Force the connection to use stop at first error
			conn.getParserConfig().set(NTriplesParserSettings.FAIL_ON_NTRIPLES_INVALID_LINES, Boolean.FALSE);

			String data = "invalid nt";
			conn.add(new StringReader(data), "http://example/", RDFFormat.NTRIPLES);
			fail("expected RDFParseException due to invalid data");
		}
		catch (RDFParseException expected) {
			;
		}
		finally {
			conn.close();
			repo.shutDown();
		}
	}

	@Test
	public void testExceptionHandlingParsingNTriplesIntoMemoryStoreWithoutStopAtFirstError()
		throws Exception
	{
		Repository repo = new SailRepository(new MemoryStore());
		repo.initialize();
		RepositoryConnection conn = repo.getConnection();
		try {
			// Force the connection to not use stop at first error
			conn.getParserConfig().addNonFatalError(NTriplesParserSettings.FAIL_ON_NTRIPLES_INVALID_LINES);
			conn.getParserConfig().set(NTriplesParserSettings.FAIL_ON_NTRIPLES_INVALID_LINES, Boolean.TRUE);

			String data = "invalid nt";
			conn.add(new StringReader(data), "http://example/", RDFFormat.NTRIPLES);

			// verify that no triples were added after the successful parse
			assertEquals(0, conn.size());
		}
		finally {
			conn.close();
			repo.shutDown();
		}
	}

	@Test
	public void testSupportedSettings()
		throws Exception
	{
		assertEquals(12, createRDFParser().getSupportedSettings().size());
	}

	protected abstract RDFParser createRDFParser();
}
