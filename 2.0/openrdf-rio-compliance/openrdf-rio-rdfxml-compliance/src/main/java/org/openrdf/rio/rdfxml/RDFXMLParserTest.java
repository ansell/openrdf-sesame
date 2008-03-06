/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.rio.rdfxml;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedHashSet;
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
import org.openrdf.sail.memory.MemoryStore;

/**
 * JUnit test for the RDF/XML parser that uses the test manifest that is
 * available <a
 * href="http://www.w3.org/2000/10/rdf-tests/rdfcore/Manifest.rdf">online</a>.
 */
public class RDFXMLParserTest {

	/*-----------*
	 * Constants *
	 *-----------*/

	private static String W3C_TESTS_DIR = "http://www.w3.org/2000/10/rdf-tests/rdfcore/";

	private static String LOCAL_TESTS_DIR = "/testcases/rdfxml/";

	private static String W3C_MANIFEST_FILE = W3C_TESTS_DIR + "Manifest.rdf";

	private static String OPENRDF_MANIFEST_FILE = LOCAL_TESTS_DIR + "openrdf/Manifest.rdf";

	/*--------------------*
	 * Static initializer *
	 *--------------------*/

	public static Test suite()
		throws Exception
	{
		// Create an RDF repository for the manifest data
		Repository repository = new SailRepository(new MemoryStore());
		repository.initialize();
		RepositoryConnection con = repository.getConnection();

		// Add W3C's manifest
		URL w3cManifest = resolveURL(W3C_MANIFEST_FILE);
		con.add(w3cManifest, W3C_MANIFEST_FILE, RDFFormat.RDFXML);

		// Add our own manifest
		URL localManifest = resolveURL(OPENRDF_MANIFEST_FILE);
		con.add(localManifest, localManifest.toString(), RDFFormat.RDFXML);

		// Create test suite
		TestSuite suite = new TestSuite();

		// Add all positive parser tests
		String query = "select TESTCASE, INPUT, OUTPUT "
				+ "from {TESTCASE} rdf:type {test:PositiveParserTest}; "
				+ "                test:inputDocument {INPUT}; "
				+ "                test:outputDocument {OUTPUT}; "
				+ "                test:status {\"APPROVED\"} "
				+ "using namespace test = <http://www.w3.org/2000/10/rdf-tests/rdfcore/testSchema#>";
		TupleQueryResult queryResult = con.prepareTupleQuery(QueryLanguage.SERQL, query).evaluate();
		while (queryResult.hasNext()) {
			BindingSet bindingSet = queryResult.next();
			String caseURI = bindingSet.getValue("TESTCASE").toString();
			String inputURL = bindingSet.getValue("INPUT").toString();
			String outputURL = bindingSet.getValue("OUTPUT").toString();
			suite.addTest(new PositiveParserTest(caseURI, inputURL, outputURL));
		}

		queryResult.close();

		// Add all negative parser tests
		query = "select TESTCASE, INPUT " + "from {TESTCASE} rdf:type {test:NegativeParserTest}; "
				+ "                test:inputDocument {INPUT}; " + "                test:status {\"APPROVED\"} "
				+ "using namespace test = <http://www.w3.org/2000/10/rdf-tests/rdfcore/testSchema#>";
		queryResult = con.prepareTupleQuery(QueryLanguage.SERQL, query).evaluate();
		while (queryResult.hasNext()) {
			BindingSet bindingSet = queryResult.next();
			String caseURI = bindingSet.getValue("TESTCASE").toString();
			String inputURL = bindingSet.getValue("INPUT").toString();
			suite.addTest(new NegativeParserTest(caseURI, inputURL));
		}

		queryResult.close();
		con.close();
		repository.shutDown();

		return suite;
	}

	private static URL resolveURL(String urlString)
		throws MalformedURLException
	{
		if (urlString.startsWith(W3C_TESTS_DIR)) {
			// resolve to local copy
			urlString = LOCAL_TESTS_DIR + "w3c-approved/" + urlString.substring(W3C_TESTS_DIR.length());
		}

		if (urlString.startsWith("/")) {
			return RDFXMLParserTest.class.getResource(urlString);
		}
		else {
			return new URL(urlString);
		}
	}

	/*--------------------------------*
	 * Inner class PositiveParserTest *
	 *--------------------------------*/

	private static class PositiveParserTest extends TestCase {

		/*-----------*
		 * Variables *
		 *-----------*/

		private String inputURL;

		private String outputURL;

		/*--------------*
		 * Constructors *
		 *--------------*/

		public PositiveParserTest(String caseURI, String inputURL, String outputURL) {
			super(caseURI);
			this.inputURL = inputURL;
			this.outputURL = outputURL;
		}

		/*---------*
		 * Methods *
		 *---------*/

		@Override
		protected void runTest()
			throws Exception
		{
			// Parse input data
			RDFXMLParser rdfxmlParser = new RDFXMLParser();
			rdfxmlParser.setDatatypeHandling(RDFParser.DatatypeHandling.IGNORE);
			rdfxmlParser.setParseStandAloneDocuments(true);

			Set<Statement> inputCollection = new LinkedHashSet<Statement>();
			StatementCollector inputCollector = new StatementCollector(inputCollection);
			rdfxmlParser.setRDFHandler(inputCollector);

			InputStream in = resolveURL(inputURL).openStream();
			rdfxmlParser.parse(in, inputURL);
			in.close();

			// Parse expected output data
			NTriplesParser ntriplesParser = new NTriplesParser();
			ntriplesParser.setDatatypeHandling(RDFParser.DatatypeHandling.IGNORE);

			Set<Statement> outputCollection = new LinkedHashSet<Statement>();
			StatementCollector outputCollector = new StatementCollector(outputCollection);
			ntriplesParser.setRDFHandler(outputCollector);

			in = resolveURL(outputURL).openStream();
			ntriplesParser.parse(in, inputURL);
			in.close();

			// Check equality of the two models
			if (!ModelUtil.equals(inputCollection, outputCollection)) {
				StringBuilder sb = new StringBuilder(1024);
				sb.append("models not equal\n");
				sb.append("Expected:\n");
				for (Statement st : outputCollection) {
					sb.append(st).append("\n");
				}
				sb.append("Actual:\n");
				for (Statement st : inputCollection) {
					sb.append(st).append("\n");
				}

				fail(sb.toString());
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

		private String inputURL;

		/*--------------*
		 * Constructors *
		 *--------------*/

		public NegativeParserTest(String caseURI, String inputURL) {
			super(caseURI);
			this.inputURL = inputURL;
		}

		/*---------*
		 * Methods *
		 *---------*/

		@Override
		protected void runTest() {
			try {
				// Try parsing the input; this should result in an error being
				// reported.
				RDFXMLParser rdfxmlParser = new RDFXMLParser();
				rdfxmlParser.setDatatypeHandling(RDFParser.DatatypeHandling.IGNORE);
				rdfxmlParser.setParseStandAloneDocuments(true);

				rdfxmlParser.setRDFHandler(new StatementCollector());

				InputStream in = resolveURL(inputURL).openStream();
				rdfxmlParser.parse(in, inputURL);
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
