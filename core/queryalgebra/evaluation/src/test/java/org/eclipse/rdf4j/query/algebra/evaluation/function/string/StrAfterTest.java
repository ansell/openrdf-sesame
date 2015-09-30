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
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.eclipse.rdf4j.query.algebra.evaluation.ValueExprEvaluationException;
import org.eclipse.rdf4j.query.algebra.evaluation.function.string.StrAfter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author jeen
 */
public class StrAfterTest {

	private StrAfter strAfterFunc;

	private ValueFactory f = SimpleValueFactory.getInstance();

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
			assertEquals("en", result.getLanguage().orElse(null));
			assertEquals(RDF.LANGSTRING, result.getDatatype());
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
			assertEquals(XMLSchema.STRING, result.getDatatype());

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
			assertEquals(
					"incompatible operands for STRAFTER: \"foobar\"^^<http://www.w3.org/2001/XMLSchema#string>, \"b\"^^<http://www.w3.org/2001/XMLSchema#date>",
					e.getMessage());
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
			assertEquals(
					"incompatible operands for STRAFTER: \"10\"^^<http://www.w3.org/2001/XMLSchema#int>, \"b\"^^<http://www.w3.org/2001/XMLSchema#string>",
					e.getMessage());
		}
	}

	@Test
	public void testEvaluate7() {

		IRI leftArg = f.createIRI("http://example.org/foobar");
		Literal rightArg = f.createLiteral("b");

		try {
			Literal result = strAfterFunc.evaluate(f, leftArg, rightArg);

			fail("operand of incompatible type, should have resulted in error");
		}
		catch (ValueExprEvaluationException e) {
			assertEquals(
					"incompatible operands for STRAFTER: http://example.org/foobar, \"b\"^^<http://www.w3.org/2001/XMLSchema#string>",
					e.getMessage());
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
			assertEquals(
					"incompatible operands for STRAFTER: \"foobar\"^^<http://www.w3.org/2001/XMLSchema#string>, \"b\"@nl",
					e.getMessage());
		}
	}

	@Test
	public void testEvaluate10() {
		Literal leftArg = f.createLiteral("foobar", "en");
		Literal rightArg = f.createLiteral("b", XMLSchema.STRING);

		try {
			Literal result = strAfterFunc.evaluate(f, leftArg, rightArg);

			assertEquals("ar", result.getLabel());
			assertEquals(RDF.LANGSTRING, result.getDatatype());
			assertEquals("en", result.getLanguage().orElse(null));

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
			assertEquals(RDF.LANGSTRING, result.getDatatype());
			assertEquals("nl", result.getLanguage().orElse(null));

		}
		catch (ValueExprEvaluationException e) {
			fail(e.getMessage());
		}
	}

}
