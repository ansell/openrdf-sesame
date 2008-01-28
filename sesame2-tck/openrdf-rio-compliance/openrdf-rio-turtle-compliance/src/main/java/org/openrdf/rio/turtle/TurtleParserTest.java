/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.rio.turtle;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedHashSet;
import java.util.Set;

import junit.framework.Test;
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
 * JUnit test for the Turtle parser that uses the tests that are available <a
 * href="http://cvs.ilrt.org/cvsweb/redland/raptor/tests/turtle/">online</a>.
 */
public class TurtleParserTest {

	/*-----------*
	 * Constants *
	 *-----------*/

	protected static String BASE_URL = "http://www.w3.org/2001/sw/DataAccess/df1/tests/";

	private static String MANIFEST_GOOD_URL = "/testcases/turtle/manifest.ttl";

	private static String MANIFEST_BAD_URL = "/testcases/turtle/manifest-bad.ttl";

	private static String NTRIPLES_TEST_URL = "http://www.w3.org/2000/10/rdf-tests/rdfcore/ntriples/test.nt";

	private static String NTRIPLES_TEST_FILE = "/testcases/ntriples/test.nt";

	/*--------------------*
	 * Static initializer *
	 *--------------------*/

	public static Test suite()
		throws Exception
	{
		// Create test suite
		TestSuite suite = new TestSuite();

		// Add the N-Triples test
		String testName = "N-Triples tests";
		URL url = TurtleParserTest.class.getResource(NTRIPLES_TEST_FILE);
		String inputURL = url.toExternalForm();
		String outputURL = inputURL;
		String baseURL = NTRIPLES_TEST_URL;
		suite.addTest(new PositiveParserTest(testName, inputURL, outputURL, baseURL));

		// Add the manifest for positive test cases to a repository and query it
		Repository repository = new SailRepository(new MemoryStore());
		repository.initialize();
		RepositoryConnection con = repository.getConnection();

		url = TurtleParserTest.class.getResource(MANIFEST_GOOD_URL);
		con.add(url, url.toExternalForm(), RDFFormat.TURTLE);

		String query = "SELECT testName, inputURL, outputURL " + "FROM {} mf:name {testName}; "
				+ "        mf:result {outputURL}; " + "        mf:action {} qt:data {inputURL} "
				+ "USING NAMESPACE " + "  mf = <http://www.w3.org/2001/sw/DataAccess/tests/test-manifest#>, "
				+ "  qt = <http://www.w3.org/2001/sw/DataAccess/tests/test-query#>";

		TupleQueryResult queryResult = con.prepareTupleQuery(QueryLanguage.SERQL, query).evaluate();

		// Add all positive parser tests to the test suite
		while (queryResult.hasNext()) {
			BindingSet bindingSet = queryResult.next();
			testName = ((Literal)bindingSet.getValue("testName")).getLabel();
			inputURL = ((URI)bindingSet.getValue("inputURL")).toString();
			outputURL = ((URI)bindingSet.getValue("outputURL")).toString();

			baseURL = BASE_URL + testName + ".ttl";

			suite.addTest(new PositiveParserTest(testName, inputURL, outputURL, baseURL));
		}

		queryResult.close();

		// Add the manifest for negative test cases to a repository and query it
		con.clear();
		url = TurtleParserTest.class.getResource(MANIFEST_BAD_URL);
		con.add(url, url.toExternalForm(), RDFFormat.TURTLE);

		query = "SELECT testName, inputURL " + "FROM {} mf:name {testName}; "
				+ "        mf:action {} qt:data {inputURL} " + "USING NAMESPACE "
				+ "  mf = <http://www.w3.org/2001/sw/DataAccess/tests/test-manifest#>, "
				+ "  qt = <http://www.w3.org/2001/sw/DataAccess/tests/test-query#>";
		queryResult = con.prepareTupleQuery(QueryLanguage.SERQL, query).evaluate();

		// Add all negative parser tests to the test suite
		while (queryResult.hasNext()) {
			BindingSet bindingSet = queryResult.next();
			testName = ((Literal)bindingSet.getValue("testName")).toString();
			inputURL = ((URI)bindingSet.getValue("inputURL")).toString();

			baseURL = BASE_URL + testName + ".ttl";

			suite.addTest(new NegativeParserTest(testName, inputURL, baseURL));
		}

		queryResult.close();
		con.close();
		repository.shutDown();

		return suite;
	}

	/*--------------------------------*
	 * Inner class PositiveParserTest *
	 *--------------------------------*/

	private static class PositiveParserTest extends TestCase {

		/*-----------*
		 * Variables *
		 *-----------*/

		private URL inputURL;

		private URL outputURL;

		private String baseURL;

		/*--------------*
		 * Constructors *
		 *--------------*/

		public PositiveParserTest(String testName, String inputURL, String outputURL, String baseURL)
			throws MalformedURLException
		{
			super(testName);
			this.inputURL = new URL(inputURL);
			this.outputURL = new URL(outputURL);
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
			TurtleParser turtleParser = new TurtleParser();
			turtleParser.setDatatypeHandling(RDFParser.DatatypeHandling.IGNORE);

			Set<Statement> inputCollection = new LinkedHashSet<Statement>();
			StatementCollector inputCollector = new StatementCollector(inputCollection);
			turtleParser.setRDFHandler(inputCollector);

			InputStream in = inputURL.openStream();
			turtleParser.parse(in, baseURL);
			in.close();

			// Parse expected output data
			NTriplesParser ntriplesParser = new NTriplesParser();
			ntriplesParser.setDatatypeHandling(RDFParser.DatatypeHandling.IGNORE);

			Set<Statement> outputCollection = new LinkedHashSet<Statement>();
			StatementCollector outputCollector = new StatementCollector(outputCollection);
			ntriplesParser.setRDFHandler(outputCollector);

			in = outputURL.openStream();
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

	} // end inner class PositiveParserTest

	/*--------------------------------*
	 * Inner class NegativeParserTest *
	 *--------------------------------*/

	private static class NegativeParserTest extends TestCase {

		/*-----------*
		 * Variables *
		 *-----------*/

		private URL inputURL;

		private String baseURL;

		/*--------------*
		 * Constructors *
		 *--------------*/

		public NegativeParserTest(String caseURI, String inputURL, String baseURL)
			throws MalformedURLException
		{
			super(caseURI);
			this.inputURL = new URL(inputURL);
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
				TurtleParser turtleParser = new TurtleParser();
				turtleParser.setDatatypeHandling(RDFParser.DatatypeHandling.IGNORE);

				turtleParser.setRDFHandler(new StatementCollector());

				InputStream in = inputURL.openStream();
				turtleParser.parse(in, baseURL);
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
}
