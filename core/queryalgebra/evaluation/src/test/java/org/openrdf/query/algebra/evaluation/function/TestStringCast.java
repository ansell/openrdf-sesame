/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2011.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.function;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.openrdf.model.Literal;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.datatypes.XMLDatatypeUtil;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;

/**
 * @author jeen
 */
public class TestStringCast {

	private StringCast stringCast;

	private ValueFactory f = new ValueFactoryImpl();
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp()
		throws Exception
	{
		stringCast = new StringCast();
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
	public void testCastPlainLiteral() {
		Literal plainLit = f.createLiteral("foo");
		try {
			Literal result = stringCast.evaluate(f, plainLit);
			assertNotNull(result);
			assertEquals(XMLSchema.STRING, result.getDatatype());
		}
		catch (ValueExprEvaluationException e) {
			fail(e.getMessage());
		}
	}
	
	@Test 
	public void testCastLangtagLiteral() {
		Literal langLit = f.createLiteral("foo", "en");
		try {
			Literal result = stringCast.evaluate(f, langLit);
			fail("casting of language-tagged literal to xsd:string should result in type error");
		}
		catch (ValueExprEvaluationException e) {
			// do nothing, expected
		}
	}
	
	@Test 
	public void testCastIntegerLiteral() {
		Literal intLit = f.createLiteral(10);
		try {
			Literal result = stringCast.evaluate(f, intLit);
			assertNotNull(result);
			assertEquals(XMLSchema.STRING, result.getDatatype());
			assertNull(result.getLanguage());
			assertEquals("10", result.getLabel());
		}
		catch (ValueExprEvaluationException e) {
			fail(e.getMessage());
		}
	}
	
	@Test 
	public void testCastDateTimeLiteral() {
		String lexVal = "2000-01-01T00:00:00";
		Literal dtLit = f.createLiteral(XMLDatatypeUtil.parseCalendar(lexVal));
		try {
			Literal result = stringCast.evaluate(f, dtLit);
			assertNotNull(result);
			assertEquals(XMLSchema.STRING, result.getDatatype());
			assertNull(result.getLanguage());
			assertEquals(lexVal, result.getLabel());
		}
		catch (ValueExprEvaluationException e) {
			fail(e.getMessage());
		}
	}
	
	@Test 
	public void testCastUnknownDatatypedLiteral() {
		String lexVal = "foobar";
		Literal dtLit = f.createLiteral(lexVal, f.createURI("foo:unknownDt"));
		try {
			Literal result = stringCast.evaluate(f, dtLit);
			assertNotNull(result);
			assertEquals(XMLSchema.STRING, result.getDatatype());
			assertNull(result.getLanguage());
			assertEquals(lexVal, result.getLabel());
		}
		catch (ValueExprEvaluationException e) {
			fail(e.getMessage());
		}
	}
}
