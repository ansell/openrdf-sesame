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
package org.openrdf.rio.trig;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.util.ModelUtil;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.repository.util.RepositoryUtil;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.openrdf.rio.helpers.ContextStatementCollector;
import org.openrdf.rio.helpers.ParseErrorCollector;
import org.openrdf.rio.helpers.StatementCollector;
import org.openrdf.rio.ntriples.NTriplesParser;
import org.openrdf.sail.memory.MemoryStore;

/**
 * JUnit test for the TriG parser.
 */
public abstract class TriGParserTestCase {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	/*-----------*
	 * Constants *
	 *-----------*/

	protected static String BASE_URL = "http://www.w3.org/2001/sw/DataAccess/df1/tests/";

	private static String MANIFEST_GOOD_URL = "/testcases/trig/manifest.ttl";

	private static String MANIFEST_BAD_URL = "/testcases/trig/manifest-bad.ttl";

	/*--------------------*
	 * Static initializer *
	 *--------------------*/

	public TestSuite createTestSuite()
		throws Exception
	{
		// Create test suite
		TestSuite suite = new TestSuite(TriGParserTestCase.class.getName());

		// Add the manifest for positive test cases to a repository and query it
		Repository repository = new SailRepository(new MemoryStore());
		repository.initialize();
		RepositoryConnection con = repository.getConnection();

		URL url = TriGParserTestCase.class.getResource(MANIFEST_GOOD_URL);
		con.add(url, base(url.toExternalForm()), RDFFormat.TURTLE);

		String query = "SELECT testName, inputURL, outputURL " + "FROM {} mf:name {testName}; "
				+ "        mf:result {outputURL}; " + "        mf:action {} qt:data {inputURL} "
				+ "USING NAMESPACE " + "  mf = <http://www.w3.org/2001/sw/DataAccess/tests/test-manifest#>, "
				+ "  qt = <http://www.w3.org/2001/sw/DataAccess/tests/test-query#>";

		TupleQueryResult queryResult = con.prepareTupleQuery(QueryLanguage.SERQL, query).evaluate();

		// Add all positive parser tests to the test suite
		while (queryResult.hasNext()) {
			BindingSet bindingSet = queryResult.next();
			String testName = bindingSet.getValue("testName").toString();
			String inputURL = bindingSet.getValue("inputURL").toString();
			String outputURL = bindingSet.getValue("outputURL").toString();

			String baseURL = BASE_URL + testName + ".ttl";

			suite.addTest(new PositiveParserTest(testName, inputURL, outputURL, baseURL));
		}

		queryResult.close();

		// Add the manifest for negative test cases to a repository and query it
		con.clear();
		url = TriGParserTestCase.class.getResource(MANIFEST_BAD_URL);
		con.add(url, base(url.toExternalForm()), RDFFormat.TURTLE);

		query = "SELECT testName, inputURL " + "FROM {} mf:name {testName}; "
				+ "        mf:action {} qt:data {inputURL} " + "USING NAMESPACE "
				+ "  mf = <http://www.w3.org/2001/sw/DataAccess/tests/test-manifest#>, "
				+ "  qt = <http://www.w3.org/2001/sw/DataAccess/tests/test-query#>";
		queryResult = con.prepareTupleQuery(QueryLanguage.SERQL, query).evaluate();

		// Add all negative parser tests to the test suite
		while (queryResult.hasNext()) {
			BindingSet bindingSet = queryResult.next();
			String testName = bindingSet.getValue("testName").toString();
			String inputURL = bindingSet.getValue("inputURL").toString();

			String baseURL = BASE_URL + testName + ".ttl";

			suite.addTest(new NegativeParserTest(testName, inputURL, baseURL));
		}

		queryResult.close();
		con.close();
		repository.shutDown();

		logger.warn("Found {} TriG tests to execute", suite.countTestCases());

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
			this.inputURL = url(inputURL);
			this.outputURL = url(outputURL);
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
			RDFParser trigParser = createRDFParser();
			// trigParser.setDatatypeHandling(RDFParser.DatatypeHandling.IGNORE);

			Model inputCollection = new LinkedHashModel();
			RDFHandler inputCollector;

			if (outputURL.toString().endsWith(".nq")) {
				inputCollector = new StatementCollector(inputCollection);
			}
			else {
				// Force context to null if the output is not given in N-Quads
				inputCollector = new ContextStatementCollector(inputCollection, ValueFactoryImpl.getInstance(),
						(Resource)null);
			}
			trigParser.setRDFHandler(inputCollector);
			ParseErrorCollector el = new ParseErrorCollector();
			trigParser.setParseErrorListener(el);

			InputStream in = inputURL.openStream();
			try {
				trigParser.parse(in, base(baseURL));
			}
			finally {
				in.close();
				
				if (!el.getFatalErrors().isEmpty()) {
					System.err.println("[TriG] Input file had fatal parsing errors: ");
					System.err.println(el.getFatalErrors());
				}

				if (!el.getErrors().isEmpty()) {
					System.err.println("[TriG] Input file had parsing errors: ");
					System.err.println(el.getErrors());
				}

				if (!el.getWarnings().isEmpty()) {
					System.err.println("[TriG] Input file had parsing warnings: ");
					System.err.println(el.getWarnings());
				}
			}

			Model outputCollection = new LinkedHashModel();
			RDFHandler outputCollector;

			RDFParser parser;
			if (outputURL.toString().endsWith(".nq")) {
				parser = Rio.createParser(RDFFormat.NQUADS);
				outputCollector = new StatementCollector(outputCollection);
			}
			else {
				parser = Rio.createParser(RDFFormat.NTRIPLES);
				// Force context to null if the output is not given in N-Quads
				outputCollector = new ContextStatementCollector(outputCollection, ValueFactoryImpl.getInstance(),
						(Resource)null);
			}
			// parser.setDatatypeHandling(RDFParser.DatatypeHandling.IGNORE);
			parser.setRDFHandler(outputCollector);

			el = new ParseErrorCollector();
			parser.setParseErrorListener(el);

			// Parse expected output data
			in = outputURL.openStream();
			try {
				parser.parse(in, base(baseURL));
			}
			finally {
				in.close();
				if (!el.getFatalErrors().isEmpty()) {
					System.err.println("[TriG] Expected output file had fatal parsing errors: ");
					System.err.println(el.getFatalErrors());
				}

				if (!el.getErrors().isEmpty()) {
					System.err.println("[TriG] Expected output file had parsing errors: ");
					System.err.println(el.getErrors());
				}

				if (!el.getWarnings().isEmpty()) {
					System.err.println("[TriG] Expected output file had parsing warnings: ");
					System.err.println(el.getWarnings());
				}
			}

			// Check equality of the two models
			if (!ModelUtil.equals(inputCollection, outputCollection)) {
				Collection<? extends Statement> missingStatements = RepositoryUtil.difference(outputCollection,
						inputCollection);
				Collection<? extends Statement> unexpectedStatements = RepositoryUtil.difference(inputCollection,
						outputCollection);

				System.err.println("===models not equal===");
				if (!missingStatements.isEmpty()) {
					System.err.println("Missing statements : ");
					for (Statement nextMissingStatement : missingStatements) {
						System.err.println(nextMissingStatement.toString());
					}
				}
				if (!unexpectedStatements.isEmpty()) {
					System.err.println("Unexpected statements : ");
					for (Statement nextUnexpectedStatement : unexpectedStatements) {
						System.err.println(nextUnexpectedStatement.toString());
					}
				}
				System.err.println("======================");

				fail("models not equal");
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

		private URL inputURL;

		private String baseURL;

		/*--------------*
		 * Constructors *
		 *--------------*/

		public NegativeParserTest(String caseURI, String inputURL, String baseURL)
			throws MalformedURLException
		{
			super(caseURI);
			this.inputURL = url(inputURL);
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
				turtleParser.setDatatypeHandling(RDFParser.DatatypeHandling.IGNORE);

				turtleParser.setRDFHandler(new StatementCollector());

				InputStream in = inputURL.openStream();
				turtleParser.parse(in, base(baseURL));
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

	private static URL url(String uri)
		throws MalformedURLException
	{
		if (!uri.startsWith("injar:"))
			return new URL(uri);
		int start = uri.indexOf(':') + 3;
		int end = uri.indexOf('/', start);
		String encoded = uri.substring(start, end);
		try {
			String jar = URLDecoder.decode(encoded, "UTF-8");
			return new URL("jar:" + jar + '!' + uri.substring(end));
		}
		catch (UnsupportedEncodingException e) {
			throw new AssertionError(e);
		}
	}

	private static String base(String uri) {
		if (!uri.startsWith("jar:"))
			return uri;
		int start = uri.indexOf(':') + 1;
		int end = uri.lastIndexOf('!');
		String jar = uri.substring(start, end);
		try {
			String encoded = URLEncoder.encode(jar, "UTF-8");
			return "injar://" + encoded + uri.substring(end + 1);
		}
		catch (UnsupportedEncodingException e) {
			throw new AssertionError(e);
		}
	}
}
