/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2011.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.EmptyIteration;

import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.BooleanLiteralImpl;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.StrLen;
import org.openrdf.query.algebra.StrStarts;
import org.openrdf.query.algebra.Substring;
import org.openrdf.query.algebra.ValueConstant;
import org.openrdf.query.algebra.evaluation.TripleSource;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;
import org.openrdf.query.impl.EmptyBindingSet;

/**
 * Unit tests on {@link EvaluationStrategyImpl} methods, specifically on input
 * validation behavior for various functions and operators in the query algebra.
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
	 * {@link org.openrdf.query.algebra.evaluation.impl.EvaluationStrategyImpl#evaluate(org.openrdf.query.algebra.StrStarts, org.openrdf.query.BindingSet)}
	 * .
	 */
	@Test
	public void testEvaluateStrStarts1() {
		
		ValueConstant leftExpr = new ValueConstant(valueFactory.createLiteral("foobar"));
		ValueConstant rightExpr = new ValueConstant(valueFactory.createLiteral("foo"));

		StrStarts strLen = new StrStarts(leftExpr, rightExpr);
		
		try {
			Value result = evaluationStrategy.evaluate(strLen, new EmptyBindingSet());
			assertTrue(result instanceof Literal);

			Literal lit = (Literal)result;
			assertEquals(BooleanLiteralImpl.TRUE, lit);
		}
		catch (QueryEvaluationException e) {
			fail(e.getMessage());
		}
	}
	
	/**
	 * Test method for
	 * {@link org.openrdf.query.algebra.evaluation.impl.EvaluationStrategyImpl#evaluate(org.openrdf.query.algebra.StrStarts, org.openrdf.query.BindingSet)}
	 * .
	 */
	@Test
	public void testEvaluateStrStarts2() {
		
		ValueConstant leftExpr = new ValueConstant(valueFactory.createLiteral("foobar"));
		ValueConstant rightExpr = new ValueConstant(valueFactory.createLiteral("foo", "en"));

		StrStarts strLen = new StrStarts(leftExpr, rightExpr);
		
		try {
			evaluationStrategy.evaluate(strLen, new EmptyBindingSet());
			fail("evaluation completed succesfully, type error expected");
		}
		catch (ValueExprEvaluationException e) {
			// do nothing, type error is expected.
		}
		catch (QueryEvaluationException e) {
			fail("unexpected exception type : " + e.getMessage());
		}
	}
	
	/**
	 * Test method for
	 * {@link org.openrdf.query.algebra.evaluation.impl.EvaluationStrategyImpl#evaluate(org.openrdf.query.algebra.StrStarts, org.openrdf.query.BindingSet)}
	 * .
	 */
	@Test
	public void testEvaluateStrStarts3() {
		
		ValueConstant leftExpr = new ValueConstant(valueFactory.createLiteral("foobar", "nl"));
		ValueConstant rightExpr = new ValueConstant(valueFactory.createLiteral("foo", "en"));

		StrStarts strLen = new StrStarts(leftExpr, rightExpr);
		
		try {
			evaluationStrategy.evaluate(strLen, new EmptyBindingSet());
			fail("evaluation completed succesfully, type error expected");
		}
		catch (ValueExprEvaluationException e) {
			// do nothing, type error is expected.
		}
		catch (QueryEvaluationException e) {
			fail("unexpected exception type : " + e.getMessage());
		}
	}
	
	/**
	 * Test method for
	 * {@link org.openrdf.query.algebra.evaluation.impl.EvaluationStrategyImpl#evaluate(org.openrdf.query.algebra.StrStarts, org.openrdf.query.BindingSet)}
	 * .
	 */
	@Test
	public void testEvaluateStrStarts4() {
		
		ValueConstant leftExpr = new ValueConstant(valueFactory.createLiteral("foobar"));
		ValueConstant rightExpr = new ValueConstant(valueFactory.createLiteral("foo", XMLSchema.STRING));

		StrStarts strLen = new StrStarts(leftExpr, rightExpr);
		
		try {
			Value result = evaluationStrategy.evaluate(strLen, new EmptyBindingSet());
			assertTrue(result instanceof Literal);

			Literal lit = (Literal)result;
			assertEquals(BooleanLiteralImpl.TRUE, lit);
		}
		catch (QueryEvaluationException e) {
			fail(e.getMessage());
		}
	}
	
	/**
	 * Test method for
	 * {@link org.openrdf.query.algebra.evaluation.impl.EvaluationStrategyImpl#evaluate(org.openrdf.query.algebra.StrStarts, org.openrdf.query.BindingSet)}
	 * .
	 */
	@Test
	public void testEvaluateStrStarts5() {
		
		ValueConstant leftExpr = new ValueConstant(valueFactory.createLiteral("foobar", "en"));
		ValueConstant rightExpr = new ValueConstant(valueFactory.createLiteral("foo"));

		StrStarts strLen = new StrStarts(leftExpr, rightExpr);
		
		try {
			Value result = evaluationStrategy.evaluate(strLen, new EmptyBindingSet());
			assertTrue(result instanceof Literal);

			Literal lit = (Literal)result;
			assertEquals(BooleanLiteralImpl.TRUE, lit);
		}
		catch (QueryEvaluationException e) {
			fail(e.getMessage());
		}
	}
	
	/**
	 * Test method for
	 * {@link org.openrdf.query.algebra.evaluation.impl.EvaluationStrategyImpl#evaluate(org.openrdf.query.algebra.StrStarts, org.openrdf.query.BindingSet)}
	 * .
	 */
	@Test
	public void testEvaluateStrStarts6() {
		
		ValueConstant leftExpr = new ValueConstant(valueFactory.createLiteral("foobar", "en"));
		ValueConstant rightExpr = new ValueConstant(valueFactory.createLiteral("foo", XMLSchema.STRING));

		StrStarts strLen = new StrStarts(leftExpr, rightExpr);
		
		try {
			Value result = evaluationStrategy.evaluate(strLen, new EmptyBindingSet());
			assertTrue(result instanceof Literal);

			Literal lit = (Literal)result;
			assertEquals(BooleanLiteralImpl.TRUE, lit);
		}
		catch (QueryEvaluationException e) {
			fail(e.getMessage());
		}
	}
	
	/**
	 * Test method for
	 * {@link org.openrdf.query.algebra.evaluation.impl.EvaluationStrategyImpl#evaluate(org.openrdf.query.algebra.StrLen, org.openrdf.query.BindingSet)}
	 * .
	 */
	@Test
	public void testEvaluateStrLen1() {
		
		String testString = "test string";
		ValueConstant stringExpr = new ValueConstant(valueFactory.createLiteral(testString));
		
		StrLen strLen = new StrLen(stringExpr);
		
		try {
			Value result = evaluationStrategy.evaluate(strLen, new EmptyBindingSet());
			assertTrue(result instanceof Literal);

			Literal lit = (Literal)result;
			assertTrue(lit.getLanguage() == null);
			assertTrue(XMLSchema.INTEGER.equals(lit.getDatatype()));

			int intVal = lit.intValue();
			assertEquals(testString.length(), intVal);
		}
		catch (QueryEvaluationException e) {
			fail(e.getMessage());
		}
	}
	
	/**
	 * Test method for
	 * {@link org.openrdf.query.algebra.evaluation.impl.EvaluationStrategyImpl#evaluate(org.openrdf.query.algebra.StrLen, org.openrdf.query.BindingSet)}
	 * .
	 */
	@Test
	public void testEvaluateStrLen2() {
		
		ValueConstant invalidStringExpr = new ValueConstant(valueFactory.createLiteral(10));
		
		StrLen strLen = new StrLen(invalidStringExpr);
		
		try {
			evaluationStrategy.evaluate(strLen, new EmptyBindingSet());
			fail("evaluation completed succesfully, type error expected");
		}
		catch (ValueExprEvaluationException e) {
			// do nothing, type error is expected: an integer is not valid input for
			// strlen.
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
			fail("evaluation completed succesfully, type error expected");
		}
		catch (ValueExprEvaluationException e) {
			// do nothing, type error is expected: a uri is not valid input for
			// substring.
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
		ValueConstant endIndexExpr = new ValueConstant(valueFactory.createLiteral(6));

		Substring substring = new Substring(stringExpr, invalidStartIndexExpr, endIndexExpr);
		try {
			evaluationStrategy.evaluate(substring, new EmptyBindingSet());
			fail("evaluation completed succesfully, QueryEvaluationException expected");
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
	 * Test method for
	 * {@link org.openrdf.query.algebra.evaluation.impl.EvaluationStrategyImpl#evaluate(org.openrdf.query.algebra.Substring, org.openrdf.query.BindingSet)}
	 * .
	 */
	@Test
	public void testEvaluateSubstring4() {
		ValueConstant stringExpr = new ValueConstant(valueFactory.createLiteral("test string"));
		ValueConstant startIndexExpr = new ValueConstant(valueFactory.createLiteral(2));
		ValueConstant invalidEndIndexExpr = new ValueConstant(valueFactory.createLiteral(100));

		Substring substring = new Substring(stringExpr, startIndexExpr, invalidEndIndexExpr);
		try {
			evaluationStrategy.evaluate(substring, new EmptyBindingSet());
			fail("evaluation completed succesfully, QueryEvaluationException expected");
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
			return new EmptyIteration<Statement, QueryEvaluationException>();
		}

		public ValueFactory getValueFactory() {
			return new ValueFactoryImpl();
		}

	}
}
