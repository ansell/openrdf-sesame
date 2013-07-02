package org.openrdf.sail;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.io.StringReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import info.aduna.io.ResourceUtil;
import info.aduna.iteration.Iterations;

import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.UnsupportedQueryLanguageException;
import org.openrdf.query.UpdateExecutionException;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.sail.inferencer.fc.CustomGraphQueryInferencer;

public class CustomGraphQueryInferencerTest extends TestCase {

	private static class Expectation {

		private final int initialCount, countAfterRemove, subjCount, predCount, objCount;

		public Expectation(int initialCount, int countAfterRemove, int subjCount, int predCount, int objCount) {
			this.initialCount = initialCount;
			this.countAfterRemove = countAfterRemove;
			this.subjCount = subjCount;
			this.predCount = predCount;
			this.objCount = objCount;
		}
	}

	private static final String TEST_DIR_PREFIX = "/testcases/custom-query-inferencing/";

	private static final String BASE = "http://foo.org/bar#";

	private static final String[] RESOURCE_FOLDERS = { "predicate", "resource" };

	private static final Map<String, Expectation> EXPECTATIONS = new HashMap<String, Expectation>();
	static {
		EXPECTATIONS.put("predicate", new Expectation(8, 2, 0, 2, 0));
		EXPECTATIONS.put("resource", new Expectation(4, 2, 2, 0, 2));
	}

	private final CustomGraphQueryInferencer inferencer;

	private final String initial;

	private final String delete;

	private final String resourceFolder;

	/**
	 * Create a new test of the CustomGraphQueryInferencer for the given
	 * underlying store and data.
	 * 
	 * @param store
	 *        the underlying storage Sail
	 * @param resourceFolder
	 *        name of folder where initialization and removal data resides
	 * @param rule
	 *        custom inferencing rule query (SPARQL)
	 * @param match
	 *        custom matcher query corresponding to rule (SPARQL)
	 * @param initial
	 *        initial data to store (Turtle)
	 * @param delete
	 *        deletion data to further test inferencer response (SPARQL/Update)
	 */
	public CustomGraphQueryInferencerTest(NotifyingSail store, String resourceFolder, String rule,
			String match, String initial, String delete)
		throws MalformedQueryException, UnsupportedQueryLanguageException, SailException, RepositoryException
	{
		super(store.getClass().getName() + "-custom-query-inferencing/" + resourceFolder);
		inferencer = new CustomGraphQueryInferencer(store, QueryLanguage.SPARQL, rule, match);
		this.resourceFolder = resourceFolder;
		this.initial = initial;
		this.delete = delete;
	}

	@Override
	protected void runTest()
		throws RepositoryException, RDFParseException, IOException, MalformedQueryException,
		UpdateExecutionException
	{
		// Initialize
		Repository sail = new SailRepository(inferencer);
		sail.initialize();
		RepositoryConnection connection = sail.getConnection();
		connection.clear();
		connection.add(new StringReader(initial), BASE, RDFFormat.TURTLE);

		// Test initial inferencer state
		Expectation testData = EXPECTATIONS.get(resourceFolder);
		Collection<Value> watchPredicates = inferencer.getWatchPredicates();
		assertThat(watchPredicates.size(), is(equalTo(testData.predCount)));
		Collection<Value> watchObjects = inferencer.getWatchObjects();
		assertThat(watchObjects.size(), is(equalTo(testData.objCount)));
		Collection<Value> watchSubjects = inferencer.getWatchSubjects();
		assertThat(watchSubjects.size(), is(equalTo(testData.subjCount)));
		ValueFactory factory = connection.getValueFactory();
		if ("predicate".equals(resourceFolder)) {
			assertThat(watchPredicates.contains(factory.createURI(BASE, "brotherOf")), is(equalTo(true)));
			assertThat(watchPredicates.contains(factory.createURI(BASE, "parentOf")), is(equalTo(true)));
		}
		else {
			URI bob = factory.createURI(BASE, "Bob");
			URI alice = factory.createURI(BASE, "Alice");
			assertThat(watchSubjects.contains(bob), is(equalTo(true)));
			assertThat(watchSubjects.contains(alice), is(equalTo(true)));
			assertThat(watchObjects.contains(bob), is(equalTo(true)));
			assertThat(watchObjects.contains(alice), is(equalTo(true)));
		}

		// Test initial inferencing results
		assertThat(Iterations.asSet(connection.getStatements(null, null, null, true)).size(),
				is(equalTo(testData.initialCount)));

		// Test results after removing some statements
		connection.prepareUpdate(QueryLanguage.SPARQL, delete).execute();
		assertThat(Iterations.asSet(connection.getStatements(null, null, null, true)).size(),
				is(equalTo(testData.countAfterRemove)));

		// Tidy up.
		connection.close();
		sail.shutDown();
	}

	public static void addTests(TestSuite suite, NotifyingSail store)
		throws IOException, MalformedQueryException, UnsupportedQueryLanguageException, SailException,
		RepositoryException
	{
		// repeat the following line for different variations
		for (String resourceFolder : RESOURCE_FOLDERS) {
			String testFolder = TEST_DIR_PREFIX + resourceFolder;
			String rule = ResourceUtil.getString(testFolder + "/rule.rq");
			String match = ResourceUtil.getString(testFolder + "/match.rq");
			String initial = ResourceUtil.getString(testFolder + "/initial.ttl");
			String delete = ResourceUtil.getString(testFolder + "/delete.ru");
			suite.addTest(new CustomGraphQueryInferencerTest(store, resourceFolder, rule, match, initial, delete));
		}
	}
}