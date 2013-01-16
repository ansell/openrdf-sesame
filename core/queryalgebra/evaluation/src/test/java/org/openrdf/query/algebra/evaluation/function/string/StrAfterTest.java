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
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;

/**
 * @author jeen
 */
public class StrAfterTest {

	private StrAfter strAfterFunc;

	private ValueFactory f = new ValueFactoryImpl();

	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp()
		throws Exception
	{
		strAfterFunc = new StrAfter();
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
			Literal result = strAfterFunc.evaluate(f, leftArg, rightArg);
			
			assertEquals("r", result.getLabel());
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
			Literal result = strAfterFunc.evaluate(f, leftArg, rightArg);
			
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
			Literal result = strAfterFunc.evaluate(f, leftArg, rightArg);
			
			assertEquals("ar", result.getLabel());
			assertEquals("en", result.getLanguage());
			assertEquals(null, result.getDatatype());
		}
		catch (ValueExprEvaluationException e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testEvaluate4() {
		
		Literal leftArg = f.createLiteral("foobar", XMLSchema.STRING);
		Literal rightArg = f.createLiteral("b");
		
		try {
			Literal result = strAfterFunc.evaluate(f, leftArg, rightArg);
			
			assertEquals("ar", result.getLabel());
			assertEquals(XMLSchema.STRING, result.getDatatype());

		}
		catch (ValueExprEvaluationException e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testEvaluate4a() {
		
		Literal leftArg = f.createLiteral("foobar");
		Literal rightArg = f.createLiteral("b", XMLSchema.STRING);
		
		try {
			Literal result = strAfterFunc.evaluate(f, leftArg, rightArg);
			
			assertEquals("ar", result.getLabel());
			assertEquals(null, result.getDatatype());

		}
		catch (ValueExprEvaluationException e) {
			fail(e.getMessage());
		}
	}
	
	
	@Test
	public void testEvaluate5() {
		
		Literal leftArg = f.createLiteral("foobar", XMLSchema.STRING);
		Literal rightArg = f.createLiteral("b", XMLSchema.DATE);
		
		try {
			Literal result = strAfterFunc.evaluate(f, leftArg, rightArg);
			
			fail("operand with incompatible datatype, should have resulted in error");
		}
		catch (ValueExprEvaluationException e) {
			assertEquals("incompatible operands for STRAFTER: \"foobar\"^^<http://www.w3.org/2001/XMLSchema#string>, \"b\"^^<http://www.w3.org/2001/XMLSchema#date>", e.getMessage());
		}
	}
	
	@Test
	public void testEvaluate6() {
		
		Literal leftArg = f.createLiteral(10);
		Literal rightArg = f.createLiteral("b");
		
		try {
			Literal result = strAfterFunc.evaluate(f, leftArg, rightArg);
			
			fail("operand with incompatible datatype, should have resulted in error");
		}
		catch (ValueExprEvaluationException e) {
			assertEquals("incompatible operands for STRAFTER: \"10\"^^<http://www.w3.org/2001/XMLSchema#int>, \"b\"", e.getMessage());
		}
	}
	
	
	@Test
	public void testEvaluate7() {
		
		URI leftArg = f.createURI("http://example.org/foobar");
		Literal rightArg = f.createLiteral("b");
		
		try {
			Literal result = strAfterFunc.evaluate(f, leftArg, rightArg);
			
			fail("operand of incompatible type, should have resulted in error");
		}
		catch (ValueExprEvaluationException e) {
			assertEquals("incompatible operands for STRAFTER: http://example.org/foobar, \"b\"", e.getMessage());
		}
	}
	
	@Test
	public void testEvaluate8() {
		Literal leftArg = f.createLiteral("foobar", "en");
		Literal rightArg = f.createLiteral("b", "nl");
		
		try {
			Literal result = strAfterFunc.evaluate(f, leftArg, rightArg);

			fail("operand of incompatible type, should have resulted in error");
		}
		catch (ValueExprEvaluationException e) {
			assertEquals("incompatible operands for STRAFTER: \"foobar\"@en, \"b\"@nl", e.getMessage());
		}
	}
	
	
	@Test
	public void testEvaluate9() {
		Literal leftArg = f.createLiteral("foobar");
		Literal rightArg = f.createLiteral("b", "nl");
		
		try {
			Literal result = strAfterFunc.evaluate(f, leftArg, rightArg);
			
			fail("operand of incompatible type, should have resulted in error");
		}
		catch (ValueExprEvaluationException e) {
			assertEquals("incompatible operands for STRAFTER: \"foobar\", \"b\"@nl", e.getMessage());
		}
	}
	
	@Test
	public void testEvaluate10() {
		Literal leftArg = f.createLiteral("foobar", "en");
		Literal rightArg = f.createLiteral("b", XMLSchema.STRING);
		
		try {
			Literal result = strAfterFunc.evaluate(f, leftArg, rightArg);
			
			assertEquals("ar", result.getLabel());
			assertEquals(null, result.getDatatype());
			assertEquals("en", result.getLanguage());

		}
		catch (ValueExprEvaluationException e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testEvaluate11() {
		Literal leftArg = f.createLiteral("foobar", "nl");
		Literal rightArg = f.createLiteral("b", "nl");
		
		try {
			Literal result = strAfterFunc.evaluate(f, leftArg, rightArg);
			
			assertEquals("ar", result.getLabel());
			assertEquals(null, result.getDatatype());
			assertEquals("nl", result.getLanguage());

		}
		catch (ValueExprEvaluationException e) {
			fail(e.getMessage());
		}
	}
	
}
