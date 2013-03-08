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
package org.openrdf.rio.turtle;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.LinkedHashSet;
import java.util.Set;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.openrdf.model.Literal;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.util.ModelUtil;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
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
 * JUnit test for the Turtle parser that uses the tests that are available <a
 * href="https://dvcs.w3.org/hg/rdf/file/09a9da374a9f/rdf-turtle/">online</a>.
 */
public abstract class TurtleParserTestCase {

	/*-----------*
	 * Constants *
	 *-----------*/

	/**
	 * Base URL for tests submitted to the W3C by Andy Seaborne.
	 */
	protected static String TESTS_AFS_BASE_URL = "http://example/base/";

	/**
	 * Base directory for tests submitted to the W3C by Andy Seaborne.
	 */
	private static String TEST_AFS_FILE_BASE_PATH = "/testcases/turtle/tests-ttl/";

	/**
	 * Manifest for tests submitted to the W3C by Andy Seaborne.
	 * <p>
	 * FIXME: This is currently under an Apache License as Andy did not relicense
	 * it before submitting.
	 * 
	 * @see http 
	 *      ://lists.w3.org/Archives/Public/public-rdf-comments/2013Feb/0070.html
	 */
	private static String TEST_AFS_MANIFEST_URL = "/testcases/turtle/tests-ttl/manifest.ttl";

	/**
	 * Base URL for coverage tests.
	 * 
	 * @see
	 */
	protected static String TESTS_COVERAGE_BASE_URL = "http://example/base/";

	/**
	 * Base directory for coverage tests.
	 */
	private static String TEST_COVERAGE_FILE_BASE_PATH = "/testcases/turtle/coverage/tests/";

	/**
	 * Manifest for coverage tests.
	 */
	private static String TEST_COVERAGE_MANIFEST_URL = "/testcases/turtle/coverage/tests/manifest.ttl";

	private static String NTRIPLES_TEST_URL = "http://www.w3.org/2000/10/rdf-tests/rdfcore/ntriples/test.nt";

	private static String NTRIPLES_TEST_FILE = "/testcases/ntriples/test.nt";

	/*--------------------*
	 * Static initializer *
	 *--------------------*/

	public TestSuite createTestSuite()
		throws Exception
	{
		// Create test suite
		TestSuite suite = new TestSuite(TurtleParserTestCase.class.getName());

		// Add the N-Triples test
		String testName = "N-Triples tests";
		String inputURL = NTRIPLES_TEST_FILE;
		String outputURL = inputURL;
		String baseURL = NTRIPLES_TEST_URL;
		suite.addTest(new PositiveParserTest(testName, inputURL, outputURL, baseURL));

		// Add the manifest for AFS test cases to a repository and query it
		Repository afsRepository = new SailRepository(new MemoryStore());
		afsRepository.initialize();
		RepositoryConnection afsCon = afsRepository.getConnection();

		InputStream inputStream = this.getClass().getResourceAsStream(TEST_AFS_MANIFEST_URL);
		afsCon.add(inputStream, TESTS_AFS_BASE_URL, RDFFormat.TURTLE);

		parsePositiveSyntaxTests(suite, TEST_AFS_FILE_BASE_PATH, TESTS_AFS_BASE_URL, afsCon);
		parseNegativeSyntaxTests(suite, TEST_AFS_FILE_BASE_PATH, TESTS_AFS_BASE_URL, afsCon);
		parsePositiveEvalTests(suite, TEST_AFS_FILE_BASE_PATH, TESTS_AFS_BASE_URL, afsCon);
		parseNegativeEvalTests(suite, TEST_AFS_FILE_BASE_PATH, TESTS_AFS_BASE_URL, afsCon);

		afsCon.close();
		afsRepository.shutDown();

		// Add the manifest for coverage test cases to a repository and query it
		Repository coverageRepository = new SailRepository(new MemoryStore());
		coverageRepository.initialize();
		RepositoryConnection coverageCon = coverageRepository.getConnection();

		InputStream coverageInputStream = this.getClass().getResourceAsStream(TEST_COVERAGE_MANIFEST_URL);
		coverageCon.add(coverageInputStream, TESTS_COVERAGE_BASE_URL, RDFFormat.TURTLE);

		parsePositiveEvalTests(suite, TEST_COVERAGE_FILE_BASE_PATH, TESTS_COVERAGE_BASE_URL, coverageCon);

		coverageCon.close();
		coverageRepository.shutDown();

		return suite;
	}

	private void parsePositiveSyntaxTests(TestSuite suite, String fileBasePath, String baseUrl,
			RepositoryConnection con)
		throws Exception
	{
		StringBuilder positiveQuery = new StringBuilder();
		positiveQuery.append(" PREFIX mf:   <http://www.w3.org/2001/sw/DataAccess/tests/test-manifest#>\n");
		positiveQuery.append(" PREFIX qt:   <http://www.w3.org/2001/sw/DataAccess/tests/test-query#>\n");
		positiveQuery.append(" PREFIX rdft: <http://www.w3.org/ns/rdftest#>\n");
		positiveQuery.append(" SELECT ?test ?testName ?inputURL ?outputURL \n");
		positiveQuery.append(" WHERE { \n");
		positiveQuery.append("     ?test a rdft:TestTurtlePositiveSyntax . ");
		positiveQuery.append("     ?test mf:name ?testName . ");
		positiveQuery.append("     ?test mf:action ?inputURL . ");
		positiveQuery.append(" }");

		TupleQueryResult queryResult = con.prepareTupleQuery(QueryLanguage.SPARQL, positiveQuery.toString()).evaluate();

		// Add all positive parser tests to the test suite
		while (queryResult.hasNext()) {
			BindingSet bindingSet = queryResult.next();
			String nextTestName = ((Literal)bindingSet.getValue("testName")).getLabel();
			String nextTestFile = removeBase(((URI)bindingSet.getValue("inputURL")).toString(), baseUrl);
			String nextInputURL = fileBasePath + nextTestFile;

			String nextBaseUrl = baseUrl + nextTestFile;

			suite.addTest(new PositiveParserTest(nextTestName, nextInputURL, null, nextBaseUrl));
		}

		queryResult.close();

	}

	private void parseNegativeSyntaxTests(TestSuite suite, String fileBasePath, String baseUrl,
			RepositoryConnection con)
		throws Exception
	{
		StringBuilder negativeQuery = new StringBuilder();
		negativeQuery.append(" PREFIX mf:   <http://www.w3.org/2001/sw/DataAccess/tests/test-manifest#>\n");
		negativeQuery.append(" PREFIX qt:   <http://www.w3.org/2001/sw/DataAccess/tests/test-query#>\n");
		negativeQuery.append(" PREFIX rdft: <http://www.w3.org/ns/rdftest#>\n");
		negativeQuery.append(" SELECT ?test ?testName ?inputURL ?outputURL \n");
		negativeQuery.append(" WHERE { \n");
		negativeQuery.append("     ?test a rdft:TestTurtleNegativeSyntax . ");
		negativeQuery.append("     ?test mf:name ?testName . ");
		negativeQuery.append("     ?test mf:action ?inputURL . ");
		negativeQuery.append(" }");

		TupleQueryResult queryResult = con.prepareTupleQuery(QueryLanguage.SPARQL, negativeQuery.toString()).evaluate();

		// Add all negative parser tests to the test suite
		while (queryResult.hasNext()) {
			BindingSet bindingSet = queryResult.next();
			String nextTestName = ((Literal)bindingSet.getValue("testName")).toString();
			String nextTestFile = removeBase(((URI)bindingSet.getValue("inputURL")).toString(), baseUrl);
			String nextInputURL = fileBasePath + nextTestFile;

			String nextBaseUrl = baseUrl + nextTestFile;

			suite.addTest(new NegativeParserTest(nextTestName, nextInputURL, nextBaseUrl));
		}

		queryResult.close();

	}

	private void parsePositiveEvalTests(TestSuite suite, String fileBasePath, String baseUrl,
			RepositoryConnection con)
		throws Exception
	{
		StringBuilder positiveEvalQuery = new StringBuilder();
		positiveEvalQuery.append(" PREFIX mf:   <http://www.w3.org/2001/sw/DataAccess/tests/test-manifest#>\n");
		positiveEvalQuery.append(" PREFIX qt:   <http://www.w3.org/2001/sw/DataAccess/tests/test-query#>\n");
		positiveEvalQuery.append(" PREFIX rdft: <http://www.w3.org/ns/rdftest#>\n");
		positiveEvalQuery.append(" SELECT ?test ?testName ?inputURL ?outputURL \n");
		positiveEvalQuery.append(" WHERE { \n");
		positiveEvalQuery.append("     ?test a rdft:TestTurtleEval . ");
		positiveEvalQuery.append("     ?test mf:name ?testName . ");
		positiveEvalQuery.append("     ?test mf:action ?inputURL . ");
		positiveEvalQuery.append("     ?test mf:result ?outputURL . ");
		positiveEvalQuery.append(" }");

		TupleQueryResult queryResult = con.prepareTupleQuery(QueryLanguage.SPARQL, positiveEvalQuery.toString()).evaluate();

		// Add all positive eval tests to the test suite
		while (queryResult.hasNext()) {
			BindingSet bindingSet = queryResult.next();
			String nextTestName = ((Literal)bindingSet.getValue("testName")).getLabel();
			String nextTestFile = removeBase(((URI)bindingSet.getValue("inputURL")).toString(), baseUrl);
			String nextInputURL = fileBasePath + nextTestFile;
			String nextOutputURL = fileBasePath
					+ removeBase(((URI)bindingSet.getValue("outputURL")).toString(), baseUrl);

			String nextBaseUrl = baseUrl + nextTestFile;

			suite.addTest(new PositiveParserTest(nextTestName, nextInputURL, nextOutputURL, nextBaseUrl));
		}

		queryResult.close();
	}

	private void parseNegativeEvalTests(TestSuite suite, String fileBasePath, String baseUrl,
			RepositoryConnection con)
		throws Exception
	{
		StringBuilder negativeEvalQuery = new StringBuilder();
		negativeEvalQuery.append(" PREFIX mf:   <http://www.w3.org/2001/sw/DataAccess/tests/test-manifest#>\n");
		negativeEvalQuery.append(" PREFIX qt:   <http://www.w3.org/2001/sw/DataAccess/tests/test-query#>\n");
		negativeEvalQuery.append(" PREFIX rdft: <http://www.w3.org/ns/rdftest#>\n");
		negativeEvalQuery.append(" SELECT ?test ?testName ?inputURL ?outputURL \n");
		negativeEvalQuery.append(" WHERE { \n");
		negativeEvalQuery.append("     ?test a rdft:TestTurtleNegativeEval . ");
		negativeEvalQuery.append("     ?test mf:name ?testName . ");
		negativeEvalQuery.append("     ?test mf:action ?inputURL . ");
		negativeEvalQuery.append(" }");

		TupleQueryResult queryResult = con.prepareTupleQuery(QueryLanguage.SPARQL, negativeEvalQuery.toString()).evaluate();

		// Add all negative eval tests to the test suite
		while (queryResult.hasNext()) {
			BindingSet bindingSet = queryResult.next();
			String nextTestName = ((Literal)bindingSet.getValue("testName")).toString();
			String nextTestFile = removeBase(((URI)bindingSet.getValue("inputURL")).toString(), baseUrl);
			String nextInputURL = fileBasePath + nextTestFile;

			String nextBaseUrl = baseUrl + nextTestFile;

			suite.addTest(new NegativeParserTest(nextTestName, nextInputURL, nextBaseUrl));
		}

		queryResult.close();
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
			RDFParser turtleParser = createRDFParser();
			// turtleParser.setDatatypeHandling(RDFParser.DatatypeHandling.IGNORE);

			Set<Statement> inputCollection = new LinkedHashSet<Statement>();
			StatementCollector inputCollector = new StatementCollector(inputCollection);
			turtleParser.setRDFHandler(inputCollector);

			InputStream in = this.getClass().getResourceAsStream(inputURL);
			turtleParser.parse(in, baseURL);
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
				RDFParser turtleParser = createRDFParser();
				// turtleParser.setDatatypeHandling(RDFParser.DatatypeHandling.IGNORE);

				turtleParser.setRDFHandler(new StatementCollector());

				InputStream in = this.getClass().getResourceAsStream(inputURL);
				turtleParser.parse(in, baseURL);
				in.close();

				// System.err.println("Ignoring Turtle Negative Parser Test that does not report an expected error: "
				// + inputURL);
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
	private String removeBase(String baseUrl, String redundantBaseUrl) {
		if (baseUrl.startsWith(redundantBaseUrl)) {
			return baseUrl.substring(redundantBaseUrl.length());
		}

		return baseUrl;
	}

}
