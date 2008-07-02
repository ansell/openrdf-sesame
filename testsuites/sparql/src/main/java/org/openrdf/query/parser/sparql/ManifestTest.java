/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.parser.sparql;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import junit.framework.TestSuite;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.aduna.net.ParsedURI;

import org.openrdf.OpenRDFUtil;
import org.openrdf.model.Resource;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.repository.util.RDFInserter;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.turtle.TurtleParser;
import org.openrdf.sail.memory.MemoryStore;

public class ManifestTest {

	private static final String JAR_FILE = "jar:file:";

	static final Logger logger = LoggerFactory.getLogger(ManifestTest.class);

	private static final boolean REMOTE = false;

	public static final String MANIFEST_FILE;

	static {
		if (REMOTE) {
			MANIFEST_FILE = "http://www.w3.org/2001/sw/DataAccess/tests/data-r2/manifest-evaluation.ttl";
		} else {
			MANIFEST_FILE = ManifestTest.class.getResource(
					"/testcases-dawg/data-r2/manifest-evaluation.ttl")
					.toString();
		}
	}

	public static TestSuite suite(SPARQLQueryTest.Factory factory)
			throws Exception {
		TestSuite suite = new TestSuite();

		Repository manifestRep = new SailRepository(new MemoryStore());
		manifestRep.initialize();
		RepositoryConnection con = manifestRep.getConnection();

		addTurtle(con, new URL(MANIFEST_FILE), MANIFEST_FILE);

		String query = "SELECT DISTINCT manifestFile "
				+ "FROM {x} rdf:first {manifestFile} "
				+ "USING NAMESPACE "
				+ "  mf = <http://www.w3.org/2001/sw/DataAccess/tests/test-manifest#>, "
				+ "  qt = <http://www.w3.org/2001/sw/DataAccess/tests/test-query#>";

		TupleQueryResult manifestResults = con.prepareTupleQuery(
				QueryLanguage.SERQL, query, MANIFEST_FILE).evaluate();

		while (manifestResults.hasNext()) {
			BindingSet bindingSet = manifestResults.next();
			String manifestFile = bindingSet.getValue("manifestFile")
					.toString();
			System.out.println();
			System.out.println(MANIFEST_FILE);
			System.out.println(manifestFile);
			System.out.println();
			suite.addTest(SPARQLQueryTest.suite(manifestFile, factory));
		}

		manifestResults.close();
		con.close();
		manifestRep.shutDown();

		logger.info("Created aggregated test suite with "
				+ suite.countTestCases() + " test cases.");
		return suite;
	}

	static void addTurtle(RepositoryConnection con, URL url,
			String baseURI, Resource... contexts) throws IOException,
			RepositoryException, RDFParseException {
		if (baseURI == null) {
			baseURI = url.toExternalForm();
		}

		InputStream in = url.openStream();

		try {
			OpenRDFUtil.verifyContextNotNull(contexts);
			final ValueFactory vf = con.getRepository().getValueFactory();
			RDFParser rdfParser = new TurtleParser() {
				@Override
				protected void setBaseURI(final String uriSpec) {
					ParsedURI baseURI = new ParsedURI(uriSpec) {
						private boolean jarFile = uriSpec.startsWith(JAR_FILE);
						private int idx = uriSpec.indexOf('!') + 1;
						private ParsedURI file = new ParsedURI("file:"
								+ uriSpec.substring(idx));

						@Override
						public ParsedURI resolve(ParsedURI uri) {
							if (jarFile) {
								String path = file.resolve(uri).toString()
										.substring(5);
								String c = uriSpec.substring(0, idx) + path;
								return new ParsedURI(c);
							}
							return super.resolve(uri);
						}
					};
					baseURI.normalize();
					setBaseURI(baseURI);
				}
			};
			rdfParser.setValueFactory(vf);

			rdfParser.setVerifyData(false);
			rdfParser.setStopAtFirstError(true);
			rdfParser.setDatatypeHandling(RDFParser.DatatypeHandling.IGNORE);

			RDFInserter rdfInserter = new RDFInserter(con);
			rdfInserter.enforceContext(contexts);
			rdfParser.setRDFHandler(rdfInserter);

			boolean autoCommit = con.isAutoCommit();
			con.setAutoCommit(false);

			try {
				rdfParser.parse(in, baseURI);
			} catch (RDFHandlerException e) {
				if (autoCommit) {
					con.rollback();
				}
				// RDFInserter only throws wrapped RepositoryExceptions
				throw (RepositoryException) e.getCause();
			} catch (RuntimeException e) {
				if (autoCommit) {
					con.rollback();
				}
				throw e;
			} finally {
				con.setAutoCommit(autoCommit);
			}
		} finally {
			in.close();
		}
	}
}
