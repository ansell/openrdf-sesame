/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.openrdf.model.BNode;
import org.openrdf.model.Graph;
import org.openrdf.model.Literal;
import org.openrdf.model.Model;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.query.impl.EmptyBindingSet;
import org.openrdf.query.impl.ListBindingSet;
import org.openrdf.query.impl.MutableTupleQueryResult;

/**
 * @author Arjohn Kampman
 */
public class QueryResultsTest extends TestCase {

	private MutableTupleQueryResult tqr1;

	private MutableTupleQueryResult tqr2;

	private MutableTupleQueryResult tqr3;

	private GraphQueryResult gqr;
	
	private static ValueFactory VF = new ValueFactoryImpl();

	private List<String> twoBindingNames = Arrays.asList("a", "b");

	private List<String> threeBindingNames = Arrays.asList("a", "b", "c");

	private URI foo;

	private URI bar;

	private BNode bnode1;

	private BNode bnode2;

	private Literal lit1;

	private Literal lit2;
	
	private URI a = VF.createURI("urn:a");
	private URI b = VF.createURI("urn:b");
	private URI c = VF.createURI("urn:c");
	private URI p = VF.createURI("urn:p");
	private URI q = VF.createURI("urn:q");

	@Override
	protected void setUp() {
		tqr1 = new MutableTupleQueryResult(twoBindingNames);
		tqr2 = new MutableTupleQueryResult(twoBindingNames);
		tqr3 = new MutableTupleQueryResult(threeBindingNames);

		gqr = new StubGraphQueryResult();
		
		foo = VF.createURI("http://example.org/foo");
		bar = VF.createURI("http://example.org/bar");

		bnode1 = VF.createBNode();
		bnode2 = VF.createBNode();

		lit1 = VF.createLiteral(1);
		lit2 = VF.createLiteral("test", "en");
	}

	public void testAsModel()
		throws QueryEvaluationException
	{
		Model model = QueryResults.asModel(gqr);
	
		assertFalse(gqr.hasNext());
		assertNotNull(model);
		assertTrue(model.contains(VF.createStatement(a, p, b)));
	}
	
	public void testAsGraph()
			throws QueryEvaluationException
		{
			Graph graph = QueryResults.asGraph(gqr);
		
			assertFalse(gqr.hasNext());
			assertNotNull(graph);
			assertTrue(graph.contains(VF.createStatement(a, p, b)));
		}
		
	private class StubGraphQueryResult implements GraphQueryResult {

		private List<Statement> statements = new ArrayList<Statement>();

		
		public StubGraphQueryResult() {
			statements.add(VF.createStatement(a, p, b));
			statements.add(VF.createStatement(b, q, c));
			statements.add(VF.createStatement(c, q, a));
		}
		
		public void close()
			throws QueryEvaluationException
		{
			// TODO Auto-generated method stub
		}

		public boolean hasNext()
			throws QueryEvaluationException
		{
			return !statements.isEmpty();
		}

		public Statement next()
			throws QueryEvaluationException
		{
			return statements.remove(0);
		}

		public void remove()
			throws QueryEvaluationException
		{
			statements.remove(0);
		}

		public Map<String, String> getNamespaces()
			throws QueryEvaluationException
		{
			// TODO Auto-generated method stub
			return null;
		}

	}

	public void testEmptyQueryResult()
		throws QueryEvaluationException
	{
		tqr1.append(EmptyBindingSet.getInstance());
		tqr2.append(EmptyBindingSet.getInstance());
		tqr3.append(EmptyBindingSet.getInstance());

		assertTrue(QueryResults.equals(tqr1, tqr2));
	}

	public void testEmptyQueryResult2()
		throws QueryEvaluationException
	{
		tqr1.append(EmptyBindingSet.getInstance());
		tqr3.append(EmptyBindingSet.getInstance());

		assertTrue(QueryResults.equals(tqr1, tqr3));
	}

	public void testEmptyQueryResult3()
		throws QueryEvaluationException
	{
		tqr1.append(EmptyBindingSet.getInstance());
		tqr3.append(EmptyBindingSet.getInstance());

		assertTrue(QueryResults.equals(tqr3, tqr1));
	}

	public void testEmptyBindingSet()
		throws QueryEvaluationException
	{
		tqr1.append(EmptyBindingSet.getInstance());
		tqr2.append(EmptyBindingSet.getInstance());
		assertTrue(QueryResults.equals(tqr1, tqr2));
	}

	public void testNonBNodeBindingSet1()
		throws QueryEvaluationException
	{
		tqr1.append(new ListBindingSet(twoBindingNames, foo, lit1));
		tqr1.append(new ListBindingSet(twoBindingNames, bar, lit2));

		tqr2.append(new ListBindingSet(twoBindingNames, bar, lit2));
		tqr2.append(new ListBindingSet(twoBindingNames, foo, lit1));

		assertTrue(QueryResults.equals(tqr1, tqr2));
	}

	public void testNonBNodeBindingSet2()
		throws QueryEvaluationException
	{
		tqr1.append(new ListBindingSet(twoBindingNames, foo, lit1));
		tqr2.append(new ListBindingSet(twoBindingNames, foo, lit2));

		assertFalse(QueryResults.equals(tqr1, tqr2));
	}

	public void testNonBNodeBindingSet3()
		throws QueryEvaluationException
	{
		tqr3.append(new ListBindingSet(threeBindingNames, foo, lit1, bar));
		tqr2.append(new ListBindingSet(twoBindingNames, foo, lit1));

		assertFalse(QueryResults.equals(tqr3, tqr2));
	}

	public void testNonBNodeBindingSet()
		throws QueryEvaluationException
	{
		tqr1.append(new ListBindingSet(twoBindingNames, foo, lit1));
		tqr2.append(new ListBindingSet(twoBindingNames, foo, lit2));

		assertFalse(QueryResults.equals(tqr1, tqr2));
	}

	public void testBNodeBindingSet1()
		throws QueryEvaluationException
	{
		tqr1.append(new ListBindingSet(twoBindingNames, foo, bnode1));
		tqr1.append(new ListBindingSet(twoBindingNames, bar, bnode2));

		tqr2.append(new ListBindingSet(twoBindingNames, foo, bnode2));
		tqr2.append(new ListBindingSet(twoBindingNames, bar, bnode1));

		assertTrue(QueryResults.equals(tqr1, tqr2));
	}

	public void testBNodeBindingSet2()
		throws QueryEvaluationException
	{
		tqr1.append(new ListBindingSet(twoBindingNames, foo, bnode1));
		tqr2.append(new ListBindingSet(twoBindingNames, foo, lit1));

		assertFalse(QueryResults.equals(tqr1, tqr2));
	}

	public void testBNodeBindingSet3()
		throws QueryEvaluationException
	{
		tqr1.append(new ListBindingSet(twoBindingNames, foo, bnode1));
		tqr1.append(new ListBindingSet(twoBindingNames, foo, bnode2));

		tqr2.append(new ListBindingSet(twoBindingNames, foo, bnode1));
		tqr2.append(new ListBindingSet(twoBindingNames, foo, bnode1));

		assertFalse(QueryResults.equals(tqr1, tqr2));
	}

	public void testBNodeBindingSet4()
		throws QueryEvaluationException
	{
		tqr1.append(new ListBindingSet(twoBindingNames, bnode1, bnode2));
		tqr1.append(new ListBindingSet(twoBindingNames, foo, bnode2));

		tqr2.append(new ListBindingSet(twoBindingNames, bnode2, bnode1));
		tqr2.append(new ListBindingSet(twoBindingNames, foo, bnode1));

		assertTrue(QueryResults.equals(tqr1, tqr2));
	}

	public void testBNodeBindingSet5()
		throws QueryEvaluationException
	{
		tqr1.append(new ListBindingSet(twoBindingNames, bnode1, bnode2));
		tqr1.append(new ListBindingSet(twoBindingNames, foo, bnode2));

		tqr2.append(new ListBindingSet(twoBindingNames, bnode2, bnode1));
		tqr2.append(new ListBindingSet(twoBindingNames, foo, bnode2));

		assertFalse(QueryResults.equals(tqr1, tqr2));
	}

	public void testBNodeBindingSet6()
		throws QueryEvaluationException
	{
		tqr3.append(new ListBindingSet(threeBindingNames, foo, bnode2, bnode1));
		tqr1.append(new ListBindingSet(twoBindingNames, foo, bnode2));

		assertFalse(QueryResults.equals(tqr3, tqr1));
	}

	public void testBNodeBindingSet7()
		throws QueryEvaluationException
	{
		tqr3.append(new ListBindingSet(threeBindingNames, foo, bnode2, bnode1));
		tqr1.append(new ListBindingSet(twoBindingNames, foo, bnode2));

		assertFalse(QueryResults.equals(tqr1, tqr3));
	}
}
