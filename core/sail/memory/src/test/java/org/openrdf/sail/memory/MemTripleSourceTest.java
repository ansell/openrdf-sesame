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

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.Iterations;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.Rio;
import org.openrdf.sail.SailException;
import org.openrdf.sail.memory.MemoryStoreConnection.MemTripleSource;
import org.openrdf.sail.memory.model.MemStatement;
import org.openrdf.sail.memory.model.MemValueFactory;
import org.openrdf.sail.memory.model.ReadMode;

/**
 * Unit Test for {@link MemTripleSource}
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

	private MemValueFactory f;
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp()
		throws Exception
	{
		store = new MemoryStore();
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
		store.shutDown();
	}

	/**
	 * Test method for
	 * {@link org.openrdf.sail.memory.MemoryStoreConnection.MemTripleSource#getStatements(org.openrdf.model.Resource, org.openrdf.model.URI, org.openrdf.model.Value, org.openrdf.model.Resource[])}
	 * .
	 */
	@Test
	public final void testGetStatementsNoContextsAllNull()
		throws Exception
	{
		loadTestData("/alp-testdata.ttl");
		MemTripleSource source = getTripleSourceCommitted();

		CloseableIteration<MemStatement, QueryEvaluationException> statements = source.getStatements(null,
				null, null);

		try {
			List<MemStatement> list = Iterations.asList(statements);

			assertEquals(8, list.size());
		}
		finally {
			statements.close();
		}
	}

	/**
	 * Test method for
	 * {@link org.openrdf.sail.memory.MemoryStoreConnection.MemTripleSource#getStatements(org.openrdf.model.Resource, org.openrdf.model.URI, org.openrdf.model.Value, org.openrdf.model.Resource[])}
	 * .
	 */
	@Test
	public final void testGetStatementsOneContextAllNull()
		throws Exception
	{
		loadTestData("/alp-testdata.ttl", this.alice);
		MemTripleSource source = getTripleSourceCommitted();

		CloseableIteration<MemStatement, QueryEvaluationException> statements = source.getStatements(null,
				null, null);

		try {
			List<MemStatement> list = Iterations.asList(statements);

			assertEquals(8, list.size());
		}
		finally {
			statements.close();
		}
	}

	/**
	 * Test method for
	 * {@link org.openrdf.sail.memory.MemoryStoreConnection.MemTripleSource#getStatements(org.openrdf.model.Resource, org.openrdf.model.URI, org.openrdf.model.Value, org.openrdf.model.Resource[])}
	 * .
	 */
	@Test
	public final void testGetStatementsTwoContextsAllNull()
		throws Exception
	{
		loadTestData("/alp-testdata.ttl", this.alice, this.bob);
		MemTripleSource source = getTripleSourceCommitted();

		CloseableIteration<MemStatement, QueryEvaluationException> statements = source.getStatements(null,
				null, null);

		try {
			List<MemStatement> list = Iterations.asList(statements);

			assertEquals(16, list.size());
		}
		finally {
			statements.close();
		}
	}

	/**
	 * Test method for
	 * {@link org.openrdf.sail.memory.MemoryStoreConnection.MemTripleSource#getStatements(org.openrdf.model.Resource, org.openrdf.model.URI, org.openrdf.model.Value, org.openrdf.model.Resource[])}
	 * .
	 */
	@Test
	public final void testGetStatementsNoContextsOnePredicate()
		throws Exception
	{
		loadTestData("/alp-testdata.ttl");
		MemTripleSource source = getTripleSourceCommitted();

		CloseableIteration<MemStatement, QueryEvaluationException> statements = source.getStatements(null,
				RDFS.SUBCLASSOF, null);

		try {
			List<MemStatement> list = Iterations.asList(statements);

			assertEquals(4, list.size());
		}
		finally {
			statements.close();
		}
	}

	/**
	 * Test method for
	 * {@link org.openrdf.sail.memory.MemoryStoreConnection.MemTripleSource#getStatements(org.openrdf.model.Resource, org.openrdf.model.URI, org.openrdf.model.Value, org.openrdf.model.Resource[])}
	 * .
	 */
	@Test
	public final void testGetStatementsThreeContextsAllNull()
		throws Exception
	{
		loadTestData("/alp-testdata.ttl", this.alice, this.bob, this.mary);
		MemTripleSource source = getTripleSourceCommitted();

		CloseableIteration<MemStatement, QueryEvaluationException> statements = source.getStatements(null,
				null, null);

		try {
			List<MemStatement> list = Iterations.asList(statements);

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
		try {
			store.startTransaction();
			for (Statement nextStatement : Rio.parse(dataset, "", RDFFormat.TURTLE, contexts)) {
				store.addStatement(nextStatement.getSubject(), nextStatement.getPredicate(),
						nextStatement.getObject(), nextStatement.getContext(), true);
			}
		}
		finally {
			store.commit();
			dataset.close();
		}
		logger.debug("dataset loaded.");
	}

	/**
	 * Helper method to avoid writing this constructor multiple times. It needs
	 * to be created after statements are added and committed.
	 * 
	 * @return
	 */
	private MemTripleSource getTripleSourceCommitted() {
		return new MemTripleSource(store, true, store.getCurrentSnapshot(), ReadMode.COMMITTED);
	}

}
