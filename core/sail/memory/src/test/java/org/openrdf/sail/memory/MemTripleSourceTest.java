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
package org.openrdf.sail.memory;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.ExceptionConvertingIteration;
import info.aduna.iteration.Iterations;

import org.openrdf.IsolationLevel;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.evaluation.TripleSource;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.Rio;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;
import org.openrdf.sail.base.SailDataset;
import org.openrdf.sail.base.SailSource;

/**
 * Unit Test for {@link TripleSource}
 * 
 * @author Peter Ansell
 */
public class MemTripleSourceTest {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private MemoryStore store;

	protected static final String EX_NS = "http://example.org/";

	private URI bob;

	private URI alice;

	private URI mary;

	private ValueFactory f;

	private SailDataset snapshot;

	private SailSource source;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp()
		throws Exception
	{
		store = new MemoryStore();
		store.initialize();
		f = store.getValueFactory();

		bob = f.createURI(EX_NS, "bob");
		alice = f.createURI(EX_NS, "alice");
		mary = f.createURI(EX_NS, "mary");
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown()
		throws Exception
	{
		if (snapshot != null) {
			snapshot.close();
		}
		if (source != null) {
			source.close();
		}
		store.shutDown();
	}

	/**
	 * Test method for
	 * {@link org.openrdf.sail.memory.TripleSource#getStatements(org.openrdf.model.Resource, org.openrdf.model.URI, org.openrdf.model.Value, org.openrdf.model.Resource[])}
	 * .
	 */
	@Test
	public final void testGetStatementsNoContextsAllNull()
		throws Exception
	{
		loadTestData("/alp-testdata.ttl");
		TripleSource source = getTripleSourceCommitted();

		CloseableIteration<? extends Statement, QueryEvaluationException> statements = source.getStatements(null,
				null, null);

		try {
			List<Statement> list = Iterations.asList(statements);

			assertEquals(8, list.size());
		}
		finally {
			statements.close();
		}
	}

	/**
	 * Test method for
	 * {@link org.openrdf.sail.memory.TripleSource#getStatements(org.openrdf.model.Resource, org.openrdf.model.URI, org.openrdf.model.Value, org.openrdf.model.Resource[])}
	 * .
	 */
	@Test
	public final void testGetStatementsOneContextAllNull()
		throws Exception
	{
		loadTestData("/alp-testdata.ttl", this.alice);
		TripleSource source = getTripleSourceCommitted();

		CloseableIteration<? extends Statement, QueryEvaluationException> statements = source.getStatements(null,
				null, null);

		try {
			List<Statement> list = Iterations.asList(statements);

			assertEquals(8, list.size());
		}
		finally {
			statements.close();
		}
	}

	/**
	 * Test method for
	 * {@link org.openrdf.sail.memory.TripleSource#getStatements(org.openrdf.model.Resource, org.openrdf.model.URI, org.openrdf.model.Value, org.openrdf.model.Resource[])}
	 * .
	 */
	@Test
	public final void testGetStatementsTwoContextsAllNull()
		throws Exception
	{
		loadTestData("/alp-testdata.ttl", this.alice, this.bob);
		TripleSource source = getTripleSourceCommitted();

		CloseableIteration<? extends Statement, QueryEvaluationException> statements = source.getStatements(null,
				null, null);

		try {
			List<Statement> list = Iterations.asList(statements);

			assertEquals(16, list.size());
		}
		finally {
			statements.close();
		}
	}

	/**
	 * Test method for
	 * {@link org.openrdf.sail.memory.TripleSource#getStatements(org.openrdf.model.Resource, org.openrdf.model.URI, org.openrdf.model.Value, org.openrdf.model.Resource[])}
	 * .
	 */
	@Test
	public final void testGetStatementsNoContextsOnePredicate()
		throws Exception
	{
		loadTestData("/alp-testdata.ttl");
		TripleSource source = getTripleSourceCommitted();

		CloseableIteration<? extends Statement, QueryEvaluationException> statements = source.getStatements(null,
				RDFS.SUBCLASSOF, null);

		try {
			List<Statement> list = Iterations.asList(statements);

			assertEquals(4, list.size());
		}
		finally {
			statements.close();
		}
	}

	/**
	 * Test method for
	 * {@link org.openrdf.sail.memory.TripleSource#getStatements(org.openrdf.model.Resource, org.openrdf.model.URI, org.openrdf.model.Value, org.openrdf.model.Resource[])}
	 * .
	 */
	@Test
	public final void testGetStatementsOneContextOnePredicate()
		throws Exception
	{
		loadTestData("/alp-testdata.ttl", this.alice);
		TripleSource source = getTripleSourceCommitted();

		CloseableIteration<? extends Statement, QueryEvaluationException> statements = source.getStatements(null,
				RDFS.SUBCLASSOF, null);

		try {
			List<Statement> list = Iterations.asList(statements);

			assertEquals(4, list.size());
		}
		finally {
			statements.close();
		}
	}

	/**
	 * Test method for
	 * {@link org.openrdf.sail.memory.TripleSource#getStatements(org.openrdf.model.Resource, org.openrdf.model.URI, org.openrdf.model.Value, org.openrdf.model.Resource[])}
	 * .
	 */
	@Test
	public final void testGetStatementsTwoContextsOnePredicate()
		throws Exception
	{
		loadTestData("/alp-testdata.ttl", this.alice, this.bob);
		TripleSource source = getTripleSourceCommitted();

		CloseableIteration<? extends Statement, QueryEvaluationException> statements = source.getStatements(null,
				RDFS.SUBCLASSOF, null);

		try {
			List<Statement> list = Iterations.asList(statements);

			assertEquals(8, list.size());
		}
		finally {
			statements.close();
		}
	}

	/**
	 * Test method for
	 * {@link org.openrdf.sail.memory.TripleSource#getStatements(org.openrdf.model.Resource, org.openrdf.model.URI, org.openrdf.model.Value, org.openrdf.model.Resource[])}
	 * .
	 */
	@Test
	public final void testGetStatementsNoContextsOnePredicateOneContext()
		throws Exception
	{
		loadTestData("/alp-testdata.ttl");
		TripleSource source = getTripleSourceCommitted();

		CloseableIteration<? extends Statement, QueryEvaluationException> statements = source.getStatements(null,
				RDFS.SUBCLASSOF, null, this.alice);

		try {
			List<Statement> list = Iterations.asList(statements);

			assertEquals(0, list.size());
		}
		finally {
			statements.close();
		}
	}

	/**
	 * Test method for
	 * {@link org.openrdf.sail.memory.TripleSource#getStatements(org.openrdf.model.Resource, org.openrdf.model.URI, org.openrdf.model.Value, org.openrdf.model.Resource[])}
	 * .
	 */
	@Test
	public final void testGetStatementsOneContextOnePredicateOneContext()
		throws Exception
	{
		loadTestData("/alp-testdata.ttl", this.alice);
		TripleSource source = getTripleSourceCommitted();

		CloseableIteration<? extends Statement, QueryEvaluationException> statements = source.getStatements(null,
				RDFS.SUBCLASSOF, null, this.alice);

		try {
			List<Statement> list = Iterations.asList(statements);

			assertEquals(4, list.size());
		}
		finally {
			statements.close();
		}
	}

	/**
	 * Test method for
	 * {@link org.openrdf.sail.memory.TripleSource#getStatements(org.openrdf.model.Resource, org.openrdf.model.URI, org.openrdf.model.Value, org.openrdf.model.Resource[])}
	 * .
	 */
	@Test
	public final void testGetStatementsTwoContextsOnePredicateOneContext()
		throws Exception
	{
		loadTestData("/alp-testdata.ttl", this.alice, this.bob);
		TripleSource source = getTripleSourceCommitted();

		CloseableIteration<? extends Statement, QueryEvaluationException> statements = source.getStatements(null,
				RDFS.SUBCLASSOF, null, this.alice);

		try {
			List<Statement> list = Iterations.asList(statements);

			assertEquals(4, list.size());
		}
		finally {
			statements.close();
		}
	}

	/**
	 * Test method for
	 * {@link org.openrdf.sail.memory.TripleSource#getStatements(org.openrdf.model.Resource, org.openrdf.model.URI, org.openrdf.model.Value, org.openrdf.model.Resource[])}
	 * .
	 */
	@Test
	public final void testGetStatementsNoContextsOnePredicateTwoContexts()
		throws Exception
	{
		loadTestData("/alp-testdata.ttl");
		TripleSource source = getTripleSourceCommitted();

		CloseableIteration<? extends Statement, QueryEvaluationException> statements = source.getStatements(null,
				RDFS.SUBCLASSOF, null, this.alice, this.bob);

		try {
			List<Statement> list = Iterations.asList(statements);

			assertEquals(0, list.size());
		}
		finally {
			statements.close();
		}
	}

	/**
	 * Test method for
	 * {@link org.openrdf.sail.memory.TripleSource#getStatements(org.openrdf.model.Resource, org.openrdf.model.URI, org.openrdf.model.Value, org.openrdf.model.Resource[])}
	 * .
	 */
	@Test
	public final void testGetStatementsOneContextOnePredicateTwoContexts()
		throws Exception
	{
		loadTestData("/alp-testdata.ttl", this.alice);
		TripleSource source = getTripleSourceCommitted();

		CloseableIteration<? extends Statement, QueryEvaluationException> statements = source.getStatements(null,
				RDFS.SUBCLASSOF, null, this.alice, this.bob);

		try {
			List<Statement> list = Iterations.asList(statements);

			assertEquals(4, list.size());
		}
		finally {
			statements.close();
		}
	}

	/**
	 * Test method for
	 * {@link org.openrdf.sail.memory.TripleSource#getStatements(org.openrdf.model.Resource, org.openrdf.model.URI, org.openrdf.model.Value, org.openrdf.model.Resource[])}
	 * .
	 */
	@Test
	public final void testGetStatementsTwoContextsOnePredicateTwoContexts()
		throws Exception
	{
		loadTestData("/alp-testdata.ttl", this.alice, this.bob);
		TripleSource source = getTripleSourceCommitted();

		CloseableIteration<? extends Statement, QueryEvaluationException> statements = source.getStatements(null,
				RDFS.SUBCLASSOF, null, this.alice, this.bob);

		try {
			List<Statement> list = Iterations.asList(statements);

			assertEquals(8, list.size());
		}
		finally {
			statements.close();
		}
	}

	/**
	 * Test method for
	 * {@link org.openrdf.sail.memory.TripleSource#getStatements(org.openrdf.model.Resource, org.openrdf.model.URI, org.openrdf.model.Value, org.openrdf.model.Resource[])}
	 * .
	 */
	@Test
	public final void testGetStatementsNoContextsPredicateOwlThingTwoContexts()
		throws Exception
	{
		loadTestData("/alp-testdata.ttl");
		TripleSource source = getTripleSourceCommitted();

		CloseableIteration<? extends Statement, QueryEvaluationException> statements = source.getStatements(null,
				RDFS.SUBCLASSOF, OWL.THING, this.alice, this.bob);

		try {
			List<Statement> list = Iterations.asList(statements);

			assertEquals(0, list.size());
		}
		finally {
			statements.close();
		}
	}

	/**
	 * Test method for
	 * {@link org.openrdf.sail.memory.TripleSource#getStatements(org.openrdf.model.Resource, org.openrdf.model.URI, org.openrdf.model.Value, org.openrdf.model.Resource[])}
	 * .
	 */
	@Test
	public final void testGetStatementsOneContextPredicateOwlThingTwoContexts()
		throws Exception
	{
		loadTestData("/alp-testdata.ttl", this.alice);
		TripleSource source = getTripleSourceCommitted();

		CloseableIteration<? extends Statement, QueryEvaluationException> statements = source.getStatements(null,
				RDFS.SUBCLASSOF, OWL.THING, this.alice, this.bob);

		try {
			List<Statement> list = Iterations.asList(statements);

			assertEquals(1, list.size());
		}
		finally {
			statements.close();
		}
	}

	/**
	 * Test method for
	 * {@link org.openrdf.sail.memory.TripleSource#getStatements(org.openrdf.model.Resource, org.openrdf.model.URI, org.openrdf.model.Value, org.openrdf.model.Resource[])}
	 * .
	 */
	@Test
	public final void testGetStatementsTwoContextsPredicateOwlThingTwoContexts()
		throws Exception
	{
		loadTestData("/alp-testdata.ttl", this.alice, this.bob);
		TripleSource source = getTripleSourceCommitted();

		CloseableIteration<? extends Statement, QueryEvaluationException> statements = source.getStatements(null,
				RDFS.SUBCLASSOF, OWL.THING, this.alice, this.bob);

		try {
			List<Statement> list = Iterations.asList(statements);

			assertEquals(2, list.size());
		}
		finally {
			statements.close();
		}
	}

	/**
	 * Test method for
	 * {@link org.openrdf.sail.memory.TripleSource#getStatements(org.openrdf.model.Resource, org.openrdf.model.URI, org.openrdf.model.Value, org.openrdf.model.Resource[])}
	 * .
	 */
	@Test
	public final void testGetStatementsNoContextsPredicateOwlClassTwoContexts()
		throws Exception
	{
		loadTestData("/alp-testdata.ttl");
		TripleSource source = getTripleSourceCommitted();

		CloseableIteration<? extends Statement, QueryEvaluationException> statements = source.getStatements(null,
				RDF.TYPE, OWL.CLASS, this.alice, this.bob);

		try {
			List<Statement> list = Iterations.asList(statements);

			assertEquals(0, list.size());
		}
		finally {
			statements.close();
		}
	}

	/**
	 * Test method for
	 * {@link org.openrdf.sail.memory.TripleSource#getStatements(org.openrdf.model.Resource, org.openrdf.model.URI, org.openrdf.model.Value, org.openrdf.model.Resource[])}
	 * .
	 */
	@Test
	public final void testGetStatementsOneContextPredicateOwlClassTwoContexts()
		throws Exception
	{
		loadTestData("/alp-testdata.ttl", this.alice);
		TripleSource source = getTripleSourceCommitted();

		CloseableIteration<? extends Statement, QueryEvaluationException> statements = source.getStatements(null,
				RDF.TYPE, OWL.CLASS, this.alice, this.bob);

		try {
			List<Statement> list = Iterations.asList(statements);

			assertEquals(4, list.size());
		}
		finally {
			statements.close();
		}
	}

	/**
	 * Test method for
	 * {@link org.openrdf.sail.memory.TripleSource#getStatements(org.openrdf.model.Resource, org.openrdf.model.URI, org.openrdf.model.Value, org.openrdf.model.Resource[])}
	 * .
	 */
	@Test
	public final void testGetStatementsTwoContextsPredicateOwlClassTwoContexts()
		throws Exception
	{
		loadTestData("/alp-testdata.ttl", this.alice, this.bob);
		TripleSource source = getTripleSourceCommitted();

		CloseableIteration<? extends Statement, QueryEvaluationException> statements = source.getStatements(null,
				RDF.TYPE, OWL.CLASS, this.alice, this.bob);

		try {
			List<Statement> list = Iterations.asList(statements);

			assertEquals(8, list.size());
		}
		finally {
			statements.close();
		}
	}

	/**
	 * Test method for
	 * {@link org.openrdf.sail.memory.TripleSource#getStatements(org.openrdf.model.Resource, org.openrdf.model.URI, org.openrdf.model.Value, org.openrdf.model.Resource[])}
	 * .
	 */
	@Test
	public final void testGetStatementsNoContextsPredicateOwlClassNoContexts()
		throws Exception
	{
		loadTestData("/alp-testdata.ttl");
		TripleSource source = getTripleSourceCommitted();

		CloseableIteration<? extends Statement, QueryEvaluationException> statements = source.getStatements(null,
				RDF.TYPE, OWL.CLASS);

		try {
			List<Statement> list = Iterations.asList(statements);

			assertEquals(4, list.size());
		}
		finally {
			statements.close();
		}
	}

	/**
	 * Test method for
	 * {@link org.openrdf.sail.memory.TripleSource#getStatements(org.openrdf.model.Resource, org.openrdf.model.URI, org.openrdf.model.Value, org.openrdf.model.Resource[])}
	 * .
	 */
	@Test
	public final void testGetStatementsOneContextPredicateOwlClassNoContexts()
		throws Exception
	{
		loadTestData("/alp-testdata.ttl", this.alice);
		TripleSource source = getTripleSourceCommitted();

		CloseableIteration<? extends Statement, QueryEvaluationException> statements = source.getStatements(null,
				RDF.TYPE, OWL.CLASS);

		try {
			List<Statement> list = Iterations.asList(statements);

			assertEquals(4, list.size());
		}
		finally {
			statements.close();
		}
	}

	/**
	 * Test method for
	 * {@link org.openrdf.sail.memory.TripleSource#getStatements(org.openrdf.model.Resource, org.openrdf.model.URI, org.openrdf.model.Value, org.openrdf.model.Resource[])}
	 * .
	 */
	@Test
	public final void testGetStatementsTwoContextsPredicateOwlClassNoContexts()
		throws Exception
	{
		loadTestData("/alp-testdata.ttl", this.alice, this.bob);
		TripleSource source = getTripleSourceCommitted();

		CloseableIteration<? extends Statement, QueryEvaluationException> statements = source.getStatements(null,
				RDF.TYPE, OWL.CLASS);

		try {
			List<Statement> list = Iterations.asList(statements);

			assertEquals(8, list.size());
		}
		finally {
			statements.close();
		}
	}

	/**
	 * Test method for
	 * {@link org.openrdf.sail.memory.TripleSource#getStatements(org.openrdf.model.Resource, org.openrdf.model.URI, org.openrdf.model.Value, org.openrdf.model.Resource[])}
	 * .
	 */
	@Test
	public final void testGetStatementsNoContextsPredicateExClassNoContexts()
		throws Exception
	{
		loadTestData("/alp-testdata.ttl");
		TripleSource source = getTripleSourceCommitted();

		CloseableIteration<? extends Statement, QueryEvaluationException> statements = source.getStatements(null,
				RDFS.SUBCLASSOF, f.createURI(EX_NS, "A"));

		try {
			List<Statement> list = Iterations.asList(statements);

			assertEquals(3, list.size());
		}
		finally {
			statements.close();
		}
	}

	/**
	 * Test method for
	 * {@link org.openrdf.sail.memory.TripleSource#getStatements(org.openrdf.model.Resource, org.openrdf.model.URI, org.openrdf.model.Value, org.openrdf.model.Resource[])}
	 * .
	 */
	@Test
	public final void testGetStatementsOneContextPredicateExClassNoContexts()
		throws Exception
	{
		loadTestData("/alp-testdata.ttl", this.alice);
		TripleSource source = getTripleSourceCommitted();

		CloseableIteration<? extends Statement, QueryEvaluationException> statements = source.getStatements(null,
				RDFS.SUBCLASSOF, f.createURI(EX_NS, "A"));

		try {
			List<Statement> list = Iterations.asList(statements);

			assertEquals(3, list.size());
		}
		finally {
			statements.close();
		}
	}

	/**
	 * Test method for
	 * {@link org.openrdf.sail.memory.TripleSource#getStatements(org.openrdf.model.Resource, org.openrdf.model.URI, org.openrdf.model.Value, org.openrdf.model.Resource[])}
	 * .
	 */
	@Test
	public final void testGetStatementsTwoContextsPredicateExClassNoContexts()
		throws Exception
	{
		loadTestData("/alp-testdata.ttl", this.alice, this.bob);
		TripleSource source = getTripleSourceCommitted();

		CloseableIteration<? extends Statement, QueryEvaluationException> statements = source.getStatements(null,
				RDFS.SUBCLASSOF, f.createURI(EX_NS, "A"));

		try {
			List<Statement> list = Iterations.asList(statements);

			assertEquals(6, list.size());
		}
		finally {
			statements.close();
		}
	}

	/**
	 * Test method for
	 * {@link org.openrdf.sail.memory.TripleSource#getStatements(org.openrdf.model.Resource, org.openrdf.model.URI, org.openrdf.model.Value, org.openrdf.model.Resource[])}
	 * .
	 */
	@Test
	public final void testGetStatementsNoContextsPredicateExClassOneContext()
		throws Exception
	{
		loadTestData("/alp-testdata.ttl");
		TripleSource source = getTripleSourceCommitted();

		CloseableIteration<? extends Statement, QueryEvaluationException> statements = source.getStatements(null,
				RDFS.SUBCLASSOF, f.createURI(EX_NS, "A"), this.alice);

		try {
			List<Statement> list = Iterations.asList(statements);

			assertEquals(0, list.size());
		}
		finally {
			statements.close();
		}
	}

	/**
	 * Test method for
	 * {@link org.openrdf.sail.memory.TripleSource#getStatements(org.openrdf.model.Resource, org.openrdf.model.URI, org.openrdf.model.Value, org.openrdf.model.Resource[])}
	 * .
	 */
	@Test
	public final void testGetStatementsOneContextPredicateExClassOneContext()
		throws Exception
	{
		loadTestData("/alp-testdata.ttl", this.alice);
		TripleSource source = getTripleSourceCommitted();

		CloseableIteration<? extends Statement, QueryEvaluationException> statements = source.getStatements(null,
				RDFS.SUBCLASSOF, f.createURI(EX_NS, "A"), this.alice);

		try {
			List<Statement> list = Iterations.asList(statements);

			assertEquals(3, list.size());
		}
		finally {
			statements.close();
		}
	}

	/**
	 * Test method for
	 * {@link org.openrdf.sail.memory.TripleSource#getStatements(org.openrdf.model.Resource, org.openrdf.model.URI, org.openrdf.model.Value, org.openrdf.model.Resource[])}
	 * .
	 */
	@Test
	public final void testGetStatementsTwoContextsPredicateExClassOneContext()
		throws Exception
	{
		loadTestData("/alp-testdata.ttl", this.alice, this.bob);
		TripleSource source = getTripleSourceCommitted();

		CloseableIteration<? extends Statement, QueryEvaluationException> statements = source.getStatements(null,
				RDFS.SUBCLASSOF, f.createURI(EX_NS, "A"), this.alice);

		try {
			List<Statement> list = Iterations.asList(statements);

			assertEquals(3, list.size());
		}
		finally {
			statements.close();
		}
	}

	/**
	 * Test method for
	 * {@link org.openrdf.sail.memory.TripleSource#getStatements(org.openrdf.model.Resource, org.openrdf.model.URI, org.openrdf.model.Value, org.openrdf.model.Resource[])}
	 * .
	 */
	@Test
	public final void testGetStatementsNoContextsPredicateExClassTwoContexts()
		throws Exception
	{
		loadTestData("/alp-testdata.ttl");
		TripleSource source = getTripleSourceCommitted();

		CloseableIteration<? extends Statement, QueryEvaluationException> statements = source.getStatements(null,
				RDFS.SUBCLASSOF, f.createURI(EX_NS, "A"), this.alice, this.bob);

		try {
			List<Statement> list = Iterations.asList(statements);

			assertEquals(0, list.size());
		}
		finally {
			statements.close();
		}
	}

	/**
	 * Test method for
	 * {@link org.openrdf.sail.memory.TripleSource#getStatements(org.openrdf.model.Resource, org.openrdf.model.URI, org.openrdf.model.Value, org.openrdf.model.Resource[])}
	 * .
	 */
	@Test
	public final void testGetStatementsOneContextPredicateExClassTwoContexts()
		throws Exception
	{
		loadTestData("/alp-testdata.ttl", this.alice);
		TripleSource source = getTripleSourceCommitted();

		CloseableIteration<? extends Statement, QueryEvaluationException> statements = source.getStatements(null,
				RDFS.SUBCLASSOF, f.createURI(EX_NS, "A"), this.alice, this.bob);

		try {
			List<Statement> list = Iterations.asList(statements);

			assertEquals(3, list.size());
		}
		finally {
			statements.close();
		}
	}

	/**
	 * Test method for
	 * {@link org.openrdf.sail.memory.TripleSource#getStatements(org.openrdf.model.Resource, org.openrdf.model.URI, org.openrdf.model.Value, org.openrdf.model.Resource[])}
	 * .
	 */
	@Test
	public final void testGetStatementsTwoContextsPredicateExClassTwoContexts()
		throws Exception
	{
		loadTestData("/alp-testdata.ttl", this.alice, this.bob);
		TripleSource source = getTripleSourceCommitted();

		CloseableIteration<? extends Statement, QueryEvaluationException> statements = source.getStatements(null,
				RDFS.SUBCLASSOF, f.createURI(EX_NS, "A"), this.alice, this.bob);

		try {
			List<Statement> list = Iterations.asList(statements);

			assertEquals(6, list.size());
		}
		finally {
			statements.close();
		}
	}

	/**
	 * Test method for
	 * {@link org.openrdf.sail.memory.TripleSource#getStatements(org.openrdf.model.Resource, org.openrdf.model.URI, org.openrdf.model.Value, org.openrdf.model.Resource[])}
	 * .
	 */
	@Test
	public final void testGetStatementsNoContextsExClassPredicateTwoContexts()
		throws Exception
	{
		loadTestData("/alp-testdata.ttl");
		TripleSource source = getTripleSourceCommitted();

		CloseableIteration<? extends Statement, QueryEvaluationException> statements = source.getStatements(
				f.createURI(EX_NS, "C"), RDFS.SUBCLASSOF, null, this.alice, this.bob);

		try {
			List<Statement> list = Iterations.asList(statements);

			assertEquals(0, list.size());
		}
		finally {
			statements.close();
		}
	}

	/**
	 * Test method for
	 * {@link org.openrdf.sail.memory.TripleSource#getStatements(org.openrdf.model.Resource, org.openrdf.model.URI, org.openrdf.model.Value, org.openrdf.model.Resource[])}
	 * .
	 */
	@Test
	public final void testGetStatementsOneContextExClassPredicateTwoContexts()
		throws Exception
	{
		loadTestData("/alp-testdata.ttl", this.alice);
		TripleSource source = getTripleSourceCommitted();

		CloseableIteration<? extends Statement, QueryEvaluationException> statements = source.getStatements(
				f.createURI(EX_NS, "C"), RDFS.SUBCLASSOF, null, this.alice, this.bob);

		try {
			List<Statement> list = Iterations.asList(statements);

			assertEquals(1, list.size());
		}
		finally {
			statements.close();
		}
	}

	/**
	 * Test method for
	 * {@link org.openrdf.sail.memory.TripleSource#getStatements(org.openrdf.model.Resource, org.openrdf.model.URI, org.openrdf.model.Value, org.openrdf.model.Resource[])}
	 * .
	 */
	@Test
	public final void testGetStatementsTwoContextsExClassPredicateTwoContexts()
		throws Exception
	{
		loadTestData("/alp-testdata.ttl", this.alice, this.bob);
		TripleSource source = getTripleSourceCommitted();

		CloseableIteration<? extends Statement, QueryEvaluationException> statements = source.getStatements(
				f.createURI(EX_NS, "C"), RDFS.SUBCLASSOF, null, this.alice, this.bob);

		try {
			List<Statement> list = Iterations.asList(statements);

			assertEquals(2, list.size());
		}
		finally {
			statements.close();
		}
	}

	/**
	 * Test method for
	 * {@link org.openrdf.sail.memory.TripleSource#getStatements(org.openrdf.model.Resource, org.openrdf.model.URI, org.openrdf.model.Value, org.openrdf.model.Resource[])}
	 * .
	 */
	@Test
	public final void testGetStatementsNoContextsExClassPredicateNoContexts()
		throws Exception
	{
		loadTestData("/alp-testdata.ttl");
		TripleSource source = getTripleSourceCommitted();

		CloseableIteration<? extends Statement, QueryEvaluationException> statements = source.getStatements(
				f.createURI(EX_NS, "C"), RDFS.SUBCLASSOF, null);

		try {
			List<Statement> list = Iterations.asList(statements);
			
			assertEquals(1, list.size());
		}
		finally {
			statements.close();
		}
	}

	/**
	 * Test method for
	 * {@link org.openrdf.sail.memory.TripleSource#getStatements(org.openrdf.model.Resource, org.openrdf.model.URI, org.openrdf.model.Value, org.openrdf.model.Resource[])}
	 * .
	 */
	@Test
	public final void testGetStatementsOneContextExClassPredicateNoContexts()
		throws Exception
	{
		loadTestData("/alp-testdata.ttl", this.alice);
		TripleSource source = getTripleSourceCommitted();

		CloseableIteration<? extends Statement, QueryEvaluationException> statements = source.getStatements(
				f.createURI(EX_NS, "C"), RDFS.SUBCLASSOF, null);

		try {
			List<Statement> list = Iterations.asList(statements);

			assertEquals(1, list.size());
		}
		finally {
			statements.close();
		}
	}

	/**
	 * Test method for
	 * {@link org.openrdf.sail.memory.TripleSource#getStatements(org.openrdf.model.Resource, org.openrdf.model.URI, org.openrdf.model.Value, org.openrdf.model.Resource[])}
	 * .
	 */
	@Test
	public final void testGetStatementsTwoContextsExClassPredicateNoContexts()
		throws Exception
	{
		loadTestData("/alp-testdata.ttl", this.alice, this.bob);
		TripleSource source = getTripleSourceCommitted();

		CloseableIteration<? extends Statement, QueryEvaluationException> statements = source.getStatements(
				f.createURI(EX_NS, "C"), RDFS.SUBCLASSOF, null);

		try {
			List<Statement> list = Iterations.asList(statements);

			assertEquals(2, list.size());
		}
		finally {
			statements.close();
		}
	}

	/**
	 * Test method for
	 * {@link org.openrdf.sail.memory.TripleSource#getStatements(org.openrdf.model.Resource, org.openrdf.model.URI, org.openrdf.model.Value, org.openrdf.model.Resource[])}
	 * .
	 */
	@Test
	public final void testGetStatementsThreeContextsAllNull()
		throws Exception
	{
		loadTestData("/alp-testdata.ttl", this.alice, this.bob, this.mary);
		TripleSource source = getTripleSourceCommitted();

		CloseableIteration<? extends Statement, QueryEvaluationException> statements = source.getStatements(null,
				null, null);

		try {
			List<Statement> list = Iterations.asList(statements);

			assertEquals(24, list.size());
		}
		finally {
			statements.close();
		}
	}

	/**
	 * Test method for
	 * {@link org.openrdf.sail.memory.TripleSource#getStatements(org.openrdf.model.Resource, org.openrdf.model.URI, org.openrdf.model.Value, org.openrdf.model.Resource[])}
	 * .
	 */
	@Test
	public final void testGetStatementsThreeContextsOneContext()
		throws Exception
	{
		loadTestData("/alp-testdata.ttl", this.alice, this.bob, this.mary);
		TripleSource source = getTripleSourceCommitted();

		CloseableIteration<? extends Statement, QueryEvaluationException> statements = source.getStatements(null,
				null, null, this.alice);

		try {
			List<Statement> list = Iterations.asList(statements);

			assertEquals(8, list.size());
		}
		finally {
			statements.close();
		}
	}

	/**
	 * Test method for
	 * {@link org.openrdf.sail.memory.TripleSource#getStatements(org.openrdf.model.Resource, org.openrdf.model.URI, org.openrdf.model.Value, org.openrdf.model.Resource[])}
	 * .
	 */
	@Test
	public final void testGetStatementsThreeContextsTwoContexts()
		throws Exception
	{
		loadTestData("/alp-testdata.ttl", this.alice, this.bob, this.mary);
		TripleSource source = getTripleSourceCommitted();

		CloseableIteration<? extends Statement, QueryEvaluationException> statements = source.getStatements(null,
				null, null, this.alice, this.bob);

		try {
			List<Statement> list = Iterations.asList(statements);

			assertEquals(16, list.size());
		}
		finally {
			statements.close();
		}
	}

	/**
	 * Test method for
	 * {@link org.openrdf.sail.memory.TripleSource#getStatements(org.openrdf.model.Resource, org.openrdf.model.URI, org.openrdf.model.Value, org.openrdf.model.Resource[])}
	 * .
	 */
	@Test
	public final void testGetStatementsThreeContextsThreeContexts()
		throws Exception
	{
		loadTestData("/alp-testdata.ttl", this.alice, this.bob, this.mary);
		TripleSource source = getTripleSourceCommitted();

		CloseableIteration<? extends Statement, QueryEvaluationException> statements = source.getStatements(null,
				null, null, this.alice, this.bob, this.mary);

		try {
			List<Statement> list = Iterations.asList(statements);

			assertEquals(24, list.size());
		}
		finally {
			statements.close();
		}
	}

	protected void loadTestData(String dataFile, Resource... contexts)
		throws RDFParseException, IOException, SailException
	{
		logger.debug("loading dataset {}", dataFile);
		InputStream dataset = this.getClass().getResourceAsStream(dataFile);
		SailConnection con = store.getConnection();
		try {
			con.begin();
			for (Statement nextStatement : Rio.parse(dataset, "", RDFFormat.TURTLE, contexts)) {
				con.addStatement(nextStatement.getSubject(), nextStatement.getPredicate(),
						nextStatement.getObject(), nextStatement.getContext());
			}
		}
		finally {
			con.commit();
			con.close();
			dataset.close();
		}
		logger.debug("dataset loaded.");
	}

	/**
	 * Helper method to avoid writing this constructor multiple times. It needs
	 * to be created after statements are added and committed.
	 * 
	 * @return
	 * @throws SailException 
	 */
	private TripleSource getTripleSourceCommitted()
		throws SailException
	{
		IsolationLevel level = store.getDefaultIsolationLevel();
		source = store.getSailStore().getExplicitSailSource().fork();
		snapshot = source.dataset(level);
		final ValueFactory vf = store.getValueFactory();
		return new TripleSource() {

			public CloseableIteration<? extends Statement, QueryEvaluationException> getStatements(
					Resource subj, URI pred, Value obj, Resource... contexts)
				throws QueryEvaluationException
			{
				try {
					return new ExceptionConvertingIteration<Statement, QueryEvaluationException>(
							snapshot.getStatements(subj, pred, obj, contexts))
					{

						protected QueryEvaluationException convert(Exception e) {
							return new QueryEvaluationException(e);
						}
					};
				}
				catch (SailException e) {
					throw new QueryEvaluationException(e);
				}
			}

			public ValueFactory getValueFactory() {
				return vf;
			}
		};
	}

}
