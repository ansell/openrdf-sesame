/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.parser.sparql;

import java.net.URL;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.sail.memory.MemoryStore;

public class ManifestTest extends TestCase {

	static final Logger logger = LoggerFactory.getLogger(ManifestTest.class);

	private static final boolean REMOTE = false;

	public static final String HOST;

	static {
		if (REMOTE) {
			HOST = "http://www.w3.org/2001/sw/DataAccess/tests/";
		}
		else {
			HOST = ManifestTest.class.getResource("/testcases-dawg/").toString();
		}
	}

	public static final String MANIFEST_DIR = HOST + "data-r2/";

	public static final String MANIFEST_FILE = MANIFEST_DIR + "manifest-evaluation.ttl";

	public ManifestTest(String name) {
		super(name);
	}

	public static TestSuite suite()
		throws Exception
	{
		TestSuite suite = new TestSuite();

		Repository manifestRep = new SailRepository(new MemoryStore());
		manifestRep.initialize();
		RepositoryConnection con = manifestRep.getConnection();

		con.add(new URL(MANIFEST_FILE), MANIFEST_FILE, RDFFormat.TURTLE);

		String query = "SELECT DISTINCT manifestFile " + "FROM {x} rdf:first {manifestFile} "
				+ "USING NAMESPACE " + "  mf = <http://www.w3.org/2001/sw/DataAccess/tests/test-manifest#>, "
				+ "  qt = <http://www.w3.org/2001/sw/DataAccess/tests/test-query#>";

		TupleQueryResult manifestResults = con.prepareTupleQuery(QueryLanguage.SERQL, query, MANIFEST_FILE).evaluate();

		while (manifestResults.hasNext()) {
			BindingSet bindingSet = manifestResults.next();
			String manifestFile = bindingSet.getValue("manifestFile").toString();
			suite.addTest(SPARQLQueryTest.suite(manifestFile));
		}

		manifestResults.close();
		con.close();
		manifestRep.shutDown();

		logger.info("Created aggregated test suite with " + suite.countTestCases() + " test cases.");
		return suite;
	}
}
