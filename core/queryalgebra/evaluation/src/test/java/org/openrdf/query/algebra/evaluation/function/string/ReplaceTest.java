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
public class ReplaceTest {

	private Replace replaceFunc;

	private ValueFactory f = new ValueFactoryImpl();

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp()
		throws Exception
	{
		replaceFunc = new Replace();
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

		Literal arg = f.createLiteral("foobar");
		Literal pattern = f.createLiteral("ba");
		Literal replacement = f.createLiteral("Z");

		try {
			Literal result = replaceFunc.evaluate(f, arg, pattern, replacement);

			assertEquals("fooZr", result.getLabel());
		}
		catch (ValueExprEvaluationException e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testEvaluate2() {

		Literal arg = f.createLiteral("foobar");
		Literal pattern = f.createLiteral("BA");
		Literal replacement = f.createLiteral("Z");

		try {
			Literal result = replaceFunc.evaluate(f, arg, pattern, replacement);

			assertEquals("foobar", result.getLabel());
		}
		catch (ValueExprEvaluationException e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testEvaluate3() {

		Literal arg = f.createLiteral("foobar");
		Literal pattern = f.createLiteral("BA");
		Literal replacement = f.createLiteral("Z");
		Literal flags = f.createLiteral("i");

		try {
			Literal result = replaceFunc.evaluate(f, arg, pattern, replacement, flags);

			assertEquals("fooZr", result.getLabel());
		}
		catch (ValueExprEvaluationException e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testEvaluate4() {

		Literal arg = f.createLiteral(10);
		Literal pattern = f.createLiteral("BA");
		Literal replacement = f.createLiteral("Z");

		try {
			Literal result = replaceFunc.evaluate(f, arg, pattern, replacement);

			fail("error expected on incompatible operand");
		}
		catch (ValueExprEvaluationException e) {
			// do nothing, expected
		}
	}
	
	@Test
	public void testEvaluate5() {

		Literal arg = f.createLiteral("foobarfoobarbarfoo");
		Literal pattern = f.createLiteral("ba");
		Literal replacement = f.createLiteral("Z");

		try {
			Literal result = replaceFunc.evaluate(f, arg, pattern, replacement);
			assertEquals("fooZrfooZrZrfoo", result.getLabel());
		}
		catch (ValueExprEvaluationException e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testEvaluate6() {

		Literal arg = f.createLiteral("foobarfoobarbarfooba");
		Literal pattern = f.createLiteral("ba.");
		Literal replacement = f.createLiteral("Z");

		try {
			Literal result = replaceFunc.evaluate(f, arg, pattern, replacement);
			assertEquals("fooZfooZZfooba", result.getLabel());
		}
		catch (ValueExprEvaluationException e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testEvaluate7() {

		Literal arg = f.createLiteral("日本語", "ja");
		Literal pattern = f.createLiteral("[^a-zA-Z0-9]");
		Literal replacement = f.createLiteral("-");

		try {
			Literal result = replaceFunc.evaluate(f, arg, pattern, replacement);
			assertEquals("---", result.getLabel());
			assertEquals("ja", result.getLanguage());
		}
		catch (ValueExprEvaluationException e) {
			fail(e.getMessage());
		}
	}
}
