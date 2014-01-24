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
package org.openrdf.rio.helpers;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.openrdf.model.Model;
import org.openrdf.model.Statement;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.rio.RDFHandlerException;

/**
 * @author ans025
 */
public class StatementCollectorTest {

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp()
		throws Exception
	{
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown()
		throws Exception
	{
	}

	/**
	 * Test method for
	 * {@link org.openrdf.rio.helpers.StatementCollector#StatementCollector()}.
	 */
	@Test
	public final void testStatementCollector()
		throws Exception
	{
		StatementCollector collector = new StatementCollector();

		assertNotNull(collector.getStatements());
		assertNotNull(collector.getNamespaces());
	}

	/**
	 * Test method for
	 * {@link org.openrdf.rio.helpers.StatementCollector#StatementCollector(java.util.Collection)}
	 * .
	 */
	@Test
	public final void testStatementCollectorList()
		throws Exception
	{
		List<Statement> testList = new ArrayList<Statement>();
		StatementCollector collector = new StatementCollector(testList);

		// Actual variable is exactly the same, although it could be theoretically
		// wrapped and still be consistent
		assertTrue(testList == collector.getStatements());
		assertNotNull(collector.getNamespaces());
	}

	/**
	 * Test method for
	 * {@link org.openrdf.rio.helpers.StatementCollector#StatementCollector(java.util.Collection)}
	 * .
	 */
	@Test
	public final void testStatementCollectorSet()
		throws Exception
	{
		Set<Statement> testList = new LinkedHashSet<Statement>();
		StatementCollector collector = new StatementCollector(testList);

		// Actual variable is exactly the same, although it could be theoretically
		// wrapped and still be consistent
		assertTrue(testList == collector.getStatements());
		assertNotNull(collector.getNamespaces());
	}

	/**
	 * Test method for
	 * {@link org.openrdf.rio.helpers.StatementCollector#StatementCollector(java.util.Collection)}
	 * .
	 */
	@Test
	public final void testStatementCollectorCollectionModel()
		throws Exception
	{
		Collection<Statement> testList = new LinkedHashModel();
		StatementCollector collector = new StatementCollector(testList);

		// Actual variable is exactly the same, although it could be theoretically
		// wrapped and still be consistent
		assertTrue(testList == collector.getStatements());
		assertNotNull(collector.getNamespaces());
	}

	/**
	 * Test method for
	 * {@link org.openrdf.rio.helpers.StatementCollector#StatementCollector(java.util.Collection, java.util.Map)}
	 * .
	 */
	@Test
	public final void testStatementCollectorCollectionModelMapIndependent()
		throws Exception
	{
		Model testList = new LinkedHashModel();
		Map<String, String> testNamespaces = new LinkedHashMap<String, String>();
		StatementCollector collector = new StatementCollector(testList, testNamespaces);

		// Actual variable is exactly the same, although it could be theoretically
		// wrapped and still be consistent
		assertTrue(testList == collector.getStatements());
		assertTrue(testNamespaces == collector.getNamespaces());

	}

	/**
	 * Test method for
	 * {@link org.openrdf.rio.helpers.StatementCollector#StatementCollector(java.util.Collection, java.util.Map)}
	 * .
	 */
	@Test
	public final void testStatementCollectorCollectionMapModel()
		throws Exception
	{
		Collection<Statement> testList = new LinkedHashModel();
		Map<String, String> testNamespaces = new LinkedHashMap<String, String>();
		StatementCollector collector = new StatementCollector(testList, testNamespaces);

		// Actual variable is exactly the same, although it could be theoretically
		// wrapped and still be consistent
		assertTrue(testList == collector.getStatements());
		assertTrue(testNamespaces == collector.getNamespaces());
	}

	/**
	 * Test method for {@link org.openrdf.rio.helpers.RDFHandlerBase#startRDF()}.
	 */
	@Test
	public final void testStartRDF()
		throws Exception
	{
		StatementCollector testCollector = new StatementCollector();
		testCollector.startRDF();
	}

	/**
	 * Test method for {@link org.openrdf.rio.helpers.RDFHandlerBase#endRDF()}.
	 */
	@Test
	public final void testEndRDF()
		throws Exception
	{
		StatementCollector testCollector = new StatementCollector();
		testCollector.startRDF();
		testCollector.endRDF();
	}

	/**
	 * Test method for
	 * {@link org.openrdf.rio.helpers.RDFHandlerBase#handleComment(java.lang.String)}
	 * .
	 * 
	 * @throws Exception
	 */
	@Test
	public final void testHandleComment()
		throws Exception
	{
		StatementCollector testCollector = new StatementCollector();
		// StatementCollector must be able to handle comments, but does not
		// preserve them
		testCollector.handleComment("Comment");
	}

}
