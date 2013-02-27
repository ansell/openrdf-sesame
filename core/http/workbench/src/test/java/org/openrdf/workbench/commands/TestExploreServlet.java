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
import static org.hamcrest.core.IsEqual.equalTo;
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
import org.openrdf.workbench.commands.ExploreServlet.ResultCursor;
import org.openrdf.workbench.util.TupleResultBuilder;

/**
 * @author Dale Visser
 */
public class TestExploreServlet {

	private RepositoryConnection connection;

	private ExploreServlet servlet;

	private URI foo, bar, bang, foos[];

	private static final String PREFIX = "PREFIX : <http://www.test.com/>\nINSERT DATA { GRAPH :foo { ";

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
		bang = factory.createURI("http://www.test.com/bang");
		foos = new URI[0x80];
		for (int i = 0; i < foos.length; i++) {
			foos[i] = factory.createURI("http://www.test.com/foo/" + i);
		}
		builder = mock(TupleResultBuilder.class);
	}

	@After
	public void tearDown()
		throws RepositoryException
	{
		connection.close();
		servlet.destroy();
	}

	@Test
	public final void testRegressionSES1748()
		throws OpenRDFException
	{
		for (int i = 0; i < foos.length; i++) {
			connection.add(foo, bar, foos[i]);
		}
		assertStatementCount(foo, 10, foos.length, 10);
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
		addToFooContext(":foo a :bar");
		assertStatementCount(foo, 1, 1);
		verify(builder).result(foo, RDF.TYPE, bar, foo);
	}

	@Test
	public final void testPredicateSameAsContext()
		throws OpenRDFException
	{
		addToFooContext(":bar :foo :bar");
		assertStatementCount(foo, 1, 1);
		verify(builder).result(bar, foo, bar, foo);
	}

	@Test
	public final void testObjectSameAsContext()
		throws OpenRDFException
	{
		addToFooContext(":bar a :foo");
		assertStatementCount(foo, 1, 1);
		verify(builder).result(bar, RDF.TYPE, foo, foo);
	}

	@Test
	public final void testNoValueSameAsContext()
		throws OpenRDFException
	{
		addToFooContext(":bar a :bar");
		assertStatementCount(foo, 1, 1);
		verify(builder).result(bar, RDF.TYPE, bar, foo);
	}

	@Test
	public final void testOneObjectSameAsContext()
		throws OpenRDFException
	{
		addToFooContext(":bar a :bar , :foo");
		assertStatementCount(foo, 2, 2);
		verify(builder).result(bar, RDF.TYPE, bar, foo);
		verify(builder).result(bar, RDF.TYPE, foo, foo);
	}

	@Test
	public final void testSubjectSameAsPredicate()
		throws OpenRDFException
	{
		addToFooContext(":bar :bar :bang");
		assertStatementCount(bar, 1, 1);
		verify(builder).result(bar, bar, bang, foo);
	}

	@Test
	public final void testSubjectSameAsObject()
		throws OpenRDFException
	{
		addToFooContext(":bar a :bar");
		assertStatementCount(bar, 1, 1);
		verify(builder).result(bar, RDF.TYPE, bar, foo);
	}

	@Test
	public final void testPredicateSameAsObject()
		throws OpenRDFException
	{
		addToFooContext(":bar :bang :bang");
		assertStatementCount(bang, 1, 1);
		verify(builder).result(bar, bang, bang, foo);
	}

	@Test
	public final void testWorstCaseDuplication()
		throws OpenRDFException
	{
		addToFooContext(":foo :foo :foo");
		assertStatementCount(foo, 1, 1);
		verify(builder).result(foo, foo, foo, foo);
	}

	@Test
	public final void testSES1723regression()
		throws OpenRDFException
	{
		addToFooContext(":foo :foo :foo");
		connection.add(foo, foo, foo);
		assertStatementCount(foo, 2, 2);
		verify(builder).result(foo, foo, foo, foo);
		verify(builder).result(foo, foo, foo, null);
	}

	private void addToFooContext(String pattern)
		throws UpdateExecutionException, RepositoryException, MalformedQueryException
	{
		connection.prepareUpdate(QueryLanguage.SPARQL, PREFIX + pattern + SUFFIX).execute();
	}

	private void assertStatementCount(URI uri, long expectedTotal, long expectedRendered)
		throws OpenRDFException
	{
		// limit = 0 means render all
		assertStatementCount(uri, 0, expectedTotal, expectedRendered);
	}

	private void assertStatementCount(URI uri, int limit, long expectedTotal, long expectedRendered)
		throws OpenRDFException
	{
		ResultCursor cursor = servlet.processResource(connection, builder, uri, 0, limit, true);
		assertThat(cursor.getTotalResultCount(), is(equalTo(expectedTotal)));
		assertThat(cursor.getRenderedResultCount(), is(equalTo(expectedRendered)));
	}
}
