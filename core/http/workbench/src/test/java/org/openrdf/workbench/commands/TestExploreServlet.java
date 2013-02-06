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
package org.openrdf.workbench.commands;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.openrdf.OpenRDFException;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.UpdateExecutionException;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.memory.MemoryStore;
import org.openrdf.workbench.util.TupleResultBuilder;

/**
 * @author Dale Visser
 */
public class TestExploreServlet {

	private RepositoryConnection connection;

	private ExploreServlet servlet;

	private URI foo;

	private URI bar;

	private static final String PREFIX = "PREFIX test: <http://www.test.com/>\n"
			+ "INSERT DATA { GRAPH test:foo { ";

	private static final String SUFFIX = " . } }";

	private TupleResultBuilder builder;

	/**
	 * @throws RepositoryException
	 *         if an issue occurs making the connection
	 * @throws MalformedQueryException
	 *         if an issue occurs inserting data
	 * @throws UpdateExecutionException
	 *         if an issue occurs inserting data
	 */
	@Before
	public void setUp()
		throws RepositoryException, MalformedQueryException, UpdateExecutionException
	{
		Repository repo = new SailRepository(new MemoryStore());
		repo.initialize();
		connection = repo.getConnection();
		servlet = new ExploreServlet();
		ValueFactory factory = connection.getValueFactory();
		foo = factory.createURI("http://www.test.com/foo");
		bar = factory.createURI("http://www.test.com/bar");
		builder = mock(TupleResultBuilder.class);
	}

	@After
	public void tearDown()
		throws RepositoryException
	{
		connection.close();
		servlet.destroy();
	}

	/**
	 * Test method for
	 * {@link org.openrdf.workbench.commands.ExploreServlet#processResource(org.openrdf.repository.RepositoryConnection, org.openrdf.workbench.util.TupleResultBuilder, org.openrdf.model.Value, int, int, boolean)}
	 * 
	 * @throws RepositoryException
	 *         if a problem occurs executing the method under test
	 */
	@Test
	public final void testSubjectSameAsContext()
		throws OpenRDFException
	{
		add("test:foo a test:bar");
		assertStatementCount(foo, 1);
		verify(builder).result(foo, RDF.TYPE, bar, foo);
	}

	@Test
	public final void testPredicateSameAsContext()
		throws OpenRDFException
	{
		add("test:bar test:foo test:bar");
		assertStatementCount(foo, 1);
		verify(builder).result(bar, foo, bar, foo);
	}

	@Test
	public final void testObjectSameAsContext()
		throws OpenRDFException
	{
		add("test:bar a test:foo");
		assertStatementCount(foo, 1);
		verify(builder).result(bar, RDF.TYPE, foo, foo);
	}

	@Test
	public final void testNoValueSameAsContext()
		throws OpenRDFException
	{
		add("test:bar a test:bar");
		assertStatementCount(foo, 1);
		verify(builder).result(bar, RDF.TYPE, bar, foo);
	}

	@Test
	public final void testOneObjectSameAsContext()
		throws OpenRDFException
	{
		add("test:bar a test:bar , test:foo");
		assertStatementCount(foo, 2);
		verify(builder).result(bar, RDF.TYPE, bar, foo);
		verify(builder).result(bar, RDF.TYPE, foo, foo);
	}
	
	private void add(String pattern)
			throws UpdateExecutionException, RepositoryException, MalformedQueryException {
			connection.prepareUpdate(QueryLanguage.SPARQL, PREFIX + pattern + SUFFIX).execute();
		}

	private void assertStatementCount(URI uri, int expectedValue) throws OpenRDFException{
		int count = servlet.processResource(connection, builder, uri, 0, 0, true);
		assertThat(count, is(expectedValue));
	}
}
