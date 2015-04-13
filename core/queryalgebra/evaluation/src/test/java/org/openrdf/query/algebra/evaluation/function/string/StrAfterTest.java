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
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.RDF;
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

		URI leftArg = f.createURI("http://example.org/foobar");
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
