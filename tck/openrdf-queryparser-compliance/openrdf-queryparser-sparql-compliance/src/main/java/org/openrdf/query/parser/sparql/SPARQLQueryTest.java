/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.parser.sparql;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.aduna.io.IOUtil;
import info.aduna.iteration.Iterations;
import info.aduna.text.StringUtil;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.util.ModelUtil;
import org.openrdf.query.BindingSet;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.Dataset;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.Query;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.QueryResultUtil;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.dawg.DAWGTestResultSetUtil;
import org.openrdf.query.impl.DatasetImpl;
import org.openrdf.query.impl.MutableTupleQueryResult;
import org.openrdf.query.resultio.BooleanQueryResultFormat;
import org.openrdf.query.resultio.BooleanQueryResultParserRegistry;
import org.openrdf.query.resultio.QueryResultIO;
import org.openrdf.query.resultio.TupleQueryResultFormat;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.dataset.DatasetRepository;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.repository.util.RDFInserter;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.openrdf.rio.RDFParser.DatatypeHandling;
import org.openrdf.rio.helpers.StatementCollector;
import org.openrdf.sail.memory.MemoryStore;

public class SPARQLQueryTest extends TestCase {

	/*-----------*
	 * Constants *
	 *-----------*/

	static final Logger logger = LoggerFactory.getLogger(SPARQLQueryTest.class);

	protected final String testURI;

	protected final String queryFileURL;

	protected final String resultFileURL;

	protected final Dataset dataset;

	/*-----------*
	 * Variables *
	 *-----------*/

	private Repository dataRep;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public SPARQLQueryTest(String testURI, String name, String queryFileURL, String resultFileURL,
			Dataset dataSet)
	{
		super(name);

		this.testURI = testURI;
		this.queryFileURL = queryFileURL;
		this.resultFileURL = resultFileURL;
		this.dataset = dataSet;
	}

	/*---------*
	 * Methods *
	 *---------*/

	@Override
	protected void setUp()
		throws Exception
	{
		dataRep = new DatasetRepository(new SailRepository(new MemoryStore()));
		dataRep.initialize();

		if (dataset != null) {
			uploadDataset(dataset);
		}
	}

	@Override
	protected void tearDown()
		throws Exception
	{
		dataRep.shutDown();
	}

	@Override
	protected void runTest()
		throws Exception
	{
		try {
			RepositoryConnection con = dataRep.getConnection();
			try {
				String queryString = readQueryString();
				Query query = con.prepareQuery(QueryLanguage.SPARQL, queryString, queryFileURL);
				if (dataset != null) {
					query.setDataset(dataset);
				}

				if (query instanceof TupleQuery) {
					TupleQueryResult queryResult = ((TupleQuery)query).evaluate();

					TupleQueryResult expectedResult = readExpectedTupleQueryResult();
					compareTupleQueryResults(queryResult, expectedResult);

					// Graph queryGraph = RepositoryUtil.asGraph(queryResult);
					// Graph expectedGraph = readExpectedTupleQueryResult();
					// compareGraphs(queryGraph, expectedGraph);
				}
				else if (query instanceof GraphQuery) {
					GraphQueryResult gqr = ((GraphQuery)query).evaluate();
					Set<Statement> queryResult = Iterations.asSet(gqr);

					Set<Statement> expectedResult = readExpectedGraphQueryResult();

					compareGraphs(queryResult, expectedResult);
				}
				else if (query instanceof BooleanQuery) {
					boolean queryResult = ((BooleanQuery)query).evaluate();
					boolean expectedResult = readExpectedBooleanQueryResult();
					assertEquals(expectedResult, queryResult);
				}
				else {
					throw new RuntimeException("Unexpected query type: " + query.getClass());
				}
			}
			finally {
				con.close();
			}
		}
		finally {
			dataRep.shutDown();
		}
	}

	private void compareTupleQueryResults(TupleQueryResult queryResult, TupleQueryResult expectedResult)
		throws Exception
	{
		// Create MutableTupleQueryResult to be able to re-iterate over the
		// results
		MutableTupleQueryResult queryResultTable = new MutableTupleQueryResult(queryResult);
		MutableTupleQueryResult expectedResultTable = new MutableTupleQueryResult(expectedResult);

		if (!QueryResultUtil.equals(queryResultTable, expectedResultTable)) {
			queryResultTable.beforeFirst();
			expectedResultTable.beforeFirst();

			/*
			 * StringBuilder message = new StringBuilder(128);
			 * message.append("\n============ "); message.append(getName());
			 * message.append(" =======================\n");
			 * message.append("Expected result: \n"); while
			 * (expectedResultTable.hasNext()) {
			 * message.append(expectedResultTable.next()); message.append("\n"); }
			 * message.append("============="); StringUtil.appendN('=',
			 * getName().length(), message);
			 * message.append("========================\n"); message.append("Query
			 * result: \n"); while (queryResultTable.hasNext()) {
			 * message.append(queryResultTable.next()); message.append("\n"); }
			 * message.append("============="); StringUtil.appendN('=',
			 * getName().length(), message);
			 * message.append("========================\n");
			 */

			List<BindingSet> queryBindings = Iterations.asList(queryResultTable);
			List<BindingSet> expectedBindings = Iterations.asList(expectedResultTable);

			List<BindingSet> missingBindings = new ArrayList<BindingSet>(expectedBindings);
			missingBindings.removeAll(queryBindings);

			List<BindingSet> unexpectedBindings = new ArrayList<BindingSet>(queryBindings);
			unexpectedBindings.removeAll(expectedBindings);

			StringBuilder message = new StringBuilder(128);
			message.append("\n============ ");
			message.append(getName());
			message.append(" =======================\n");

			if (!missingBindings.isEmpty()) {
				message.append("Missing bindings: \n");
				for (BindingSet bs : missingBindings) {
					message.append(bs);
					message.append("\n");
				}

				message.append("=============");
				StringUtil.appendN('=', getName().length(), message);
				message.append("========================\n");
			}

			if (!unexpectedBindings.isEmpty()) {
				message.append("Unexpected bindings: \n");
				for (BindingSet bs : unexpectedBindings) {
					message.append(bs);
					message.append("\n");
				}

				message.append("=============");
				StringUtil.appendN('=', getName().length(), message);
				message.append("========================\n");
			}

			logger.error(message.toString());
			fail(message.toString());
		}
	}

	private void compareGraphs(Set<Statement> queryResult, Set<Statement> expectedResult)
		throws Exception
	{
		if (!ModelUtil.equals(expectedResult, queryResult)) {
			// Don't use RepositoryUtil.difference, it reports incorrect diffs
			/*
			 * Collection<? extends Statement> unexpectedStatements =
			 * RepositoryUtil.difference(queryResult, expectedResult); Collection<?
			 * extends Statement> missingStatements =
			 * RepositoryUtil.difference(expectedResult, queryResult);
			 * StringBuilder message = new StringBuilder(128);
			 * message.append("\n=======Diff: "); message.append(getName());
			 * message.append("========================\n"); if
			 * (!unexpectedStatements.isEmpty()) { message.append("Unexpected
			 * statements in result: \n"); for (Statement st :
			 * unexpectedStatements) { message.append(st.toString());
			 * message.append("\n"); } message.append("============="); for (int i =
			 * 0; i < getName().length(); i++) { message.append("="); }
			 * message.append("========================\n"); } if
			 * (!missingStatements.isEmpty()) { message.append("Statements missing
			 * in result: \n"); for (Statement st : missingStatements) {
			 * message.append(st.toString()); message.append("\n"); }
			 * message.append("============="); for (int i = 0; i <
			 * getName().length(); i++) { message.append("="); }
			 * message.append("========================\n"); }
			 */
			StringBuilder message = new StringBuilder(128);
			message.append("\n============ ");
			message.append(getName());
			message.append(" =======================\n");
			message.append("Expected result: \n");
			for (Statement st : expectedResult) {
				message.append(st.toString());
				message.append("\n");
			}
			message.append("=============");
			StringUtil.appendN('=', getName().length(), message);
			message.append("========================\n");

			message.append("Query result: \n");
			for (Statement st : queryResult) {
				message.append(st.toString());
				message.append("\n");
			}
			message.append("=============");
			StringUtil.appendN('=', getName().length(), message);
			message.append("========================\n");

			logger.error(message.toString());
			fail(message.toString());
		}
	}

	private void uploadDataset(Dataset dataset)
		throws Exception
	{
		RepositoryConnection con = dataRep.getConnection();
		try {
			// Merge default and named graphs to filter duplicates
			Set<URI> graphURIs = new HashSet<URI>();
			graphURIs.addAll(dataset.getDefaultGraphs());
			graphURIs.addAll(dataset.getNamedGraphs());

			for (Resource graphURI : graphURIs) {
				upload(((URI)graphURI), graphURI);
			}
		}
		finally {
			con.close();
		}
	}

	private void upload(URI graphURI, Resource context)
		throws Exception
	{
		RepositoryConnection con = dataRep.getConnection();
		con.setAutoCommit(false);
		try {
			RDFFormat rdfFormat = Rio.getParserFormatForFileName(graphURI.toString(), RDFFormat.TURTLE);
			RDFParser rdfParser = Rio.createParser(rdfFormat, dataRep.getValueFactory());
			rdfParser.setVerifyData(false);
			rdfParser.setDatatypeHandling(DatatypeHandling.IGNORE);
			// rdfParser.setPreserveBNodeIDs(true);

			RDFInserter rdfInserter = new RDFInserter(con);
			rdfInserter.enforceContext(context);
			rdfParser.setRDFHandler(rdfInserter);

			URL graphURL = new URL(graphURI.toString());
			InputStream in = graphURL.openStream();
			try {
				rdfParser.parse(in, graphURI.toString());
			}
			finally {
				in.close();
			}

			con.setAutoCommit(true);
		}
		finally {
			con.close();
		}
	}

	private String readQueryString()
		throws IOException
	{
		InputStream stream = new URL(queryFileURL).openStream();
		try {
			return IOUtil.readString(new InputStreamReader(stream, "UTF-8"));
		}
		finally {
			stream.close();
		}
	}

	private TupleQueryResult readExpectedTupleQueryResult()
		throws Exception
	{
		TupleQueryResultFormat tqrFormat = QueryResultIO.getParserFormatForFileName(resultFileURL);

		if (tqrFormat != null) {
			InputStream in = new URL(resultFileURL).openStream();
			try {
				TupleQueryResult tqr = QueryResultIO.parse(in, tqrFormat);
				// return RepositoryUtil.asGraph(tqr);
				return tqr;
			}
			finally {
				in.close();
			}
		}
		else {
			Set<Statement> resultGraph = readExpectedGraphQueryResult();
			return DAWGTestResultSetUtil.toTupleQueryResult(resultGraph);
		}
	}

	private boolean readExpectedBooleanQueryResult()
		throws Exception
	{
		BooleanQueryResultFormat bqrFormat = BooleanQueryResultParserRegistry.getInstance().getFileFormatForFileName(
				resultFileURL);

		if (bqrFormat != null) {
			InputStream in = new URL(resultFileURL).openStream();
			try {
				return QueryResultIO.parse(in, bqrFormat);
			}
			finally {
				in.close();
			}
		}
		else {
			Set<Statement> resultGraph = readExpectedGraphQueryResult();
			return DAWGTestResultSetUtil.toBooleanQueryResult(resultGraph);
		}
	}

	private Set<Statement> readExpectedGraphQueryResult()
		throws Exception
	{
		RDFFormat rdfFormat = Rio.getParserFormatForFileName(resultFileURL);

		if (rdfFormat != null) {
			RDFParser parser = Rio.createParser(rdfFormat);
			parser.setDatatypeHandling(DatatypeHandling.IGNORE);
			parser.setPreserveBNodeIDs(true);

			Set<Statement> result = new LinkedHashSet<Statement>();
			parser.setRDFHandler(new StatementCollector(result));

			InputStream in = new URL(resultFileURL).openStream();
			try {
				parser.parse(in, resultFileURL);
			}
			finally {
				in.close();
			}

			return result;
		}
		else {
			throw new RuntimeException("Unable to determine file type of results file");
		}
	}

	public static TestSuite suite(String manifestFileURL)
		throws Exception
	{
		logger.info("Building test suite for {}", manifestFileURL);

		TestSuite suite = new TestSuite();

		// Read manifest and create declared test cases
		Repository manifestRep = new SailRepository(new MemoryStore());
		manifestRep.initialize();
		RepositoryConnection con = manifestRep.getConnection();

		URL manifest = new URL(manifestFileURL);

		con.add(manifest, manifestFileURL, RDFFormat.TURTLE);

		suite.setName(manifestFileURL.substring(ManifestTest.MANIFEST_DIR.length()));

		// note that we only select those test cases that are mentioned in the
		// list.
		StringBuilder query = new StringBuilder(512);
		query.append(" SELECT DISTINCT testURI, testName, resultFile, action, queryFile, defaultGraph ");
		query.append(" FROM {} rdf:first {testURI} dawgt:approval {dawgt:Approved}; ");
		query.append("                             mf:name {testName}; ");
		query.append("                             mf:result {resultFile}; ");
		query.append("                             mf:action {action} qt:query {queryFile}; ");
		query.append("                                               [qt:data {defaultGraph}] ");
		query.append(" USING NAMESPACE ");
		query.append("  mf = <http://www.w3.org/2001/sw/DataAccess/tests/test-manifest#>, ");
		query.append("  dawgt = <http://www.w3.org/2001/sw/DataAccess/tests/test-dawg#>, ");
		query.append("  qt = <http://www.w3.org/2001/sw/DataAccess/tests/test-query#>");
		TupleQuery testCaseQuery = con.prepareTupleQuery(QueryLanguage.SERQL, query.toString());

		query.setLength(0);
		query.append(" SELECT graph ");
		query.append(" FROM {action} qt:graphData {graph} ");
		query.append(" USING NAMESPACE ");
		query.append(" qt = <http://www.w3.org/2001/sw/DataAccess/tests/test-query#>");
		TupleQuery namedGraphsQuery = con.prepareTupleQuery(QueryLanguage.SERQL, query.toString());

		logger.debug("evaluating query..");
		TupleQueryResult testCases = testCaseQuery.evaluate();
		while (testCases.hasNext()) {
			BindingSet bindingSet = testCases.next();

			String testURI = bindingSet.getValue("testURI").toString();
			String testName = bindingSet.getValue("testName").toString();
			String resultFile = bindingSet.getValue("resultFile").toString();
			String queryFile = bindingSet.getValue("queryFile").toString();
			URI defaultGraphURI = (URI)bindingSet.getValue("defaultGraph");
			Value action = bindingSet.getValue("action");

			logger.debug("found test case : {}", testName);

			// Query named graphs
			namedGraphsQuery.setBinding("action", action);
			TupleQueryResult namedGraphs = namedGraphsQuery.evaluate();

			DatasetImpl dataset = null;

			if (defaultGraphURI != null || namedGraphs.hasNext()) {
				dataset = new DatasetImpl();

				if (defaultGraphURI != null) {
					dataset.addDefaultGraph(defaultGraphURI);
				}

				while (namedGraphs.hasNext()) {
					BindingSet graphBindings = namedGraphs.next();
					URI namedGraphURI = (URI)graphBindings.getValue("graph");
					dataset.addNamedGraph(namedGraphURI);
				}
			}

			suite.addTest(new SPARQLQueryTest(testURI, testName, queryFile, resultFile, dataset));
		}

		testCases.close();
		con.close();

		manifestRep.shutDown();
		logger.info("Created test suite with " + suite.countTestCases() + " test cases.");
		return suite;
	}
}
