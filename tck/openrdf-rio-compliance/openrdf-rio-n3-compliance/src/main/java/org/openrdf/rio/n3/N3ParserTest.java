/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.rio.n3;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.openrdf.model.Statement;
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
import org.openrdf.rio.turtle.TurtleParser;
import org.openrdf.sail.memory.MemoryStore;

/**
 * JUnit test for the N3 parser that uses the tests that are available <a
 * href="http://www.w3.org/2000/10/swap/test/n3parser.tests">online</a>.
 */
public class N3ParserTest {

	/*-----------*
	 * Constants *
	 *-----------*/

	private static String BASE_URL = "http://www.w3.org/2000/10/swap/test/";

	private static String MANIFEST_URL = "http://www.w3.org/2000/10/swap/test/n3parser.tests";

	/*--------------------*
	 * Static initializer *
	 *--------------------*/

	public static Test suite()
		throws Exception
	{
		// Create test suite
		TestSuite suite = new TestSuite();

		// Add the manifest to a repository and query it
		Repository repository = new SailRepository(new MemoryStore());
		repository.initialize();
		RepositoryConnection con = repository.getConnection();

		URL url = new URL(MANIFEST_URL);
		con.add(url, MANIFEST_URL, RDFFormat.TURTLE);

		// Add all positive parser tests to the test suite
		String query = "SELECT testURI, inputURL, outputURL "
				+ "FROM {testURI} rdf:type {n3test:PositiveParserTest}; "
				+ "               n3test:inputDocument {inputURL}; "
				+ "               n3test:outputDocument {outputURL} "
				+ "USING NAMESPACE n3test = <http://www.w3.org/2004/11/n3test#>";

		TupleQueryResult queryResult = con.prepareTupleQuery(QueryLanguage.SERQL, query).evaluate();
		while(queryResult.hasNext()) {
			BindingSet bindingSet = queryResult.next();
			String testURI = bindingSet.getValue("testURI").toString();
			String inputURL = bindingSet.getValue("inputURL").toString();
			String outputURL = bindingSet.getValue("outputURL").toString();

			suite.addTest(new PositiveParserTest(testURI, inputURL, outputURL));
		}

		queryResult.close();

		// Add all negative parser tests to the test suite
		query = "SELECT testURI, inputURL "
				+ "FROM {testURI} rdf:type {n3test:NegativeParserTest}; "
				+ "               n3test:inputDocument {inputURL} "
				+ "USING NAMESPACE n3test = <http://www.w3.org/2004/11/n3test#>";

		queryResult = con.prepareTupleQuery(QueryLanguage.SERQL, query).evaluate();

		while(queryResult.hasNext()) {
			BindingSet bindingSet = queryResult.next();
			String testURI = bindingSet.getValue("testURI").toString();
			String inputURL = bindingSet.getValue("inputURL").toString();

			suite.addTest(new NegativeParserTest(testURI, inputURL));
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

		private URL _inputURL;

		private URL _outputURL;

		/*--------------*
		 * Constructors *
		 *--------------*/

		public PositiveParserTest(String testURI, String inputURL, String outputURL)
			throws MalformedURLException
		{
			super(testURI);
			_inputURL = new URL(inputURL);
			_outputURL = new URL(outputURL);
		}

		/*---------*
		 * Methods *
		 *---------*/

		protected void runTest()
			throws Exception
		{
			// Parse input data
			TurtleParser turtleParser = new TurtleParser();
			turtleParser.setDatatypeHandling(RDFParser.DatatypeHandling.IGNORE);

			Set<Statement> inputCollection = new LinkedHashSet<Statement>();
			StatementCollector inputCollector = new StatementCollector(inputCollection);
			turtleParser.setRDFHandler(inputCollector);

			InputStream in = _inputURL.openStream();
			turtleParser.parse(in, _inputURL.toExternalForm());
			in.close();

			// Parse expected output data
			NTriplesParser ntriplesParser = new NTriplesParser();
			ntriplesParser.setDatatypeHandling(RDFParser.DatatypeHandling.IGNORE);

			Set<Statement> outputCollection = new LinkedHashSet<Statement>();
			StatementCollector outputCollector = new StatementCollector(outputCollection);
			ntriplesParser.setRDFHandler(outputCollector);

			in = _outputURL.openStream();
			ntriplesParser.parse(in, _outputURL.toExternalForm());
			in.close();

			// Check equality of the two models
			if (!ModelUtil.equals(inputCollection, outputCollection)) {
				System.err.println("===models not equal===");
				// System.err.println("Expected: " + outputCollection);
				// System.err.println("Actual : " + inputCollection);
				// System.err.println("======================");

				List<Statement> missing = new LinkedList<Statement>(outputCollection);
				missing.removeAll(inputCollection);

				List<Statement> unexpected = new LinkedList<Statement>(inputCollection);
				unexpected.removeAll(outputCollection);

				if (!missing.isEmpty()) {
					System.err.println("Missing   : " + missing);
				}
				if (!unexpected.isEmpty()) {
					System.err.println("Unexpected: " + unexpected);
				}

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

		private URL _inputURL;

		/*--------------*
		 * Constructors *
		 *--------------*/

		public NegativeParserTest(String testURI, String inputURL)
			throws MalformedURLException
		{
			super(testURI);
			_inputURL = new URL(inputURL);
		}

		/*---------*
		 * Methods *
		 *---------*/

		protected void runTest() {
			try {
				// Try parsing the input; this should result in an error being
				// reported.
				TurtleParser turtleParser = new TurtleParser();
				turtleParser.setDatatypeHandling(RDFParser.DatatypeHandling.IGNORE);

				turtleParser.setRDFHandler(new StatementCollector());

				InputStream in = _inputURL.openStream();
				turtleParser.parse(in, _inputURL.toExternalForm());
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
