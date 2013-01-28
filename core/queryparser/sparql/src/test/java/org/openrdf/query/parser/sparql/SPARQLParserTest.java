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

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.openrdf.query.parser.ParsedQuery;


/**
 *
 * @author jeen
 */
public class SPARQLParserTest {

	private SPARQLParser parser;
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp()
		throws Exception
	{
		parser = new SPARQLParser();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown()
		throws Exception
	{
		parser = null;
	}

	/**
	 * Test method for {@link org.openrdf.query.parser.sparql.SPARQLParser#parseQuery(java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testSourceStringAssignment() throws Exception {
		String simpleSparqlQuery = "SELECT * WHERE {?X ?P ?Y }";
		
		ParsedQuery q = parser.parseQuery(simpleSparqlQuery, null);
		
		assertNotNull(q);
		assertEquals(simpleSparqlQuery, q.getSourceString());
	}

}
