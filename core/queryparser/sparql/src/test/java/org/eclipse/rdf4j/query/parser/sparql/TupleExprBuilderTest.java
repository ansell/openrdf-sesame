/*******************************************************************************
 * Copyright (c) 2015, Eclipse Foundation, Inc. and its licensors.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * - Neither the name of the Eclipse Foundation, Inc. nor the names of its
 *   contributors may be used to endorse or promote products derived from this
 *   software without specific prior written permission. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package org.eclipse.rdf4j.query.parser.sparql;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.rdf4j.model.impl.ValueFactoryImpl;
import org.eclipse.rdf4j.query.algebra.Extension;
import org.eclipse.rdf4j.query.algebra.Order;
import org.eclipse.rdf4j.query.algebra.Projection;
import org.eclipse.rdf4j.query.algebra.Service;
import org.eclipse.rdf4j.query.algebra.SingletonSet;
import org.eclipse.rdf4j.query.algebra.TupleExpr;
import org.eclipse.rdf4j.query.parser.ParsedQuery;
import org.eclipse.rdf4j.query.parser.sparql.AbstractASTVisitor;
import org.eclipse.rdf4j.query.parser.sparql.TupleExprBuilder;
import org.eclipse.rdf4j.query.parser.sparql.ast.ASTAskQuery;
import org.eclipse.rdf4j.query.parser.sparql.ast.ASTQueryContainer;
import org.eclipse.rdf4j.query.parser.sparql.ast.ASTServiceGraphPattern;
import org.eclipse.rdf4j.query.parser.sparql.ast.ParseException;
import org.eclipse.rdf4j.query.parser.sparql.ast.SyntaxTreeBuilder;
import org.eclipse.rdf4j.query.parser.sparql.ast.TokenMgrError;
import org.eclipse.rdf4j.query.parser.sparql.ast.VisitorException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

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
	public void testAskQuerySolutionModifiers() {
		String query = "ASK WHERE { ?foo ?bar ?baz . } ORDER BY ?foo LIMIT 1";
		
		try {
			TupleExprBuilder builder = new TupleExprBuilder(ValueFactoryImpl.getInstance());
			ASTQueryContainer qc = SyntaxTreeBuilder.parseQuery(query);
			TupleExpr result = builder.visit(qc, null);
			assertTrue(result instanceof Order);
		}
		catch (Exception e) {
			e.printStackTrace();
			fail("should parse ask query with solution modifiers");
		}
		
		
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

	private class ServiceNodeFinder extends AbstractASTVisitor {

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
