/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.parser.sparql;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import junit.framework.TestSuite;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.aduna.io.FileUtil;

import org.openrdf.OpenRDFUtil;
import org.openrdf.model.Resource;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.StoreException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.repository.util.RDFInserter;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.turtle.TurtleParser;
import org.openrdf.sail.memory.MemoryStore;

public class ManifestTest {

	static final Logger logger = LoggerFactory.getLogger(ManifestTest.class);

	private static final boolean REMOTE = false;

	public static final String MANIFEST_FILE;

	static {
		if (REMOTE) {
			MANIFEST_FILE = "http://www.w3.org/2001/sw/DataAccess/tests/data-r2/manifest-evaluation.ttl";
		} else {
			URL url = ManifestTest.class.getResource(
					"/testcases-dawg/data-r2/manifest-evaluation.ttl");
			if ("jar".equals(url.getProtocol())) {
				try {
					File destDir = FileUtil.createTempDir("sparql");
					JarURLConnection con = (JarURLConnection)url.openConnection();
					JarFile jar = con.getJarFile();
					Enumeration<JarEntry> entries = jar.entries();
					while (entries.hasMoreElements()) {
						JarEntry file = entries.nextElement();
						File f = new File(destDir + File.separator + file.getName());
						if (file.isDirectory()) {
							f.mkdir();
							continue;
						}
						InputStream is = jar.getInputStream(file);
						FileOutputStream fos = new FileOutputStream(f);
						while (is.available() > 0) {
							fos.write(is.read());
						}
						fos.close();
						is.close();
					}
					File localFile = new File(destDir, con.getEntryName());
					destDir.deleteOnExit();
					MANIFEST_FILE = localFile.toURI().toURL().toString();
				}
				catch (IOException e) {
					throw new AssertionError(e);
				}
			} else {
				MANIFEST_FILE = url.toString();
			}
		}
	}

	public static TestSuite suite(SPARQLQueryTest.Factory factory)
			throws Exception {
		TestSuite suite = new TestSuite(factory.getClass().getName());

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
			StoreException, RDFParseException {
		if (baseURI == null) {
			baseURI = url.toExternalForm();
		}

		InputStream in = url.openStream();

		try {
			OpenRDFUtil.verifyContextNotNull(contexts);
			final ValueFactory vf = con.getValueFactory();
			RDFParser rdfParser = new TurtleParser();
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
				// RDFInserter only throws wrapped StoreExceptions
				throw (StoreException) e.getCause();
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
