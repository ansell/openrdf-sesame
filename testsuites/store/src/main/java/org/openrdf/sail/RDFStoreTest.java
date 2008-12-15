/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.CountDownLatch;

import junit.framework.TestCase;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.NumericLiteralImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.impl.EmptyBindingSet;
import org.openrdf.query.impl.MapBindingSet;
import org.openrdf.query.parser.QueryParserUtil;
import org.openrdf.query.parser.TupleQueryModel;
import org.openrdf.results.Cursor;
import org.openrdf.store.StoreException;

/**
 * A JUnit test for testing Sail implementations that store RDF data. This is
 * purely a test for data storage and retrieval which assumes that no
 * inferencing or whatsoever is performed. This is an abstract class that should
 * be extended for specific Sail implementations.
 */
public abstract class RDFStoreTest extends TestCase {

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

	protected URI painter;

	protected URI paints;

	protected URI painting;

	protected URI picasso;

	protected URI rembrandt;

	protected URI guernica;

	protected URI nightwatch;

	protected URI context1;

	protected URI context2;

	protected Sail sail;

	protected SailConnection con;

	protected ValueFactory vf;

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
	 * @throws StoreException
	 *         If the initialization of the repository failed.
	 */
	protected abstract Sail createSail()
		throws StoreException;

	@Override
	protected void setUp()
		throws Exception
	{
		sail = createSail();

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
			if (con.isOpen()) {
				con.rollback();
				con.close();
			}
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
		assertEquals("Empty repository should not return any statements", 0, countAllElements());

		assertEquals("Named context should be empty", 0, countContext1Elements());

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
		BNode subj = vf.createBNode();
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

	public void testDecimalRoundTrip()
		throws Exception
	{
		URI subj = new URIImpl(EXAMPLE_NS + PICASSO);
		URI pred = new URIImpl(EXAMPLE_NS + PAINTS);
		Literal obj = new NumericLiteralImpl(3, XMLSchema.DECIMAL);

		testValueRoundTrip(subj, pred, obj);
	}

	public void testTimeZoneRoundTrip()
		throws Exception
	{
		URI subj = new URIImpl(EXAMPLE_NS + PICASSO);
		URI pred = new URIImpl(EXAMPLE_NS + PAINTS);
		Literal obj = new LiteralImpl("2006-08-23+00:00", XMLSchema.DATE);
		testValueRoundTrip(subj, pred, obj);

		con.removeStatements(null, null, null);
		obj = new LiteralImpl("2006-08-23", XMLSchema.DATE);
		testValueRoundTrip(subj, pred, obj);
	}

	public void testLongURIRoundTrip()
		throws Exception
	{
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < 512; i++) {
			sb.append(Character.toChars('A' + (i % 26)));
		}
		URI subj = new URIImpl(EXAMPLE_NS + PICASSO);
		URI pred = new URIImpl(EXAMPLE_NS + PAINTS);
		URI obj = new URIImpl(EXAMPLE_NS + GUERNICA + sb.toString());

		testValueRoundTrip(subj, pred, obj);
	}

	public void testLongLiteralRoundTrip()
		throws Exception
	{
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < 512; i++) {
			sb.append(Character.toChars('A' + (i % 26)));
		}
		URI subj = new URIImpl(EXAMPLE_NS + PICASSO);
		URI pred = new URIImpl(EXAMPLE_NS + PAINTS);
		Literal obj = new LiteralImpl("guernica" + sb.toString());

		testValueRoundTrip(subj, pred, obj);
	}

	public void testReallyLongLiteralRoundTrip()
		throws Exception
	{
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < 1024000; i++) {
			sb.append(Character.toChars('A' + (i % 26)));
		}
		URI subj = new URIImpl(EXAMPLE_NS + PICASSO);
		URI pred = new URIImpl(EXAMPLE_NS + PAINTS);
		Literal obj = new LiteralImpl("guernica" + sb.toString());

		testValueRoundTrip(subj, pred, obj);
	}

	public void testLongLangRoundTrip()
		throws Exception
	{
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < 512; i++) {
			sb.append(Character.toChars('A' + (i % 26)));
		}
		URI subj = new URIImpl(EXAMPLE_NS + PICASSO);
		URI pred = new URIImpl(EXAMPLE_NS + PAINTS);
		Literal obj = new LiteralImpl("guernica" + sb.toString(), "es");

		testValueRoundTrip(subj, pred, obj);
	}

	private void testValueRoundTrip(Resource subj, URI pred, Value obj)
		throws Exception
	{
		con.addStatement(subj, pred, obj);
		con.commit();

		Cursor<? extends Statement> stIter = con.getStatements(null, null, null, false);

		try {

			Statement st = stIter.next();
			assertEquals(subj, st.getSubject());
			assertEquals(pred, st.getPredicate());
			assertEquals(obj, st.getObject());
			assertNull(stIter.next());
		}
		finally {
			stIter.close();
		}

		TupleQueryModel tupleQuery = QueryParserUtil.parseTupleQuery(QueryLanguage.SERQL,
				"SELECT S, P, O FROM {S} P {O} WHERE P = <" + pred.stringValue() + ">", null);

		Cursor<? extends BindingSet> iter;
		iter = con.evaluate(tupleQuery, EmptyBindingSet.getInstance(), false);

		try {
			BindingSet bindings = iter.next();
			assertEquals(subj, bindings.getValue("S"));
			assertEquals(pred, bindings.getValue("P"));
			assertEquals(obj, bindings.getValue("O"));
			assertNull(iter.next());
		}
		finally {
			iter.close();
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

		assertEquals("createURI(Sring) and createURI(String, String) should create equal URIs", 1, con.size(
				null, null, null, false));
	}

	public void testCreateURI2()
		throws Exception
	{
		URI picasso1 = vf.createURI(EXAMPLE_NS + PICASSO);
		URI picasso2 = vf.createURI(EXAMPLE_NS, PICASSO);
		con.addStatement(picasso1, paints, guernica);
		con.addStatement(picasso2, paints, guernica);
		con.commit();

		assertEquals("createURI(Sring) and createURI(String, String) should create equal URIs", 1, con.size(
				null, null, null, false));
	}

	public void testSize()
		throws Exception
	{
		assertEmpty(con);

		// Add some data to the repository
		con.addStatement(painter, RDF.TYPE, RDFS.CLASS);
		con.addStatement(painting, RDF.TYPE, RDFS.CLASS);
		con.addStatement(picasso, RDF.TYPE, painter, context1);
		con.addStatement(guernica, RDF.TYPE, painting, context1);
		con.addStatement(picasso, paints, guernica, context1);
		con.commit();

		assertEquals(5, con.size(null, null, null, false));
		assertEquals(3, con.size(null, null, null, false, context1));
		assertEquals(4, con.size(null, RDF.TYPE, null, false));
		assertEquals(1, con.size(null, paints, null, false));
		assertEquals(2, con.size(picasso, null, null, false));

		URI unknownContext = new URIImpl(EXAMPLE_NS + "unknown");

		assertEquals(0, con.size(null, null, null, false, unknownContext));
		assertEquals(0, con.size(null, picasso, null, false));

		URIImpl uriImplContext1 = new URIImpl(context1.toString());

		assertEquals(3, con.size(null, null, null, false, uriImplContext1));
	}

	private void assertEmpty(SailConnection con)
		throws StoreException
	{
		URI unknownContext = new URIImpl(EXAMPLE_NS + "unknown");
		for (Resource subj : Arrays.asList(null, picasso)) {
			for (URI pred : Arrays.asList(null, paints, RDF.TYPE)) {
				for (Value obj : Arrays.asList(null, guernica)) {
					for (Resource[] ctx : Arrays.asList(new Resource[0], new Resource[] { context1 },
							new Resource[] { unknownContext }))
					{
						assertEquals(0, con.size(subj, pred, obj, false, ctx));
					}
				}
			}
		}
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

		assertEquals("Repository should contain 5 statements in total", 5, countAllElements());

		assertEquals("Named context should contain 3 statements", 3, countContext1Elements());

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

	public void testAddWhileQuerying()
		throws Exception
	{
		// Add some data to the repository
		con.addStatement(painter, RDF.TYPE, RDFS.CLASS);
		con.addStatement(painting, RDF.TYPE, RDFS.CLASS);
		con.addStatement(picasso, RDF.TYPE, painter);
		con.addStatement(guernica, RDF.TYPE, painting);
		con.addStatement(picasso, paints, guernica);
		con.commit();

		TupleQueryModel tupleQuery = QueryParserUtil.parseTupleQuery(QueryLanguage.SERQL,
				"SELECT C FROM {} rdf:type {C}", null);

		Cursor<? extends BindingSet> iter;
		iter = con.evaluate(tupleQuery, EmptyBindingSet.getInstance(), false);

		BindingSet bindings;
		while ((bindings = iter.next()) != null) {
			Value c = bindings.getValue("C");
			if (c instanceof Resource) {
				con.addStatement((Resource)c, RDF.TYPE, RDFS.CLASS);
			}
		}

		con.commit();

		// Simulate auto-commit

		assertEquals(3, countElements(con.getStatements(null, RDF.TYPE, RDFS.CLASS, false)));

		tupleQuery = QueryParserUtil.parseTupleQuery(QueryLanguage.SERQL, "SELECT P FROM {} P {}", null);
		iter = con.evaluate(tupleQuery, EmptyBindingSet.getInstance(), false);

		while ((bindings = iter.next()) != null) {
			Value p = bindings.getValue("P");
			if (p instanceof URI) {
				con.addStatement((URI)p, RDF.TYPE, RDF.PROPERTY);
				con.commit();
			}
		}

		assertEquals(2, countElements(con.getStatements(null, RDF.TYPE, RDF.PROPERTY, false)));
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

		assertEquals("Repository should contain 4 statements in total", 4, countAllElements());

		assertEquals("Named context should contain 3 statements", 3, countContext1Elements());

		assertEquals("Statement (Painting, type, Class) should no longer be in the repository", 0,
				countQueryResults("select 1 from {ex:Painting} rdf:type {rdfs:Class}"));

		con.removeStatements(null, null, null, context1);
		con.commit();

		assertEquals("Repository should contain 1 statement in total", 1, countAllElements());

		assertEquals("Named context should be empty", 0, countContext1Elements());

		con.removeStatements(null, null, null);
		con.commit();

		assertEquals("Repository should no longer contain any statements", 0, countAllElements());
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
		catch (StoreException e) {
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

		assertEquals("context1 should contain 3 statements", 3, countContext1Elements());
		assertEquals("context2 should contain 3 statements", 3, countElements(con.getStatements(null, null,
				null, false, context2)));
		assertEquals("Repository should contain 8 statements", 8, countAllElements());
		assertEquals("statements without context should equal 2", 2, countElements(con.getStatements(null,
				null, null, false, (Resource)null)));

		assertEquals("Statements without context and statements in context 1 together should total 5", 5,
				countElements(con.getStatements(null, null, null, false, null, context1)));

		assertEquals("Statements without context and statements in context 2 together should total 5", 5,
				countElements(con.getStatements(null, null, null, false, null, context2)));

		assertEquals("Statements in context 1 and in context 2 together should total 6", 6,
				countElements(con.getStatements(null, null, null, false, context1, context2)));

		// remove two statements from context1.
		con.removeStatements(picasso, null, null, context1);
		con.commit();

		assertEquals("context1 should contain 1 statements", 1, countContext1Elements());

		assertEquals("Repository should contain 6 statements", 6, countAllElements());

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

		TupleQueryModel tupleQuery = QueryParserUtil.parseTupleQuery(QueryLanguage.SERQL,
				"select X from {X} rdf:type {Y} rdf:type {rdfs:Class}", null);

		MapBindingSet bindings = new MapBindingSet(1);

		Cursor<? extends BindingSet> iter;
		iter = con.evaluate(tupleQuery, bindings, false);

		int resultCount = verifyQueryResult(iter, 1);
		assertEquals("Wrong number of query results", 2, resultCount);

		bindings.addBinding("Y", painter);
		iter = con.evaluate(tupleQuery, bindings, false);
		resultCount = verifyQueryResult(iter, 1);
		assertEquals("Wrong number of query results", 1, resultCount);

		bindings.addBinding("Z", painting);
		iter = con.evaluate(tupleQuery, bindings, false);
		resultCount = verifyQueryResult(iter, 1);
		assertEquals("Wrong number of query results", 1, resultCount);

		bindings.removeBinding("Y");
		iter = con.evaluate(tupleQuery, bindings, false);
		resultCount = verifyQueryResult(iter, 1);
		assertEquals("Wrong number of query results", 2, resultCount);
	}

	public void testMultiThreadedAccess() {
		final CountDownLatch latch = new CountDownLatch(1);

		Runnable runnable = new Runnable() {

			SailConnection sharedCon = con;

			public void run() {
				assertTrue(sharedCon != null);

				try {
					latch.countDown();
					latch.await();
					sharedCon.addStatement(painter, RDF.TYPE, RDFS.CLASS);
					sharedCon.commit();

					// wait a bit to allow other thread to add stuff as well.
					Thread.sleep(500L);
					Cursor<? extends Statement> result = sharedCon.getStatements(null, null, null, true);

					int numberOfStatements = 0;
					Statement st;
					while ((st = result.next()) != null) {
						numberOfStatements++;
						assertTrue(st.getSubject().equals(painter) || st.getSubject().equals(picasso));
						assertTrue(st.getPredicate().equals(RDF.TYPE));
						assertTrue(st.getObject().equals(RDFS.CLASS) || st.getObject().equals(painter));
					}
					assertTrue("we should have retrieved statements from both threads", numberOfStatements == 2);

				}
				catch (StoreException e) {
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
				catch (StoreException e) {
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
			latch.countDown();
			latch.await();
			con.addStatement(picasso, RDF.TYPE, painter);
			con.commit();
			// let this thread sleep to enable other thread to finish its business.
			Thread.sleep(1000L);
			con.close();
		}
		catch (StoreException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		catch (InterruptedException e) {
			fail(e.getMessage());
		}
	}

	public void testStatementEquals()
		throws Exception
	{
		Statement st = vf.createStatement(picasso, RDF.TYPE, painter);
		assertFalse(st.equals(vf.createStatement(picasso, RDF.TYPE, painter, context1)));
		assertFalse(st.equals(vf.createStatement(picasso, RDF.TYPE, painter, context2)));
	}

	public void testStatementSerialization()
		throws Exception
	{
		Statement st = vf.createStatement(picasso, RDF.TYPE, painter);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream out = new ObjectOutputStream(baos);
		out.writeObject(st);
		out.close();

		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		ObjectInputStream in = new ObjectInputStream(bais);
		Statement deserializedStatement = (Statement)in.readObject();
		in.close();

		assertTrue(st.equals(deserializedStatement));
	}

	public void testGetNamespaces()
		throws Exception
	{
		con.setNamespace("rdf", RDF.NAMESPACE);
		con.commit();

		Cursor<? extends Namespace> namespaces = con.getNamespaces();
		try {
			Namespace rdf = namespaces.next();
			assertEquals("rdf", rdf.getPrefix());
			assertEquals(RDF.NAMESPACE, rdf.getName());
			assertNull(namespaces.next());
		}
		finally {
			namespaces.close();
		}
	}

	public void testGetNamespace()
		throws Exception
	{
		con.setNamespace("rdf", RDF.NAMESPACE);
		con.commit();
		assertEquals(RDF.NAMESPACE, con.getNamespace("rdf"));
	}

	public void testClearNamespaces()
		throws Exception
	{
		con.setNamespace("rdf", RDF.NAMESPACE);
		con.setNamespace("rdfs", RDFS.NAMESPACE);
		con.clearNamespaces();
		con.commit();
		assertNull(con.getNamespaces().next());
	}

	public void testRemoveNamespaces()
		throws Exception
	{
		con.setNamespace("rdf", RDF.NAMESPACE);
		con.removeNamespace("rdf");
		con.commit();
		assertNull(con.getNamespace("rdf"));
	}

	public void testGetContextIDs()
		throws Exception
	{
		assertEquals(0, countElements(con.getContextIDs()));

		// load data
		con.addStatement(picasso, paints, guernica, context1);
		assertEquals(1, countElements(con.getContextIDs()));
		assertEquals(context1, first(con.getContextIDs()));

		con.removeStatements(picasso, paints, guernica, context1);
		assertEquals(0, countElements(con.getContextIDs()));
		con.commit();

		assertEquals(0, countElements(con.getContextIDs()));

		con.addStatement(picasso, paints, guernica, context2);
		assertEquals(1, countElements(con.getContextIDs()));
		assertEquals(context2, first(con.getContextIDs()));
		con.commit();
	}

	public void testGetMultipleContextIDs()
		throws Exception
	{
		assertEquals(0, countElements(con.getContextIDs()));

		// load data
		con.addStatement(painter, RDF.TYPE, RDFS.CLASS);
		con.addStatement(painting, RDF.TYPE, RDFS.CLASS);
		con.addStatement(picasso, RDF.TYPE, painter, context1);
		con.addStatement(guernica, RDF.TYPE, painting, context1);
		con.addStatement(picasso, paints, guernica, context2);
		con.addStatement(picasso, paints, guernica, context1);
		assertEquals(2, countElements(con.getContextIDs()));
	}

	public void testOldURI()
		throws Exception
	{
		assertEquals(0, countAllElements());
		con.addStatement(painter, RDF.TYPE, RDFS.CLASS);
		con.addStatement(painting, RDF.TYPE, RDFS.CLASS);
		con.addStatement(picasso, RDF.TYPE, painter, context1);
		con.addStatement(guernica, RDF.TYPE, painting, context1);
		con.addStatement(picasso, paints, guernica, context1);
		assertEquals(5, countAllElements());
		con.commit();
		con.removeStatements(null, null, null);
		con.commit();
		con.addStatement(picasso, paints, guernica, context1);
		con.commit();
		assertEquals(1, countAllElements());
	}

	public void testDualConnections()
		throws Exception
	{
		SailConnection con2 = sail.getConnection();
		try {
			assertEquals(0, countAllElements());
			con.addStatement(painter, RDF.TYPE, RDFS.CLASS);
			con.addStatement(painting, RDF.TYPE, RDFS.CLASS);
			con.addStatement(picasso, RDF.TYPE, painter, context1);
			con.addStatement(guernica, RDF.TYPE, painting, context1);
			con.commit();
			assertEquals(4, countAllElements());
			con2.addStatement(RDF.NIL, RDF.TYPE, RDF.LIST);
			String query = "SELECT S, P, O FROM {S} P {O}";
			TupleQueryModel tupleQuery = QueryParserUtil.parseTupleQuery(QueryLanguage.SERQL, query, null);
			assertEquals(5, countElements(con2.evaluate(tupleQuery,
					EmptyBindingSet.getInstance(), false)));
			Runnable clearer = new Runnable() {

				public void run() {
					try {
						con.removeStatements(null, null, null);
						con.commit();
					}
					catch (StoreException e) {
						throw new RuntimeException(e);
					}
				}
			};
			Thread thread = new Thread(clearer);
			thread.start();
			Thread.yield();
			Thread.yield();
			con2.commit();
			thread.join();
		}
		finally {
			con2.close();
		}
	}

	public void testBNodeReuse()
		throws Exception
	{
		con.addStatement(RDF.VALUE, RDF.VALUE, RDF.VALUE);
		assertEquals(1, con.size(null, null, null, false));
		BNode b1 = vf.createBNode();
		con.addStatement(b1, RDF.VALUE, b1);
		con.removeStatements(b1, RDF.VALUE, b1);
		assertEquals(1, con.size(null, null, null, false));
		BNode b2 = vf.createBNode();
		con.addStatement(b2, RDF.VALUE, b2);
		con.addStatement(b1, RDF.VALUE, b1);
		assertEquals(3, con.size(null, null, null, false));
	}

	private <T> T first(Cursor<T> iter)
		throws Exception
	{
		try {
			return iter.next();
		}
		finally {
			iter.close();
		}
	}

	protected int countContext1Elements()
		throws Exception, StoreException
	{
		return countElements(con.getStatements(null, null, null, false, context1));
	}

	protected int countAllElements()
		throws Exception, StoreException
	{
		return countElements(con.getStatements(null, null, null, false));
	}

	private int countElements(Cursor<?> iter)
		throws Exception
	{
		int count = 0;

		try {
			while (iter.next() != null) {
				count++;
			}
		}
		finally {
			iter.close();
		}

		return count;
	}

	protected int countQueryResults(String query)
		throws Exception
	{
		TupleQueryModel tupleQuery = QueryParserUtil.parseTupleQuery(QueryLanguage.SERQL, query
				+ " using namespace ex = <" + EXAMPLE_NS + ">", null);

		return countElements(con.evaluate(tupleQuery, EmptyBindingSet.getInstance(), false));
	}

	private int verifyQueryResult(Cursor<? extends BindingSet> resultIter, int expectedBindingCount)
		throws StoreException
	{
		int resultCount = 0;

		BindingSet resultBindings;
		while ((resultBindings = resultIter.next()) != null) {
			resultCount++;

			assertEquals("Wrong number of binding names for binding set", expectedBindingCount,
					resultBindings.getBindingNames().size());

			int bindingCount = 0;
			Iterator<Binding> bindingIter = resultBindings.iterator();
			while (bindingIter.hasNext()) {
				bindingIter.next();
				bindingCount++;
			}

			assertEquals("Wrong number of bindings in binding set", expectedBindingCount, bindingCount);
		}

		return resultCount;
	}
}
