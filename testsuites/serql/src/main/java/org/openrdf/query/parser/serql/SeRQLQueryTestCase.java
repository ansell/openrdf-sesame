/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.parser.serql;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.aduna.io.IOUtil;
import info.aduna.iteration.Iterations;

import org.openrdf.model.Literal;
import org.openrdf.model.Statement;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.util.ModelUtil;
import org.openrdf.query.BindingSet;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.repository.util.RepositoryUtil;
import org.openrdf.rio.RDFFormat;
import org.openrdf.sail.NotifyingSail;
import org.openrdf.sail.SailException;
import org.openrdf.sail.inferencer.fc.DirectTypeHierarchyInferencer;
import org.openrdf.sail.inferencer.fc.ForwardChainingRDFSInferencer;
import org.openrdf.sail.memory.MemoryStore;

public abstract class SeRQLQueryTestCase extends TestCase {

	static final Logger logger = LoggerFactory.getLogger(SeRQLQueryTestCase.class);

	/*-----------*
	 * Constants *
	 *-----------*/

	private static final String MANIFEST_FILE = "/testcases/SeRQL/construct/manifest.ttl";

	/*-----------*
	 * Variables *
	 *-----------*/

	private String dataFile;

	private String queryFile;

	private String resultFile;

	private List<String> graphNames;

	private String entailment;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public interface Factory {
		Test createTest(String name, String dataFile, List<String> graphNames, String queryFile,
				String resultFile, String entailment);
	}

	/**
	 * Creates a new SeRQL Query test.
	 */
	public SeRQLQueryTestCase(String name, String dataFile, List<String> graphNames, String queryFile,
			String resultFile, String entailment)
	{
		super(name);

		this.dataFile = dataFile;
		this.queryFile = queryFile;
		this.resultFile = resultFile;
		this.graphNames = graphNames;
		this.entailment = entailment;
	}

	/*---------*
	 * Methods *
	 *---------*/

	@Override
	protected void runTest()
		throws Exception
	{
		String query = readQuery();
		Repository dataRep = createRepository(entailment);

		RepositoryConnection dataCon = dataRep.getConnection();

		// Add unnamed graph
		dataCon.add(url(dataFile), enc(dataFile), RDFFormat.forFileName(dataFile));

		// add named graphs
		for (String graphName : graphNames) {
			dataCon.add(url(graphName), enc(graphName), RDFFormat.forFileName(graphName), new URIImpl(graphName));
		}

		// Evaluate the query on the query data
		GraphQueryResult result = dataCon.prepareGraphQuery(getQueryLanguage(), query).evaluate();
		Collection<Statement> actualStatements = Iterations.addAll(result, new ArrayList<Statement>(1));
		result.close();

		dataCon.close();
		dataRep.shutDown();

		// Create a repository with the expected result data
		Repository expectedResultRep = new SailRepository(newSail());
		expectedResultRep.initialize();

		RepositoryConnection erCon = expectedResultRep.getConnection();

		erCon.add(url(resultFile), enc(resultFile), RDFFormat.forFileName(resultFile));

		Collection<Statement> expectedStatements = Iterations.addAll(erCon.getStatements(null, null, null,
				false), new ArrayList<Statement>(1));

		erCon.close();
		expectedResultRep.shutDown();

		// Compare query result to expected data
		if (!ModelUtil.equals(actualStatements, expectedStatements)) {
			// Found differences between expected and actual results
			StringBuilder message = new StringBuilder(128);

			Collection<? extends Statement> diff = RepositoryUtil.difference(actualStatements,
					expectedStatements);

			message.append("\n=======Diff: ");
			message.append(getName());
			message.append("========================\n");
			if (diff.size() != 0) {
				message.append("Unexpected statements in result: \n");
				for (Statement st : diff) {
					message.append(st.toString());
					message.append("\n");
				}
				message.append("=============");
				for (int i = 0; i < getName().length(); i++) {
					message.append("=");
				}
				message.append("========================\n");
			}

			diff = RepositoryUtil.difference(expectedStatements, actualStatements);
			if (diff.size() != 0) {
				message.append("Statements missing in result: \n");
				for (Statement st : diff) {
					message.append(st.toString());
					message.append("\n");
				}
				message.append("=============");
				for (int i = 0; i < getName().length(); i++) {
					message.append("=");
				}
				message.append("========================\n");
			}

			logger.error(message.toString());
			fail(message.toString());
		}

	}

	protected Repository createRepository(String entailment)
			throws SailException, RepositoryException {
		Repository dataRep;
		if ("RDF".equals(entailment)) {
			dataRep = newRepository();
		} else {
			dataRep = newRepository(entailment);
		}
		dataRep.initialize();
		return dataRep;
	}

	protected Repository newRepository() throws SailException {
		return new SailRepository(newSail());
	}

	protected Repository newRepository(String entailment) throws SailException {
		return new SailRepository(createSail(entailment));
	}

	protected NotifyingSail createSail(String entailment) throws SailException {
		NotifyingSail sail = newSail();

		if ("RDF".equals(entailment)) {
			// do not add inferencers
		}
		else if ("RDFS".equals(entailment)) {
			sail = new ForwardChainingRDFSInferencer(sail);
		}
		else if ("RDFS-VP".equals(entailment)) {
			sail = new ForwardChainingRDFSInferencer(sail);
			sail = new DirectTypeHierarchyInferencer(sail);
		}
		else {
			sail.shutDown();
			fail("Invalid value for entailment level:" + entailment);
		}
		return sail;
	}

	protected abstract NotifyingSail newSail();

	private String readQuery()
		throws IOException
	{
		InputStream stream = url(queryFile).openStream();
		try {
			return IOUtil.readString(new InputStreamReader(stream, "UTF-8"));
		}
		finally {
			stream.close();
		}
	}

	/*--------------*
	 * Test methods *
	 *--------------*/

	public static Test suite(Factory factory)
		throws Exception
	{
		TestSuite suite = new TestSuite();

		// Read manifest and create declared test cases
		Repository manifestRep = new SailRepository(new MemoryStore());
		manifestRep.initialize();

		RepositoryConnection con = manifestRep.getConnection();

		URL manifestURL = SeRQLQueryTestCase.class.getResource(MANIFEST_FILE);
		RDFFormat format = RDFFormat.forFileName(MANIFEST_FILE, RDFFormat.TURTLE);
		con.add(manifestURL, enc(manifestURL.toExternalForm()), format);

		String query = "SELECT testName, entailment, input, query, result " + "FROM {} mf:name {testName};"
				+ "        mf:result {result}; " + "        tck:entailment {entailment}; "
				+ "        mf:action {} qt:query {query}; " + "                     qt:data {input} "
				+ "USING NAMESPACE " + "  mf = <http://www.w3.org/2001/sw/DataAccess/tests/test-manifest#>, "
				+ "  qt = <http://www.w3.org/2001/sw/DataAccess/tests/test-query#>, "
				+ "  tck = <urn:openrdf.org:sesame:tests#> ";

		TupleQueryResult tests = con.prepareTupleQuery(QueryLanguage.SERQL, query).evaluate();
		while (tests.hasNext()) {
			BindingSet testBindings = tests.next();
			String testName = ((Literal)testBindings.getValue("testName")).getLabel();
			String inputFile = testBindings.getValue("input").toString();
			String queryFile = testBindings.getValue("query").toString();
			String resultFile = testBindings.getValue("result").toString();
			String entailment = ((Literal)testBindings.getValue("entailment")).getLabel();

			query = "SELECT graph " + "FROM {} mf:name {testName}; "
					+ "        mf:action {} qt:graphData {graph} " + "WHERE testName = \""
					+ SeRQLUtil.encodeString(testName) + "\" " + "USING NAMESPACE"
					+ "  mf = <http://www.w3.org/2001/sw/DataAccess/tests/test-manifest#>,"
					+ "  qt = <http://www.w3.org/2001/sw/DataAccess/tests/test-query#>";

			List<String> graphNames = new ArrayList<String>();

			TupleQueryResult graphs = con.prepareTupleQuery(QueryLanguage.SERQL, query).evaluate();
			while (graphs.hasNext()) {
				BindingSet graphBindings = graphs.next();
				graphNames.add(graphBindings.getValue("graph").toString());
			}
			graphs.close();

			if (testName.startsWith("test-029:")) {
				logger.error("test-029 SKIPPED in {}", SeRQLQueryTestCase.class.getName());
				continue;
			}
			suite.addTest(factory.createTest(testName, inputFile, graphNames, queryFile, resultFile, entailment));
		}

		tests.close();
		con.close();
		manifestRep.shutDown();

		return suite;
	}

	private static URL url(String uri)
			throws IOException {
		return new URL(dec(uri));
	}

	private static String enc(String uri)
			throws UnsupportedEncodingException {
		if (!uri.startsWith("jar:"))
			return uri;
		int start = uri.indexOf(':') + 1;
		int end = uri.lastIndexOf('!');
		String jar = uri.substring(start, end);
		String encoded = URLEncoder.encode(jar, "UTF-8");
		return "injar://" + encoded + uri.substring(end + 1);
	}

	private static String dec(String uri)
			throws UnsupportedEncodingException {
		if (!uri.startsWith("injar:"))
			return uri;
		int start = uri.indexOf(':') + 3;
		int end = uri.indexOf('/', start);
		String encoded = uri.substring(start, end);
		String jar = URLDecoder.decode(encoded, "UTF-8");
		return "jar:" + jar + '!' + uri.substring(end);
	}

	protected abstract QueryLanguage getQueryLanguage();
}
