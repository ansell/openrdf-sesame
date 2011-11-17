/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2011.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.function.string;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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
public class StrBeforeTest {

	private StrBefore strBeforeFunc;

	private ValueFactory f = new ValueFactoryImpl();

	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp()
		throws Exception
	{
		strBeforeFunc = new StrBefore();
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
		
		Literal leftArg = f.createLiteral("foobar");
		Literal rightArg = f.createLiteral("ba");
		
		try {
			Literal result = strBeforeFunc.evaluate(f, leftArg, rightArg);
			
			assertEquals("foo", result.getLabel());
		}
		catch (ValueExprEvaluationException e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testEvaluate2() {
		
		Literal leftArg = f.createLiteral("foobar");
		Literal rightArg = f.createLiteral("xyz");
		
		try {
			Literal result = strBeforeFunc.evaluate(f, leftArg, rightArg);
			
			assertEquals("", result.getLabel());
		}
		catch (ValueExprEvaluationException e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testEvaluate3() {
		
		Literal leftArg = f.createLiteral("foobar", "en");
		Literal rightArg = f.createLiteral("b");
		
		try {
			Literal result = strBeforeFunc.evaluate(f, leftArg, rightArg);
			
			assertEquals("foo", result.getLabel());
		}
		catch (ValueExprEvaluationException e) {
			fail(e.getMessage());
		}
	}

}
