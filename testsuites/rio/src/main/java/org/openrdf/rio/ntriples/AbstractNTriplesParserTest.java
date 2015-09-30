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
package org.openrdf.rio.ntriples;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.InputStream;
import java.io.StringReader;

import junit.framework.TestSuite;

import org.junit.Ignore;
import org.junit.Test;

import org.openrdf.model.Literal;
import org.openrdf.model.Model;
import org.openrdf.model.IRI;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.FailureMode;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.helpers.NTriplesParserSettings;
import org.openrdf.rio.helpers.StatementCollector;
import org.openrdf.rio.NegativeParserTest;
import org.openrdf.rio.PositiveParserTest;
import org.openrdf.sail.memory.MemoryStore;

/**
 * JUnit test for the N-Triples parser that uses the tests that are available <a
 * href="http://www.w3.org/2013/N-TriplesTests/">online</a>.
 */
public abstract class AbstractNTriplesParserTest {

	/*-----------*
	 * Constants *
	 *-----------*/

	/**
	 * Base directory for W3C N-Triples tests
	 */
	private static String TEST_W3C_FILE_BASE_PATH = "/testcases/ntriples/";

	private static String TEST_W3C_MANIFEST_URL = TEST_W3C_FILE_BASE_PATH + "manifest.ttl";

	private static String TEST_W3C_MANIFEST_URI_BASE = "http://www.w3.org/2013/N-TriplesTests/manifest.ttl#";

	private static String TEST_W3C_TEST_URI_BASE = "http://www.w3.org/2013/N-TriplesTests/";

	/*---------*
	 * Methods *
	 *---------*/

	public TestSuite createTestSuite()
		throws Exception
	{
		// Create test suite
		TestSuite suite = new TestSuite(this.getClass().getName());

		// Add the manifest for W3C test cases to a repository and query it
		Repository w3cRepository = new SailRepository(new MemoryStore());
		w3cRepository.initialize();
		RepositoryConnection w3cCon = w3cRepository.getConnection();

		InputStream inputStream = this.getClass().getResourceAsStream(TEST_W3C_MANIFEST_URL);
		w3cCon.add(inputStream, TEST_W3C_MANIFEST_URI_BASE, RDFFormat.TURTLE);

		parsePositiveNTriplesSyntaxTests(suite, TEST_W3C_FILE_BASE_PATH, TEST_W3C_TEST_URI_BASE, w3cCon);
		parseNegativeNTriplesSyntaxTests(suite, TEST_W3C_FILE_BASE_PATH, TEST_W3C_TEST_URI_BASE, w3cCon);

		w3cCon.close();
		w3cRepository.shutDown();

		return suite;
	}

	private void parsePositiveNTriplesSyntaxTests(TestSuite suite, String fileBasePath,
			String testLocationBaseUri, RepositoryConnection con)
		throws Exception
	{
		StringBuilder positiveQuery = new StringBuilder();
		positiveQuery.append(" PREFIX mf:   <http://www.w3.org/2001/sw/DataAccess/tests/test-manifest#>\n");
		positiveQuery.append(" PREFIX qt:   <http://www.w3.org/2001/sw/DataAccess/tests/test-query#>\n");
		positiveQuery.append(" PREFIX rdft: <http://www.w3.org/ns/rdftest#>\n");
		positiveQuery.append(" SELECT ?test ?testName ?inputURL ?outputURL \n");
		positiveQuery.append(" WHERE { \n");
		positiveQuery.append("     ?test a rdft:TestNTriplesPositiveSyntax . ");
		positiveQuery.append("     ?test mf:name ?testName . ");
		positiveQuery.append("     ?test mf:action ?inputURL . ");
		positiveQuery.append(" }");

		TupleQueryResult queryResult = con.prepareTupleQuery(QueryLanguage.SPARQL, positiveQuery.toString()).evaluate();

		// Add all positive parser tests to the test suite
		while (queryResult.hasNext()) {
			BindingSet bindingSet = queryResult.next();
			IRI nextTestUri = (IRI)bindingSet.getValue("test");
			String nextTestName = ((Literal)bindingSet.getValue("testName")).getLabel();
			String nextTestFile = removeBase(((IRI)bindingSet.getValue("inputURL")).toString(),
					testLocationBaseUri);
			String nextInputURL = fileBasePath + nextTestFile;

			String nextBaseUrl = testLocationBaseUri + nextTestFile;

			suite.addTest(new PositiveParserTest(nextTestUri, nextTestName, nextInputURL, null, nextBaseUrl,
					createRDFParser(), createRDFParser()));
		}

		queryResult.close();

	}

	private void parseNegativeNTriplesSyntaxTests(TestSuite suite, String fileBasePath,
			String testLocationBaseUri, RepositoryConnection con)
		throws Exception
	{
		StringBuilder negativeQuery = new StringBuilder();
		negativeQuery.append(" PREFIX mf:   <http://www.w3.org/2001/sw/DataAccess/tests/test-manifest#>\n");
		negativeQuery.append(" PREFIX qt:   <http://www.w3.org/2001/sw/DataAccess/tests/test-query#>\n");
		negativeQuery.append(" PREFIX rdft: <http://www.w3.org/ns/rdftest#>\n");
		negativeQuery.append(" SELECT ?test ?testName ?inputURL ?outputURL \n");
		negativeQuery.append(" WHERE { \n");
		negativeQuery.append("     ?test a rdft:TestNTriplesNegativeSyntax . ");
		negativeQuery.append("     ?test mf:name ?testName . ");
		negativeQuery.append("     ?test mf:action ?inputURL . ");
		negativeQuery.append(" }");

		TupleQueryResult queryResult = con.prepareTupleQuery(QueryLanguage.SPARQL, negativeQuery.toString()).evaluate();

		// Add all negative parser tests to the test suite
		while (queryResult.hasNext()) {
			BindingSet bindingSet = queryResult.next();
			IRI nextTestUri = (IRI)bindingSet.getValue("test");
			String nextTestName = ((Literal)bindingSet.getValue("testName")).toString();
			String nextTestFile = removeBase(((IRI)bindingSet.getValue("inputURL")).toString(),
					testLocationBaseUri);
			String nextInputURL = fileBasePath + nextTestFile;

			String nextBaseUrl = testLocationBaseUri + nextTestFile;

			suite.addTest(new NegativeParserTest(nextTestUri, nextTestName, nextInputURL, nextBaseUrl,
					createRDFParser(), FailureMode.DO_NOT_IGNORE_FAILURE));
		}

		queryResult.close();

	}

	protected abstract RDFParser createRDFParser();

	private String removeBase(String baseUrl, String redundantBaseUrl) {
		if (baseUrl.startsWith(redundantBaseUrl)) {
			return baseUrl.substring(redundantBaseUrl.length());
		}

		return baseUrl;
	}
}
