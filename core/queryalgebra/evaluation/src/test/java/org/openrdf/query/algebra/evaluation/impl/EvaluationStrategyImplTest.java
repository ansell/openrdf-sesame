/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2011.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.impl;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import info.aduna.iteration.CloseableIteration;

import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.Substring;
import org.openrdf.query.algebra.ValueConstant;
import org.openrdf.query.algebra.evaluation.TripleSource;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;
import org.openrdf.query.impl.EmptyBindingSet;

/**
 * Unit tests on {@link EvaluationStrategyImpl} methods.
 * 
 * @author Jeen Broekstra
 */
public class EvaluationStrategyImplTest {

	private EvaluationStrategyImpl evaluationStrategy;

	private ValueFactory valueFactory;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp()
		throws Exception
	{
		evaluationStrategy = new EvaluationStrategyImpl(new TripleSourceStub());
		valueFactory = new ValueFactoryImpl();
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
	 * {@link org.openrdf.query.algebra.evaluation.impl.EvaluationStrategyImpl#evaluate(org.openrdf.query.algebra.Substring, org.openrdf.query.BindingSet)}
	 * .
	 */
	@Test
	public void testEvaluateSubstring1() {
		ValueConstant stringExpr = new ValueConstant(valueFactory.createLiteral("test string"));
		ValueConstant startIndexExpr = new ValueConstant(valueFactory.createLiteral(2));

		Substring substring = new Substring(stringExpr, startIndexExpr);
		try {
			Value result = evaluationStrategy.evaluate(substring, new EmptyBindingSet());
			assertTrue(result instanceof Literal);

			Literal lit = (Literal)result;
			assertTrue(lit.getLanguage() == null);

			String lexicalValue = lit.getLabel();
			assertTrue("st string".equals(lexicalValue));
		}
		catch (QueryEvaluationException e) {
			fail(e.getMessage());
		}
	}

	/**
	 * Test method for
	 * {@link org.openrdf.query.algebra.evaluation.impl.EvaluationStrategyImpl#evaluate(org.openrdf.query.algebra.Substring, org.openrdf.query.BindingSet)}
	 * .
	 */
	@Test
	public void testEvaluateSubstring2() {
		ValueConstant invalidStringExpr = new ValueConstant(valueFactory.createURI("http://example.org/"));
		ValueConstant startIndexExpr = new ValueConstant(valueFactory.createLiteral(2));

		Substring substring = new Substring(invalidStringExpr, startIndexExpr);
		try {
			evaluationStrategy.evaluate(substring, new EmptyBindingSet());
		}
		catch (ValueExprEvaluationException e) {
			// do nothing, expected: a uri is not valid input for substring.
		}
		catch (QueryEvaluationException e) {
			fail("unexpected exception type : " + e.getMessage());
		}
	}

	/**
	 * Test method for
	 * {@link org.openrdf.query.algebra.evaluation.impl.EvaluationStrategyImpl#evaluate(org.openrdf.query.algebra.Substring, org.openrdf.query.BindingSet)}
	 * .
	 */
	@Test
	public void testEvaluateSubstring3() {
		ValueConstant stringExpr = new ValueConstant(valueFactory.createLiteral("test string"));
		ValueConstant invalidStartIndexExpr = new ValueConstant(valueFactory.createLiteral(11));

		Substring substring = new Substring(stringExpr, invalidStartIndexExpr);
		try {
			evaluationStrategy.evaluate(substring, new EmptyBindingSet());
		}
		catch (ValueExprEvaluationException e) {
			fail("unexpected exception type : " + e.getMessage());
		}
		catch (QueryEvaluationException e) {
			// do nothing, expected: index out of bounds should result in
			// QueryEvaluationException
		}
	}

	/**
	 * Stub implementation of the TripleSource interface, used for initializing
	 * the test object.
	 * 
	 * @author Jeen Broekstra
	 */
	private class TripleSourceStub implements TripleSource {

		public CloseableIteration<? extends Statement, QueryEvaluationException> getStatements(Resource subj,
				URI pred, Value obj, Resource... contexts)
			throws QueryEvaluationException
		{
			// TODO Auto-generated method stub
			return null;
		}

		public ValueFactory getValueFactory() {
			// TODO Auto-generated method stub
			return null;
		}

	}
}
