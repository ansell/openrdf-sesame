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

import org.openrdf.query.algebra.Join;
import org.openrdf.query.algebra.Slice;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.parser.ParsedQuery;
import org.openrdf.query.parser.sparql.ast.ParseException;
import org.openrdf.query.parser.sparql.ast.TokenMgrError;
import org.openrdf.query.parser.sparql.ast.VisitorException;

/**
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
	 * Test method for
	 * {@link org.openrdf.query.parser.sparql.SPARQLParser#parseQuery(java.lang.String, java.lang.String)}
	 * .
	 */
	@Test
	public void testSourceStringAssignment()
		throws Exception
	{
		String simpleSparqlQuery = "SELECT * WHERE {?X ?P ?Y }";

		ParsedQuery q = parser.parseQuery(simpleSparqlQuery, null);

		assertNotNull(q);
		assertEquals(simpleSparqlQuery, q.getSourceString());
	}

	@Test
	public void testSES1922PathSequenceWithValueConstant()
		throws Exception
	{

		StringBuilder qb = new StringBuilder();
		qb.append("ASK {?A (<foo:bar>)/<foo:foo> <foo:objValue>} ");

		ParsedQuery q = parser.parseQuery(qb.toString(), null);
		TupleExpr te = q.getTupleExpr();

		assertNotNull(te);

		assertTrue(te instanceof Slice);
		Slice s = (Slice)te;
		assertTrue(s.getArg() instanceof Join);
		Join j = (Join)s.getArg();

		assertTrue(j.getLeftArg() instanceof StatementPattern);
		assertTrue(j.getRightArg() instanceof StatementPattern);
		StatementPattern leftArg = (StatementPattern)j.getLeftArg();
		StatementPattern rightArg = (StatementPattern)j.getRightArg();

		assertTrue(leftArg.getObjectVar().equals(rightArg.getSubjectVar()));
		assertEquals(leftArg.getObjectVar().getName(), rightArg.getSubjectVar().getName());
	}
	
	@Test
	public void testSES1927UnequalLiteralValueConstants1()
		throws Exception
	{

		StringBuilder qb = new StringBuilder();
		qb.append("ASK {?a <foo:bar> \"test\". ?a <foo:foo> \"test\"@en .} ");

		ParsedQuery q = parser.parseQuery(qb.toString(), null);
		TupleExpr te = q.getTupleExpr();

		assertNotNull(te);

		assertTrue(te instanceof Slice);
		Slice s = (Slice)te;
		assertTrue(s.getArg() instanceof Join);
		Join j = (Join)s.getArg();

		assertTrue(j.getLeftArg() instanceof StatementPattern);
		assertTrue(j.getRightArg() instanceof StatementPattern);
		StatementPattern leftArg = (StatementPattern)j.getLeftArg();
		StatementPattern rightArg = (StatementPattern)j.getRightArg();

		assertFalse(leftArg.getObjectVar().equals(rightArg.getObjectVar()));
		assertNotEquals(leftArg.getObjectVar().getName(), rightArg.getObjectVar().getName());
	}
	
	@Test
	public void testSES1927UnequalLiteralValueConstants2()
		throws Exception
	{

		StringBuilder qb = new StringBuilder();
		qb.append("ASK {?a <foo:bar> \"test\". ?a <foo:foo> \"test\"^^<foo:bar> .} ");

		ParsedQuery q = parser.parseQuery(qb.toString(), null);
		TupleExpr te = q.getTupleExpr();

		assertNotNull(te);

		assertTrue(te instanceof Slice);
		Slice s = (Slice)te;
		assertTrue(s.getArg() instanceof Join);
		Join j = (Join)s.getArg();

		assertTrue(j.getLeftArg() instanceof StatementPattern);
		assertTrue(j.getRightArg() instanceof StatementPattern);
		StatementPattern leftArg = (StatementPattern)j.getLeftArg();
		StatementPattern rightArg = (StatementPattern)j.getRightArg();
		
		assertFalse(leftArg.getObjectVar().equals(rightArg.getObjectVar()));
		assertNotEquals(leftArg.getObjectVar().getName(), rightArg.getObjectVar().getName());
	}
}
