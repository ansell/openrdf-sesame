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
package org.openrdf.query.algebra.evaluation.function;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.openrdf.model.Literal;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.datatypes.XMLDatatypeUtil;
import org.openrdf.model.impl.SimpleValueFactory;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;

/**
 * @author jeen
 */
public class TestStringCast {

	private StringCast stringCast;

	private ValueFactory f = SimpleValueFactory.getInstance();
	
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
			assertFalse(result.getLanguage().isPresent());
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
			assertFalse(result.getLanguage().isPresent());
			assertEquals(lexVal, result.getLabel());
		}
		catch (ValueExprEvaluationException e) {
			fail(e.getMessage());
		}
	}
	
	@Test 
	public void testCastUnknownDatatypedLiteral() {
		String lexVal = "foobar";
		Literal dtLit = f.createLiteral(lexVal, f.createIRI("foo:unknownDt"));
		try {
			Literal result = stringCast.evaluate(f, dtLit);
			assertNotNull(result);
			assertEquals(XMLSchema.STRING, result.getDatatype());
			assertFalse(result.getLanguage().isPresent());
			assertEquals(lexVal, result.getLabel());
		}
		catch (ValueExprEvaluationException e) {
			fail(e.getMessage());
		}
	}
}
