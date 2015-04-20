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
package org.openrdf.query.algebra.evaluation.function.string;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.openrdf.model.Literal;
import org.openrdf.model.IRI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.SimpleValueFactory;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;

/**
 * @author jeen
 */
public class ReplaceTest {

	private Replace replaceFunc;

	private ValueFactory f = new SimpleValueFactory();

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
			assertEquals("ja", result.getLanguage().orElse(null));
		}
		catch (ValueExprEvaluationException e) {
			fail(e.getMessage());
		}
	}
}
