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
package org.openrdf.rio.rdfjson;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.LinkedHashSet;
import java.util.Set;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.openrdf.model.Literal;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.util.ModelUtil;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.helpers.StatementCollector;
import org.openrdf.rio.ntriples.NTriplesParser;
import org.openrdf.sail.memory.MemoryStore;

/**
 * JUnit test for the RDFJSON Parser.
 */
public abstract class RDFJSONParserTestCase {

	/*-----------*
	 * Constants *
	 *-----------*/

	protected static String BASE_URL = "http://example/base/";

	private static String TEST_FILE_BASE_PATH = "/testcases/rdfjson/";

	private static String MANIFEST_GOOD_URL = "/testcases/rdfjson/manifest.ttl";

	/*--------------------*
	 * Static initializer *
	 *--------------------*/

	public TestSuite createTestSuite()
		throws Exception
	{
		// Create test suite
		TestSuite suite = new TestSuite(RDFJSONParserTestCase.class.getName());

		// Add the manifest for positive test cases to a repository and query it
		Repository repository = new SailRepository(new MemoryStore());
		repository.initialize();
		RepositoryConnection con = repository.getConnection();

		InputStream inputStream = this.getClass().getResourceAsStream(MANIFEST_GOOD_URL);
		con.add(inputStream, BASE_URL, RDFFormat.TURTLE);

		StringBuilder positiveQuery = new StringBuilder();
		positiveQuery.append(" PREFIX mf:   <http://www.w3.org/2001/sw/DataAccess/tests/test-manifest#>\n");
		positiveQuery.append(" PREFIX qt:   <http://www.w3.org/2001/sw/DataAccess/tests/test-query#>\n");
		positiveQuery.append(" PREFIX rdft: <http://www.w3.org/ns/rdftest#>\n");
		positiveQuery.append(" SELECT ?test ?testName ?inputURL ?outputURL \n");
		positiveQuery.append(" WHERE { \n");
		positiveQuery.append("     ?test a rdft:TestRDFJSONPositiveSyntax . ");
		positiveQuery.append("     ?test mf:name ?testName . ");
		positiveQuery.append("     ?test mf:action ?inputURL . ");
		positiveQuery.append(" }");

		TupleQueryResult queryResult = con.prepareTupleQuery(QueryLanguage.SPARQL, positiveQuery.toString()).evaluate();

		// Add all positive parser tests to the test suite
		while (queryResult.hasNext()) {
			BindingSet bindingSet = queryResult.next();
			String nextTestName = ((Literal)bindingSet.getValue("testName")).getLabel();
			String nextTestFile = removeBase(((URI)bindingSet.getValue("inputURL")).toString());
			String nextInputURL = TEST_FILE_BASE_PATH + nextTestFile;

			String nextBaseUrl = BASE_URL + nextTestFile;

			suite.addTest(new PositiveParserTest(nextTestName, nextInputURL, null, nextBaseUrl));
		}

		queryResult.close();

		StringBuilder negativeQuery = new StringBuilder();
		negativeQuery.append(" PREFIX mf:   <http://www.w3.org/2001/sw/DataAccess/tests/test-manifest#>\n");
		negativeQuery.append(" PREFIX qt:   <http://www.w3.org/2001/sw/DataAccess/tests/test-query#>\n");
		negativeQuery.append(" PREFIX rdft: <http://www.w3.org/ns/rdftest#>\n");
		negativeQuery.append(" SELECT ?test ?testName ?inputURL ?outputURL \n");
		negativeQuery.append(" WHERE { \n");
		negativeQuery.append("     ?test a rdft:TestRDFJSONNegativeSyntax . ");
		negativeQuery.append("     ?test mf:name ?testName . ");
		negativeQuery.append("     ?test mf:action ?inputURL . ");
		negativeQuery.append(" }");

		queryResult = con.prepareTupleQuery(QueryLanguage.SPARQL, negativeQuery.toString()).evaluate();

		// Add all negative parser tests to the test suite
		while (queryResult.hasNext()) {
			BindingSet bindingSet = queryResult.next();
			String nextTestName = ((Literal)bindingSet.getValue("testName")).toString();
			String nextTestFile = removeBase(((URI)bindingSet.getValue("inputURL")).toString());
			String nextInputURL = TEST_FILE_BASE_PATH + nextTestFile;

			String nextBaseUrl = BASE_URL + nextTestFile;

			suite.addTest(new NegativeParserTest(nextTestName, nextInputURL, nextBaseUrl));
		}

		queryResult.close();

		con.close();
		repository.shutDown();

		return suite;
	}

	protected abstract RDFParser createRDFParser();

	/*--------------------------------*
	 * Inner class PositiveParserTest *
	 *--------------------------------*/

	private class PositiveParserTest extends TestCase {

		/*-----------*
		 * Variables *
		 *-----------*/

		private String inputURL;

		private String outputURL;

		private String baseURL;

		/*--------------*
		 * Constructors *
		 *--------------*/

		public PositiveParserTest(String testName, String inputURL, String outputURL, String baseURL)
			throws MalformedURLException
		{
			super(testName);
			this.inputURL = inputURL;
			if (outputURL != null) {
				this.outputURL = outputURL;
			}
			this.baseURL = baseURL;
		}

		/*---------*
		 * Methods *
		 *---------*/

		@Override
		protected void runTest()
			throws Exception
		{
			// Parse input data
			RDFParser rdfjsonParser = createRDFParser();
			rdfjsonParser.setDatatypeHandling(RDFParser.DatatypeHandling.IGNORE);

			Set<Statement> inputCollection = new LinkedHashSet<Statement>();
			StatementCollector inputCollector = new StatementCollector(inputCollection);
			rdfjsonParser.setRDFHandler(inputCollector);

			InputStream in = this.getClass().getResourceAsStream(inputURL);
			rdfjsonParser.parse(in, baseURL);
			in.close();

			// Parse expected output data
			NTriplesParser ntriplesParser = new NTriplesParser();
			ntriplesParser.setDatatypeHandling(RDFParser.DatatypeHandling.IGNORE);

			Set<Statement> outputCollection = new LinkedHashSet<Statement>();
			StatementCollector outputCollector = new StatementCollector(outputCollection);
			ntriplesParser.setRDFHandler(outputCollector);

			if (outputURL != null) {
				in = this.getClass().getResourceAsStream(outputURL);
				ntriplesParser.parse(in, baseURL);
				in.close();

				// Check equality of the two models
				if (!ModelUtil.equals(inputCollection, outputCollection)) {
					System.err.println("===models not equal===");
					System.err.println("Expected: " + outputCollection);
					System.err.println("Actual  : " + inputCollection);
					System.err.println("======================");

					fail("models not equal");
				}
			}
		}

	} // end inner class PositiveParserTest

	/*--------------------------------*
	 * Inner class NegativeParserTest *
	 *--------------------------------*/

	private class NegativeParserTest extends TestCase {

		/*-----------*
		 * Variables *
		 *-----------*/

		private String inputURL;

		private String baseURL;

		/*--------------*
		 * Constructors *
		 *--------------*/

		public NegativeParserTest(String caseURI, String inputURL, String baseURL)
			throws MalformedURLException
		{
			super(caseURI);
			this.inputURL = inputURL;
			this.baseURL = baseURL;
		}

		/*---------*
		 * Methods *
		 *---------*/

		@Override
		protected void runTest() {
			try {
				// Try parsing the input; this should result in an error being
				// reported.
				RDFParser rdfjsonParser = createRDFParser();
				rdfjsonParser.setDatatypeHandling(RDFParser.DatatypeHandling.IGNORE);

				rdfjsonParser.setRDFHandler(new StatementCollector());

				InputStream in = this.getClass().getResourceAsStream(inputURL);
				rdfjsonParser.parse(in, baseURL);
				in.close();

				fail("Parser parses erroneous data without reporting errors");
			}
			catch (RDFParseException e) {
				// This is expected as the input file is incorrect RDF
			}
			catch (Exception e) {
				fail("Error: " + e.getMessage());
			}
		}

	} // end inner class NegativeParserTest

	/**
	 * @param baseUrl
	 * @return
	 */
	private String removeBase(String baseUrl) {
		if (baseUrl.startsWith(BASE_URL)) {
			return baseUrl.substring(BASE_URL.length());
		}

		return baseUrl;
	}

}
