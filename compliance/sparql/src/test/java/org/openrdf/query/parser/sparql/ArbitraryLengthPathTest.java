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
package org.openrdf.query.parser.sparql;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.openrdf.model.ValueFactory;
import org.openrdf.query.QueryLanguage;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.memory.MemoryStore;

/**
 * @author james
 */
public class ArbitraryLengthPathTest extends TestCase {

	private Repository repo;

	private RepositoryConnection con;

	@Before
	public void setUp()
		throws Exception
	{
		repo = new SailRepository(new MemoryStore());
		repo.initialize();
		con = repo.getConnection();
	}

	@After
	public void tearDown()
		throws Exception
	{
		con.close();
		repo.shutDown();
	}

	@Test
	public void test10()
		throws Exception
	{
		populate(10);
		String sparql = "ASK { <urn:test:root> <urn:test:hasChild>* <urn:test:node-end> }";
		assertTrue(con.prepareBooleanQuery(QueryLanguage.SPARQL, sparql).evaluate());
	}

	@Test
	public void test100()
		throws Exception
	{
		populate(100);
		String sparql = "ASK { <urn:test:root> <urn:test:hasChild>* <urn:test:node-end> }";
		assertTrue(con.prepareBooleanQuery(QueryLanguage.SPARQL, sparql).evaluate());
	}

	@Test
	public void test1000()
		throws Exception
	{
		populate(1000);
		String sparql = "ASK { <urn:test:root> <urn:test:hasChild>* <urn:test:node-end> }";
		assertTrue(con.prepareBooleanQuery(QueryLanguage.SPARQL, sparql).evaluate());
	}

	@Test
	public void test10000()
		throws Exception
	{
		populate(10000);
		String sparql = "ASK { <urn:test:root> <urn:test:hasChild>* <urn:test:node-end> }";
		assertTrue(con.prepareBooleanQuery(QueryLanguage.SPARQL, sparql).evaluate());
	}

	@Test
	public void test100000()
		throws Exception
	{
		populate(100000);
		String sparql = "ASK { <urn:test:root> <urn:test:hasChild>* <urn:test:node-end> }";
		assertTrue(con.prepareBooleanQuery(QueryLanguage.SPARQL, sparql).evaluate());
	}

	private void populate(int n)
		throws RepositoryException
	{
		ValueFactory vf = con.getValueFactory();
		for (int i = 0; i < n; i++) {
			con.add(vf.createIRI("urn:test:root"), vf.createIRI("urn:test:hasChild"),
					vf.createIRI("urn:test:node" + i));
		}
		con.add(vf.createIRI("urn:test:root"), vf.createIRI("urn:test:hasChild"),
				vf.createIRI("urn:test:node-end"));
	}

}
