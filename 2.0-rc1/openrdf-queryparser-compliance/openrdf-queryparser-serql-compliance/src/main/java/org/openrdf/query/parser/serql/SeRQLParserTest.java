/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.parser.serql;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.aduna.io.IOUtil;

import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.parser.QueryParser;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.sail.memory.MemoryStore;

public class SeRQLParserTest extends TestCase {

	static final Logger logger = LoggerFactory.getLogger(SeRQLParserTest.class);

	/*-----------*
	 * Constants *
	 *-----------*/

	private static final String MANIFEST_FILE = "/testcases/SeRQL/syntax/manifest.ttl";

	/* Variables */

	private String queryFile;

	private Value result;

	/* constants */

	private static String MFX = "http://www.openrdf.org/test-manifest-extensions#";

	private static URI MFX_CORRECT = new URIImpl(MFX + "Correct");

	private static URI MFX_PARSE_ERROR = new URIImpl(MFX + "ParseError");

	/* Constructors */

	/**
	 * Creates a new SeRQL Parser test.
	 */
	public SeRQLParserTest(String name, String queryFile, Value result) {
		super(name);

		this.queryFile = queryFile;

		if (!(MFX_CORRECT.equals(result) || MFX_PARSE_ERROR.equals(result))) {
			throw new IllegalArgumentException("unknown result type: " + result);
		}
		this.result = result;
	}

	/*---------*
	 * Methods *
	 *---------*/

	@Override
	protected void runTest()
		throws Exception
	{
		// Read query from file
		InputStream stream = new URL(queryFile).openStream();
		String query = IOUtil.readString(new InputStreamReader(stream, "UTF-8"));
		stream.close();

		try {
			QueryParser parser = new SeRQLParser();
			parser.parseQuery(query, null);
			if (MFX_PARSE_ERROR.equals(result)) {
				fail("Negative syntax test failed. Malformed query caused no error.");
			}
		}
		catch (MalformedQueryException e) {
			if (MFX_CORRECT.equals(result)) {
				fail("Positive syntax test failed: " + e.getMessage());
			}
			else {
				return;
			}
		}
	}

	/*--------------*
	 * Test methods *
	 *--------------*/

	public static Test suite()
		throws Exception
	{
		TestSuite suite = new TestSuite();
		suite.setName("SeRQL Syntax Tests");

		TestSuite positiveTests = new TestSuite();
		positiveTests.setName("Positive Syntax Tests");

		TestSuite negativeTests = new TestSuite();
		negativeTests.setName("Negative Syntax Tests");

		// Read manifest and create declared test cases
		Repository manifestRep = new SailRepository(new MemoryStore());
		manifestRep.initialize();
		RepositoryConnection con = manifestRep.getConnection();

		URL manifestURL = SeRQLParserTest.class.getResource(MANIFEST_FILE);
		con.add(manifestURL, null, RDFFormat.forFileName(MANIFEST_FILE));

		String query = "SELECT testName, query, result " + "FROM {} mf:name {testName}; "
				+ "        mf:action {query}; " + "        mf:result {result} " + "USING NAMESPACE "
				+ "  mf = <http://www.w3.org/2001/sw/DataAccess/tests/test-manifest#>, "
				+ "  mfx = <http://www.openrdf.org/test-manifest-extensions#>, "
				+ "  qt = <http://www.w3.org/2001/sw/DataAccess/tests/test-query#>";

		TupleQueryResult tests = con.prepareTupleQuery(QueryLanguage.SERQL, query).evaluate();
		while (tests.hasNext()) {
			BindingSet testBindings = tests.next();
			String testName = testBindings.getValue("testName").toString();
			String queryFile = testBindings.getValue("query").toString();
			Value result = testBindings.getValue("result");
			if (MFX_CORRECT.equals(result)) {
				positiveTests.addTest(new SeRQLParserTest(testName, queryFile, result));
			}
			else if (MFX_PARSE_ERROR.equals(result)) {
				negativeTests.addTest(new SeRQLParserTest(testName, queryFile, result));
			}
			else {
				logger.warn("Unexpected result value for test \"" + testName + "\": " + result);
			}
		}

		tests.close();
		con.close();
		manifestRep.shutDown();

		suite.addTest(positiveTests);
		suite.addTest(negativeTests);
		return suite;
	}
}
