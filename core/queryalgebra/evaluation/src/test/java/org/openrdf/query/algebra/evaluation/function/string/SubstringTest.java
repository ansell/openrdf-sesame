/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2011.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.function.string;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.openrdf.model.Literal;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;

/**
 * @author jeen
 */
public class SubstringTest {

	private Substring substrFunc;

	private ValueFactory f = new ValueFactoryImpl();

	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp()
		throws Exception
	{
		substrFunc = new Substring();
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
	public void testEvaluate1() {
		
		Literal pattern = f.createLiteral("foobar");
		Literal startIndex = f.createLiteral(3);
		
		try {
			Literal result = substrFunc.evaluate(f, pattern, startIndex);
			
			assertTrue(result.getLabel().equals("bar"));
		}
		catch (ValueExprEvaluationException e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testEvaluate2() {
		
		Literal pattern = f.createLiteral("foobar");
		Literal startIndex = f.createLiteral(3);
		Literal length = f.createLiteral(2);
		
		try {
			Literal result = substrFunc.evaluate(f, pattern, startIndex, length);
			
			assertTrue(result.getLabel().equals("ba"));
		}
		catch (ValueExprEvaluationException e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testEvaluate3() {
		
		Literal pattern = f.createLiteral("foobar");
		Literal startIndex = f.createLiteral(3);
		Literal length = f.createLiteral(5);
		
		try {
			Literal result = substrFunc.evaluate(f, pattern, startIndex, length);
			fail("illegal length spec should have resulted in error");
		}
		catch (ValueExprEvaluationException e) {
			// do nothing, expected
		}
	}

	
	@Test
	public void testEvaluate4() {
		
		Literal pattern = f.createLiteral("foobar");
		
		try {
			Literal result = substrFunc.evaluate(f, pattern);
			fail("illegal number of args hould have resulted in error");
		}
		catch (ValueExprEvaluationException e) {
			// do nothing, expected
		}
	}
}
