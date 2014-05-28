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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.openrdf.query.algebra.Extension;
import org.openrdf.query.algebra.Order;
import org.openrdf.query.algebra.Projection;
import org.openrdf.query.algebra.Service;
import org.openrdf.query.algebra.SingletonSet;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.parser.ParsedQuery;
import org.openrdf.query.parser.sparql.ast.ASTQuery;
import org.openrdf.query.parser.sparql.ast.ASTQueryContainer;
import org.openrdf.query.parser.sparql.ast.ASTServiceGraphPattern;
import org.openrdf.query.parser.sparql.ast.ParseException;
import org.openrdf.query.parser.sparql.ast.SyntaxTreeBuilder;
import org.openrdf.query.parser.sparql.ast.TokenMgrError;
import org.openrdf.query.parser.sparql.ast.VisitorException;

/**
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
	public void testServiceGraphPatternStringDetection1()
		throws TokenMgrError, ParseException, VisitorException
	{

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

		assertTrue(f.getGraphPatterns().size() == 1);
		assertTrue(servicePattern.equals(f.getGraphPatterns().get(0)));
	}

	@Test
	public void testServiceGraphPatternStringDetection2()
		throws TokenMgrError, ParseException, VisitorException
	{

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

		assertTrue(f.getGraphPatterns().size() == 1);
		assertTrue(servicePattern.equals(f.getGraphPatterns().get(0)));
	}

	@Test
	public void testServiceGraphPatternStringDetection3()
		throws TokenMgrError, ParseException, VisitorException
	{

		String servicePattern1 = "SERVICE <foo:bar> \n { ?x <foo:baz> ?y. }";
		String servicePattern2 = "SERVICE <foo:bar2> \n { ?x <foo:baz> ?y. }";

		StringBuilder qb = new StringBuilder();
		qb.append("SELECT * \n");
		qb.append("WHERE { \n");
		qb.append(servicePattern1);
		qb.append(" OPTIONAL { \n");
		qb.append(servicePattern2);
		qb.append("    } \n");
		qb.append(" } ");

		ASTQueryContainer qc = SyntaxTreeBuilder.parseQuery(qb.toString());

		ServiceNodeFinder f = new ServiceNodeFinder();
		f.visit(qc, null);

		assertTrue(f.getGraphPatterns().size() == 2);
		assertTrue(servicePattern1.equals(f.getGraphPatterns().get(0)));
		assertTrue(servicePattern2.equals(f.getGraphPatterns().get(1)));
	}

	@Test
	public void testServiceGraphPatternStringDetection4()
		throws TokenMgrError, ParseException, VisitorException
	{

		String servicePattern1 = "SERVICE <http://localhost:18080/openrdf/repositories/endpoint1> {  ?s ?p ?o1 . "
				+ "OPTIONAL {	SERVICE SILENT <http://invalid.endpoint.org/sparql> { ?s ?p2 ?o2 } } }";

		String servicePattern2 = "SERVICE SILENT <http://invalid.endpoint.org/sparql> { ?s ?p2 ?o2 }";

		StringBuilder qb = new StringBuilder();
		qb.append("SELECT * \n");
		qb.append("WHERE { \n");
		qb.append(servicePattern1);
		qb.append(" } ");

		ASTQueryContainer qc = SyntaxTreeBuilder.parseQuery(qb.toString());

		ServiceNodeFinder f = new ServiceNodeFinder();
		f.visit(qc, null);

		assertTrue(f.getGraphPatterns().size() == 2);
		assertTrue(servicePattern1.equals(f.getGraphPatterns().get(0)));
		assertTrue(servicePattern2.equals(f.getGraphPatterns().get(1)));
	}

	@Test
	public void testServiceGraphPatternChopping()
		throws Exception
	{

		// just for construction
		Service service = new Service(null, new SingletonSet(), "", null, null, false);

		service.setExpressionString("SERVICE <a> { ?s ?p ?o }");
		assertEquals("?s ?p ?o", service.getServiceExpressionString());

		service.setExpressionString("SERVICE <a> {?s ?p ?o}");
		assertEquals("?s ?p ?o", service.getServiceExpressionString());

	}

	@Test
	public void testOrderByWithAliases1() throws Exception
	{
		String queryString = " SELECT ?x (SUM(?v1)/COUNT(?v1) as ?r) WHERE { ?x <urn:foo> ?v1 } GROUP BY ?x ORDER BY ?r"; 


		SPARQLParser parser = new SPARQLParser();
		ParsedQuery query = parser.parseQuery(queryString, null);
		
		assertNotNull(query);
		TupleExpr te = query.getTupleExpr();
		
		assertTrue(te instanceof Projection);
		
		te = ((Projection)te).getArg();
		
		assertTrue(te instanceof Order);
		
		te = ((Order)te).getArg();
		
		assertTrue(te instanceof Extension);
	}
	
	
	private class ServiceNodeFinder extends ASTVisitorBase {

		private List<String> graphPatterns = new ArrayList<String>();

		@Override
		public Object visit(ASTServiceGraphPattern node, Object data)
			throws VisitorException
		{
			graphPatterns.add(node.getPatternString());
			return super.visit(node, data);
		}

		public List<String> getGraphPatterns() {
			return graphPatterns;
		}
	}
}
