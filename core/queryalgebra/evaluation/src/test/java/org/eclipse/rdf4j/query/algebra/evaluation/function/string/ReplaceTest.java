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
package org.eclipse.rdf4j.query.algebra.evaluation.function.string;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.eclipse.rdf4j.query.algebra.evaluation.ValueExprEvaluationException;
import org.eclipse.rdf4j.query.algebra.evaluation.function.string.Replace;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author jeen
 */
public class ReplaceTest {

	private Replace replaceFunc;

	private ValueFactory f = SimpleValueFactory.getInstance();

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
