/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2011.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.parser.sparql;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.query.algebra.Service;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;
import org.openrdf.query.parser.sparql.ast.ASTQueryContainer;
import org.openrdf.query.parser.sparql.ast.ASTServiceGraphPattern;
import org.openrdf.query.parser.sparql.ast.ParseException;
import org.openrdf.query.parser.sparql.ast.SyntaxTreeBuilder;
import org.openrdf.query.parser.sparql.ast.TokenMgrError;
import org.openrdf.query.parser.sparql.ast.VisitorException;


/**
 *
 * @author jeen
 */
public class TupleExprBuilderTest {

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

	@Test
	public void testServiceGraphPatternStringDetection1() throws TokenMgrError, ParseException, VisitorException {
		
		String servicePattern = "SERVICE <foo:bar> { ?x <foo:baz> ?y }";
		
		StringBuilder qb = new StringBuilder();
		qb.append("SELECT * \n");
		qb.append("WHERE { \n");
		qb.append(" { ?s ?p ?o } \n");
		qb.append(" UNION \n");
		qb.append(" { ?p ?q ?r } \n");
		qb.append(servicePattern);
		qb.append("\n");
		qb.append(" FILTER (?s = <foo:bar>) ");
		qb.append(" } ");
	
		ASTQueryContainer qc = SyntaxTreeBuilder.parseQuery(qb.toString());
		
		ServiceNodeFinder f = new ServiceNodeFinder();
		f.visit(qc, null);
		
		assertTrue(f.graphPattern != null);
		assertEquals(servicePattern, f.graphPattern);
	}
	
	@Test
	public void testServiceGraphPatternStringDetection2() throws TokenMgrError, ParseException, VisitorException {
		
		String servicePattern = "SERVICE <foo:bar> \r\n { ?x <foo:baz> ?y. \r\n \r\n }";
		
		StringBuilder qb = new StringBuilder();
		qb.append("SELECT * \n");
		qb.append("WHERE { \n");
		qb.append(" { ?s ?p ?o } \n");
		qb.append(" UNION \n");
		qb.append(" { ?p ?q ?r } \n");
		qb.append(servicePattern);
		qb.append("\n");
		qb.append(" FILTER (?s = <foo:bar>) ");
		qb.append(" } ");
	
		ASTQueryContainer qc = SyntaxTreeBuilder.parseQuery(qb.toString());
		
		ServiceNodeFinder f = new ServiceNodeFinder();
		f.visit(qc, null);
		
		assertTrue(f.graphPattern != null);
		assertTrue(servicePattern.equals(f.graphPattern));
	}
	
	private class ServiceNodeFinder extends ASTVisitorBase {
		
		public String graphPattern;
		
		@Override
		public Object visit(ASTServiceGraphPattern node, Object data) throws VisitorException {
			graphPattern = node.getPatternString();
			return super.visit(node, data);
		}
	}
}
