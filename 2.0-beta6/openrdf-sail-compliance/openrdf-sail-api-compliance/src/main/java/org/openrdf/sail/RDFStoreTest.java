/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail;

import junit.framework.TestCase;

import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.Iteration;
import info.aduna.iteration.Iterations;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.BNodeImpl;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.NumericLiteralImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.impl.EmptyBindingSet;
import org.openrdf.query.impl.MapBindingSet;
import org.openrdf.query.parser.ParsedTupleQuery;
import org.openrdf.query.parser.QueryParserUtil;

/**
 * A JUnit test for testing Sail implementations that store RDF data. This is
 * purely a test for data storage and retrieval which assumes that no
 * inferencing or whatsoever is performed. This is an abstract class that should
 * be extended for specific Sail implementations.
 */
public abstract class RDFStoreTest extends TestCase implements SailChangedListener {

	/*-----------*
	 * Constants *
	 *-----------*/

	private static final String EXAMPLE_NS = "http://example.org/";

	private static final String PAINTER = "Painter";

	private static final String PAINTS = "paints";

	private static final String PAINTING = "Painting";

	private static final String PICASSO = "picasso";

	private static final String REMBRANDT = "rembrandt";

	private static final String GUERNICA = "guernica";

	private static final String NIGHTWATCH = "nightwatch";

	private static final String CONTEXT_1 = "context1";

	private static final String CONTEXT_2 = "context2";

	/*-----------*
	 * Variables *
	 *-----------*/

	private URI painter;

	private URI paints;

	private URI painting;

	private URI picasso;

	private URI rembrandt;

	private URI guernica;

	private URI nightwatch;

	private URI context1;

	private URI context2;

	private Sail sail;

	private SailConnection con;

	private ValueFactory vf;

	private int removeEventCount;

	private int addEventCount;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public RDFStoreTest(String name) {
		super(name);
	}

	/*---------*
	 * Methods *
	 *---------*/

	/**
	 * Gets an instance of the Sail that should be tested. The returned
	 * repository should already have been initialized.
	 * 
	 * @return an initialized Sail.
	 * @throws SailException
	 *         If the initialization of the repository failed.
	 */
	protected abstract Sail createSail()
		throws SailException;

	@Override
	protected void setUp()
		throws Exception
	{
		sail = createSail();

		// set self as listener
		sail.addSailChangedListener(this);

		con = sail.getConnection();

		// Create values
		vf = sail.getValueFactory();

		painter = vf.createURI(EXAMPLE_NS, PAINTER);
		paints = vf.createURI(EXAMPLE_NS, PAINTS);
		painting = vf.createURI(EXAMPLE_NS, PAINTING);
		picasso = vf.createURI(EXAMPLE_NS, PICASSO);
		guernica = vf.createURI(EXAMPLE_NS, GUERNICA);
		rembrandt = vf.createURI(EXAMPLE_NS, REMBRANDT);
		nightwatch = vf.createURI(EXAMPLE_NS, NIGHTWATCH);

		context1 = vf.createURI(EXAMPLE_NS, CONTEXT_1);
		context2 = vf.createURI(EXAMPLE_NS, CONTEXT_2);

	}

	@Override
	protected void tearDown()
		throws Exception
	{
		try {
			con.close();
		}
		finally {
			sail.shutDown();
			sail = null;
		}
	}

	public void testEmptyRepository()
		throws Exception
	{
		// repository should be empty
		assertEquals("Empty repository should not return any statements", 0, countElements(con.getStatements(
				null, null, null, false)));

		assertEquals("Named context should be empty", 0, countElements(con.getStatements(null, null, null,
				false, context1)));

		assertEquals("Empty repository should not return any context identifiers", 0,
				countElements(con.getContextIDs()));

		assertEquals("Empty repository should not return any query results", 0,
				countQueryResults("select * from {S} P {O}"));
	}

	public void testValueRoundTrip1()
		throws Exception
	{
		URI subj = new URIImpl(EXAMPLE_NS + PICASSO);
		URI pred = new URIImpl(EXAMPLE_NS + PAINTS);
		URI obj = new URIImpl(EXAMPLE_NS + GUERNICA);

		testValueRoundTrip(subj, pred, obj);
	}

	public void testValueRoundTrip2()
		throws Exception
	{
		BNode subj = new BNodeImpl("foo");
		URI pred = new URIImpl(EXAMPLE_NS + PAINTS);
		URI obj = new URIImpl(EXAMPLE_NS + GUERNICA);

		testValueRoundTrip(subj, pred, obj);
	}

	public void testValueRoundTrip3()
		throws Exception
	{
		URI subj = new URIImpl(EXAMPLE_NS + PICASSO);
		URI pred = new URIImpl(EXAMPLE_NS + PAINTS);
		Literal obj = new LiteralImpl("guernica");

		testValueRoundTrip(subj, pred, obj);
	}

	public void testValueRoundTrip4()
		throws Exception
	{
		URI subj = new URIImpl(EXAMPLE_NS + PICASSO);
		URI pred = new URIImpl(EXAMPLE_NS + PAINTS);
		Literal obj = new LiteralImpl("guernica", "es");

		testValueRoundTrip(subj, pred, obj);
	}

	public void testValueRoundTrip5()
		throws Exception
	{
		URI subj = new URIImpl(EXAMPLE_NS + PICASSO);
		URI pred = new URIImpl(EXAMPLE_NS + PAINTS);
		Literal obj = new NumericLiteralImpl(3);

		testValueRoundTrip(subj, pred, obj);
	}

	private void testValueRoundTrip(Resource subj, URI pred, Value obj)
		throws Exception
	{
		con.addStatement(subj, pred, obj);
		con.commit();

		CloseableIteration<? extends Statement, SailException> stIter = con.getStatements(null, null, null,
				false);

		try {
			assertTrue(stIter.hasNext());

			Statement st = stIter.next();
			assertEquals(st.getSubject(), subj);
			assertEquals(st.getPredicate(), pred);
			assertEquals(st.getObject(), obj);
		}
		finally {
			stIter.close();
		}
	}

	public void testCreateURI1()
		throws Exception
	{
		URI picasso1 = vf.createURI(EXAMPLE_NS, PICASSO);
		URI picasso2 = vf.createURI(EXAMPLE_NS + PICASSO);
		con.addStatement(picasso1, paints, guernica);
		con.addStatement(picasso2, paints, guernica);
		con.commit();

		assertEquals("createURI(Sring) and createURI(String, String) should create equal URIs", 1, con.size());
	}

	public void testCreateURI2()
		throws Exception
	{
		URI picasso1 = vf.createURI(EXAMPLE_NS + PICASSO);
		URI picasso2 = vf.createURI(EXAMPLE_NS, PICASSO);
		con.addStatement(picasso1, paints, guernica);
		con.addStatement(picasso2, paints, guernica);
		con.commit();

		assertEquals("createURI(Sring) and createURI(String, String) should create equal URIs", 1, con.size());
	}

	public void testSize()
		throws Exception
	{
		assertEquals("Size of empty repository should be 0", 0, con.size());

		// Add some data to the repository
		con.addStatement(painter, RDF.TYPE, RDFS.CLASS);
		con.addStatement(painting, RDF.TYPE, RDFS.CLASS);
		con.addStatement(picasso, RDF.TYPE, painter, context1);
		con.addStatement(guernica, RDF.TYPE, painting, context1);
		con.addStatement(picasso, paints, guernica, context1);
		con.commit();

		assertEquals("Size of repository should be 5", 5, con.size());
		assertEquals("Size of named context should be 3", 3, con.size(context1));

		URI unknownContext = new URIImpl(EXAMPLE_NS + "unknown");

		assertEquals("Size of unknown context should be 0", 0, con.size(unknownContext));

		URIImpl uriImplContext1 = new URIImpl(context1.toString());

		assertEquals("Size of named context (defined as URIImpl) should be 3", 3, con.size(uriImplContext1));
	}

	public void testAddData()
		throws Exception
	{
		// Add some data to the repository
		con.addStatement(painter, RDF.TYPE, RDFS.CLASS);
		con.addStatement(painting, RDF.TYPE, RDFS.CLASS);
		con.addStatement(picasso, RDF.TYPE, painter, context1);
		con.addStatement(guernica, RDF.TYPE, painting, context1);
		con.addStatement(picasso, paints, guernica, context1);
		con.commit();

		assertEquals("Repository should contain 5 statements in total", 5, countElements(con.getStatements(
				null, null, null, false)));

		assertEquals("Named context should contain 3 statements", 3, countElements(con.getStatements(null,
				null, null, false, context1)));

		assertEquals("Repository should have 1 context identifier", 1, countElements(con.getContextIDs()));

		assertEquals("Repository should contain 5 statements in total", 5,
				countQueryResults("select * from {S} P {O}"));

		// Check for presence of the added statements
		assertEquals("Statement (Painter, type, Class) should be in the repository", 1,
				countQueryResults("select 1 from {ex:Painter} rdf:type {rdfs:Class}"));

		assertEquals("Statement (picasso, type, Painter) should be in the repository", 1,
				countQueryResults("select 1 from {ex:picasso} rdf:type {ex:Painter}"));

		// Check for absense of non-added statements
		assertEquals("Statement (Painter, paints, Painting) should not be in the repository", 0,
				countQueryResults("select 1 from {ex:Painter} ex:paints {ex:Painting}"));

		assertEquals("Statement (picasso, creates, guernica) should not be in the repository", 0,
				countQueryResults("select 1 from {ex:picasso} ex:creates {ex:guernica}"));

		// Various other checks
		assertEquals("Repository should contain 2 statements matching (picasso, _, _)", 2,
				countQueryResults("select * from {ex:picasso} P {O}"));

		assertEquals("Repository should contain 1 statement matching (picasso, paints, _)", 1,
				countQueryResults("select * from {ex:picasso} ex:paints {O}"));

		assertEquals("Repository should contain 4 statements matching (_, type, _)", 4,
				countQueryResults("select * from {S} rdf:type {O}"));

		assertEquals("Repository should contain 2 statements matching (_, _, Class)", 2,
				countQueryResults("select * from {S} P {rdfs:Class}"));

		assertEquals("Repository should contain 0 statements matching (_, _, type)", 0,
				countQueryResults("select * from {S} P {rdf:type}"));
	}

	public void testRemoveAndClear()
		throws Exception
	{
		// Add some data to the repository
		con.addStatement(painter, RDF.TYPE, RDFS.CLASS);
		con.addStatement(painting, RDF.TYPE, RDFS.CLASS);
		con.addStatement(picasso, RDF.TYPE, painter, context1);
		con.addStatement(guernica, RDF.TYPE, painting, context1);
		con.addStatement(picasso, paints, guernica, context1);
		con.commit();

		// Test removal of statements
		con.removeStatements(painting, RDF.TYPE, RDFS.CLASS);
		con.commit();

		assertEquals("Repository should contain 4 statements in total", 4, countElements(con.getStatements(
				null, null, null, false)));

		assertEquals("Named context should contain 3 statements", 3, countElements(con.getStatements(null,
				null, null, false, context1)));

		assertEquals("Statement (Painting, type, Class) should no longer be in the repository", 0,
				countQueryResults("select 1 from {ex:Painting} rdf:type {rdfs:Class}"));

		con.removeStatements(null, null, null, context1);
		con.commit();

		assertEquals("Repository should contain 1 statement in total", 1, countElements(con.getStatements(null,
				null, null, false)));

		assertEquals("Named context should be empty", 0, countElements(con.getStatements(null, null, null,
				false, context1)));

		con.clear();
		con.commit();

		assertEquals("Repository should no longer contain any statements", 0, countElements(con.getStatements(
				null, null, null, false)));

		// test if event listener works properly.
		assertEquals("There should have been 1 event in which statements were added", 1, addEventCount);

		assertEquals("There should have been 3 events in which statements were removed", 3, removeEventCount);
	}

	public void testClose() {
		try {
			con.close();
			con.addStatement(painter, RDF.TYPE, RDFS.CLASS);
			fail("Operation on connection after close should result in IllegalStateException");
		}
		catch (IllegalStateException e) {
			// do nothing, this is expected
		}
		catch (SailException e) {
			fail(e.getMessage());
		}
	}

	public void testContexts()
		throws Exception
	{
		// Add schema data to the repository, no context
		con.addStatement(painter, RDF.TYPE, RDFS.CLASS);
		con.addStatement(painting, RDF.TYPE, RDFS.CLASS);

		// Add stuff about picasso to context1
		con.addStatement(picasso, RDF.TYPE, painter, context1);
		con.addStatement(guernica, RDF.TYPE, painting, context1);
		con.addStatement(picasso, paints, guernica, context1);

		// Add stuff about rembrandt to context2
		con.addStatement(rembrandt, RDF.TYPE, painter, context2);
		con.addStatement(nightwatch, RDF.TYPE, painting, context2);
		con.addStatement(rembrandt, paints, nightwatch, context2);

		con.commit();

		assertEquals("context1 should contain 3 statements", 3, countElements(con.getStatements(null, null,
				null, false, context1)));
		assertEquals("context2 should contain 3 statements", 3, countElements(con.getStatements(null, null,
				null, false, context2)));
		assertEquals("Repository should contain 8 statements", 8, countElements(con.getStatements(null, null,
				null, false)));
		assertEquals("statements without context should equal 2", 2, countElements(con.getStatements(null,
				null, null, false, (Resource)null)));

		try {
			// test if IllegalArgumentException is thrown if no explicit cast is
			// done.
			con.getStatements(null, null, null, false, null);
			fail("no cast on vararg parameter should result in IllegalArgumentException");
		}
		catch (IllegalArgumentException e) {
			// do nothing, this is what should happen
		}

		assertEquals("Statements without context and statements in context 1 together should total 5", 5,
				countElements(con.getStatements(null, null, null, false, null, context1)));

		assertEquals("Statements without context and statements in context 2 together should total 5", 5,
				countElements(con.getStatements(null, null, null, false, null, context2)));

		assertEquals("Statements in context 1 and in context 2 together should total 6", 6,
				countElements(con.getStatements(null, null, null, false, context1, context2)));

		// remove two statements from context1.
		con.removeStatements(picasso, null, null, context1);
		con.commit();

		assertEquals("context1 should contain 1 statements", 1, countElements(con.getStatements(null, null,
				null, false, context1)));

		assertEquals("Repository should contain 6 statements", 6, countElements(con.getStatements(null, null,
				null, false)));

		assertEquals("Statements without context and statements in context 1 together should total 3", 3,
				countElements(con.getStatements(null, null, null, false, null, context1)));

		assertEquals("Statements without context and statements in context 2 together should total 5", 5,
				countElements(con.getStatements(null, null, null, false, context2, null)));

		assertEquals("Statements in context 1 and in context 2 together should total 4", 4,
				countElements(con.getStatements(null, null, null, false, context1, context2)));
	}

	public void testQueryBindings()
		throws Exception
	{
		// Add some data to the repository
		con.addStatement(painter, RDF.TYPE, RDFS.CLASS);
		con.addStatement(painting, RDF.TYPE, RDFS.CLASS);
		con.addStatement(picasso, RDF.TYPE, painter, context1);
		con.addStatement(guernica, RDF.TYPE, painting, context1);
		con.addStatement(picasso, paints, guernica, context1);
		con.commit();

		ParsedTupleQuery tupleQuery = QueryParserUtil.parseTupleQuery(QueryLanguage.SERQL,
				"select X from {X} rdf:type {Y} rdf:type {rdfs:Class}", null);
		TupleExpr tupleExpr = tupleQuery.getTupleExpr();

		MapBindingSet bindings = new MapBindingSet(1);

		CloseableIteration<? extends BindingSet, QueryEvaluationException> iter;
		iter = con.evaluate(tupleExpr, null, bindings, false);
		assertEquals(countElements(iter), 2);

		bindings.addBinding("Y", painter);
		iter = con.evaluate(tupleExpr, null, bindings, false);
		assertEquals(countElements(iter), 1);

		bindings.addBinding("Z", painting);
		iter = con.evaluate(tupleExpr, null, bindings, false);
		assertEquals(countElements(iter), 1);

		bindings.removeBinding("Y");
		iter = con.evaluate(tupleExpr, null, bindings, false);
		assertEquals(countElements(iter), 2);
	}

	public void testMultiThreadedAccess() {

		Runnable runnable = new Runnable() {

			SailConnection sharedCon = con;

			public void run() {
				assertTrue(sharedCon != null);

				try {
					sharedCon.addStatement(painter, RDF.TYPE, RDFS.CLASS);
					sharedCon.commit();

					// wait a bit to allow other thread to add stuff as well.
					Thread.sleep(500L);
					CloseableIteration<? extends Statement, SailException> result = sharedCon.getStatements(null,
							null, null, true);

					assertTrue(result.hasNext());
					int numberOfStatements = 0;
					while (result.hasNext()) {
						numberOfStatements++;
						Statement st = result.next();
						assertTrue(st.getSubject().equals(painter) || st.getSubject().equals(picasso));
						assertTrue(st.getPredicate().equals(RDF.TYPE));
						assertTrue(st.getObject().equals(RDFS.CLASS) || st.getObject().equals(painter));
					}
					assertTrue("we should have retrieved statements from both threads", numberOfStatements == 2);

				}
				catch (SailException e) {
					e.printStackTrace();
					fail(e.getMessage());
				}
				catch (InterruptedException e) {
					fail(e.getMessage());
				}

				// let this thread sleep so the other thread can invoke close()
				// first.
				try {
					Thread.sleep(1000L);

					// the connection should now be closed (by the other thread),
					// invoking any further operation should cause a
					// IllegalStateException
					sharedCon.getStatements(null, null, null, true);
					fail("should have caused an IllegalStateException");
				}
				catch (InterruptedException e) {
					fail(e.getMessage());
				}
				catch (SailException e) {
					e.printStackTrace();
					fail(e.getMessage());
				}
				catch (IllegalStateException e) {
					// do nothing, this is the expected behaviour
				}
			}
		}; // end anonymous class declaration

		// execute the other thread
		Thread newThread = new Thread(runnable, "B (parallel)");
		newThread.start();

		try {
			con.addStatement(picasso, RDF.TYPE, painter);
			con.commit();
			// let this thread sleep to enable other thread to finish its business.
			Thread.sleep(1000L);
			con.close();
		}
		catch (SailException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		catch (InterruptedException e) {
			fail(e.getMessage());
		}
	}

	public void sailChanged(SailChangedEvent event) {
		if (event.statementsAdded()) {
			addEventCount++;
		}
		if (event.statementsRemoved()) {
			removeEventCount++;
		}
	}

	private int countElements(Iteration<?, ?> iter)
		throws Exception
	{
		int count = 0;

		try {
			while (iter.hasNext()) {
				iter.next();
				count++;
			}
		}
		finally {
			Iterations.closeCloseable(iter);
		}

		return count;
	}

	private int countQueryResults(String query)
		throws Exception
	{
		ParsedTupleQuery tupleQuery = QueryParserUtil.parseTupleQuery(QueryLanguage.SERQL, query
				+ " using namespace ex = <" + EXAMPLE_NS + ">", null);

		return countElements(con.evaluate(tupleQuery.getTupleExpr(), null, EmptyBindingSet.getInstance(), false));
	}
}
