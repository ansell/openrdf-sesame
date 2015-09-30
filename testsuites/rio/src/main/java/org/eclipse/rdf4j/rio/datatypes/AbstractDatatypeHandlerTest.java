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
package org.eclipse.rdf4j.rio.datatypes;

import static org.junit.Assert.*;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.util.LiteralUtilException;
import org.eclipse.rdf4j.rio.DatatypeHandler;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Abstract test for DatatypeHandler interface.
 * 
 * @author Peter Ansell
 */
public abstract class AbstractDatatypeHandlerTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	/**
	 * Generates a new instance of the {@link DatatypeHandler} implementation in
	 * question and returns it.
	 * 
	 * @return A new instance of the {@link DatatypeHandler} implementation being
	 *         tested.
	 */
	protected abstract DatatypeHandler getNewDatatypeHandler();

	/**
	 * @return A URI that must return true from
	 *         {@link DatatypeHandler#isRecognizedDatatype(IRI)} and not throw an
	 *         exception if used with a valid value when calling
	 *         {@link DatatypeHandler#verifyDatatype(String, IRI)} and
	 *         {@link DatatypeHandler#normalizeDatatype(String, IRI, ValueFactory)}
	 *         .
	 */
	protected abstract IRI getRecognisedDatatypeUri();

	/**
	 * @return A URI that must return false from
	 *         {@link DatatypeHandler#isRecognizedDatatype(IRI)} and throw an
	 *         exception if used with
	 *         {@link DatatypeHandler#verifyDatatype(String, IRI)} and
	 *         {@link DatatypeHandler#normalizeDatatype(String, IRI, ValueFactory)}
	 *         .
	 */
	protected abstract IRI getUnrecognisedDatatypeUri();

	/**
	 * @return A string value that does match the recognised datatype URI, and
	 *         will succeed when used with both
	 *         {@link DatatypeHandler#verifyDatatype(String, IRI)} and
	 *         {@link DatatypeHandler#normalizeDatatype(String, IRI, ValueFactory)}
	 *         .
	 */
	protected abstract String getValueMatchingRecognisedDatatypeUri();

	/**
	 * @return A string value that does not match the recognised datatype URI,
	 *         and will fail when used with both
	 *         {@link DatatypeHandler#verifyDatatype(String, IRI)} and
	 *         {@link DatatypeHandler#normalizeDatatype(String, IRI, ValueFactory)}
	 *         .
	 */
	protected abstract String getValueNotMatchingRecognisedDatatypeUri();

	/**
	 * @return An instance of {@link Literal} that is equal to the expected
	 *         output from a successful call to
	 *         {@link DatatypeHandler#normalizeDatatype(String, IRI, org.eclipse.rdf4j.model.ValueFactory)}
	 *         ;
	 */
	protected abstract Literal getNormalisedLiteralForRecognisedDatatypeAndValue();

	/**
	 * @return An instance of {@link ValueFactory} that can be used to produce a
	 *         normalised literal.
	 */
	protected abstract ValueFactory getValueFactory();

	/**
	 * @return The key that is expected to be returned for
	 *         {@link DatatypeHandler#getKey()} to identify the service.
	 */
	protected abstract String getExpectedKey();

	private DatatypeHandler testHandler;

	private ValueFactory vf;

	@Before
	public void setUp()
		throws Exception
	{
		testHandler = getNewDatatypeHandler();
		vf = getValueFactory();
	}

	@After
	public void tearDown()
		throws Exception
	{
		testHandler = null;
		vf = null;
	}

	/**
	 * Test method for
	 * {@link org.eclipse.rdf4j.rio.DatatypeHandler#isRecognizedDatatype(org.eclipse.rdf4j.model.IRI)}
	 * .
	 */
	@Test
	public void testIsRecognizedDatatypeNull()
		throws Exception
	{
		thrown.expect(NullPointerException.class);
		testHandler.isRecognizedDatatype(null);
	}

	/**
	 * Test method for
	 * {@link org.eclipse.rdf4j.rio.DatatypeHandler#isRecognizedDatatype(org.eclipse.rdf4j.model.IRI)}
	 * .
	 */
	@Test
	public void testIsRecognizedDatatypeTrue()
		throws Exception
	{
		assertTrue(testHandler.isRecognizedDatatype(getRecognisedDatatypeUri()));
	}

	/**
	 * Test method for
	 * {@link org.eclipse.rdf4j.rio.DatatypeHandler#isRecognizedDatatype(org.eclipse.rdf4j.model.IRI)}
	 * .
	 */
	@Test
	public void testIsRecognizedDatatypeFalse()
		throws Exception
	{
		assertFalse(testHandler.isRecognizedDatatype(getUnrecognisedDatatypeUri()));
	}

	/**
	 * Test method for
	 * {@link org.eclipse.rdf4j.rio.DatatypeHandler#verifyDatatype(java.lang.String, org.eclipse.rdf4j.model.IRI)}
	 * .
	 */
	@Test
	public void testVerifyDatatypeNullDatatypeUri()
		throws Exception
	{
		thrown.expect(NullPointerException.class);
		testHandler.verifyDatatype(getValueMatchingRecognisedDatatypeUri(), null);
	}

	/**
	 * Test method for
	 * {@link org.eclipse.rdf4j.rio.DatatypeHandler#verifyDatatype(java.lang.String, org.eclipse.rdf4j.model.IRI)}
	 * .
	 */
	@Test
	public void testVerifyDatatypeNullValueRecognised()
		throws Exception
	{
		thrown.expect(NullPointerException.class);
		testHandler.verifyDatatype(null, getRecognisedDatatypeUri());
	}

	/**
	 * Test method for
	 * {@link org.eclipse.rdf4j.rio.DatatypeHandler#verifyDatatype(java.lang.String, org.eclipse.rdf4j.model.IRI)}
	 * .
	 */
	@Test
	public void testVerifyDatatypeNullValueUnrecognised()
		throws Exception
	{
		thrown.expect(LiteralUtilException.class);
		testHandler.verifyDatatype(null, getUnrecognisedDatatypeUri());
	}

	/**
	 * Test method for
	 * {@link org.eclipse.rdf4j.rio.DatatypeHandler#verifyDatatype(java.lang.String, org.eclipse.rdf4j.model.IRI)}
	 * .
	 */
	@Test
	public void testVerifyDatatypeUnrecognisedDatatypeUri()
		throws Exception
	{
		thrown.expect(LiteralUtilException.class);
		testHandler.verifyDatatype(getValueMatchingRecognisedDatatypeUri(), getUnrecognisedDatatypeUri());
	}

	/**
	 * Test method for
	 * {@link org.eclipse.rdf4j.rio.DatatypeHandler#verifyDatatype(java.lang.String, org.eclipse.rdf4j.model.IRI)}
	 * .
	 */
	@Test
	public void testVerifyDatatypeInvalidValue()
		throws Exception
	{
		assertFalse(testHandler.verifyDatatype(getValueNotMatchingRecognisedDatatypeUri(),
				getRecognisedDatatypeUri()));
	}

	/**
	 * Test method for
	 * {@link org.eclipse.rdf4j.rio.DatatypeHandler#verifyDatatype(java.lang.String, org.eclipse.rdf4j.model.IRI)}
	 * .
	 */
	@Test
	public void testVerifyDatatypeValidValue()
		throws Exception
	{
		assertTrue(testHandler.verifyDatatype(getValueMatchingRecognisedDatatypeUri(),
				getRecognisedDatatypeUri()));
	}

	/**
	 * Test method for
	 * {@link org.eclipse.rdf4j.rio.DatatypeHandler#normalizeDatatype(java.lang.String, org.eclipse.rdf4j.model.IRI, org.eclipse.rdf4j.model.ValueFactory)}
	 * .
	 */
	@Test
	public void testNormalizeDatatypeNullDatatypeUri()
		throws Exception
	{
		thrown.expect(NullPointerException.class);
		testHandler.normalizeDatatype(getValueMatchingRecognisedDatatypeUri(), null, vf);
	}

	/**
	 * Test method for
	 * {@link org.eclipse.rdf4j.rio.DatatypeHandler#normalizeDatatype(java.lang.String, org.eclipse.rdf4j.model.IRI, org.eclipse.rdf4j.model.ValueFactory)}
	 * .
	 */
	@Test
	public void testNormalizeDatatypeNullValue()
		throws Exception
	{
		thrown.expect(NullPointerException.class);
		testHandler.normalizeDatatype(null, getRecognisedDatatypeUri(), vf);
	}

	/**
	 * Test method for
	 * {@link org.eclipse.rdf4j.rio.DatatypeHandler#normalizeDatatype(java.lang.String, org.eclipse.rdf4j.model.IRI, org.eclipse.rdf4j.model.ValueFactory)}
	 * .
	 */
	@Test
	public void testNormalizeDatatypeUnrecognisedDatatypeUri()
		throws Exception
	{
		thrown.expect(LiteralUtilException.class);
		testHandler.normalizeDatatype(getValueMatchingRecognisedDatatypeUri(), getUnrecognisedDatatypeUri(), vf);
	}

	/**
	 * Test method for
	 * {@link org.eclipse.rdf4j.rio.DatatypeHandler#normalizeDatatype(java.lang.String, org.eclipse.rdf4j.model.IRI, org.eclipse.rdf4j.model.ValueFactory)}
	 * .
	 */
	@Test
	public void testNormalizeDatatypeInvalidValue()
		throws Exception
	{
		thrown.expect(LiteralUtilException.class);
		testHandler.normalizeDatatype(getValueNotMatchingRecognisedDatatypeUri(), getRecognisedDatatypeUri(),
				vf);
	}

	/**
	 * Test method for
	 * {@link org.eclipse.rdf4j.rio.DatatypeHandler#normalizeDatatype(java.lang.String, org.eclipse.rdf4j.model.IRI, org.eclipse.rdf4j.model.ValueFactory)}
	 * .
	 */
	@Test
	public void testNormalizeDatatypeValidValue()
		throws Exception
	{
		Literal result = testHandler.normalizeDatatype(getValueMatchingRecognisedDatatypeUri(),
				getRecognisedDatatypeUri(), vf);
		Literal expectedResult = getNormalisedLiteralForRecognisedDatatypeAndValue();

		assertNotNull(expectedResult.getDatatype());
		assertNotNull(expectedResult.getLabel());
		assertFalse(expectedResult.getLanguage().isPresent());

		assertEquals(expectedResult.getDatatype(), result.getDatatype());
		assertEquals(expectedResult.getLabel(), result.getLabel());
		assertEquals(expectedResult.getLanguage(), result.getLanguage());
	}

	/**
	 * Test method for {@link org.eclipse.rdf4j.rio.DatatypeHandler#getKey()}.
	 */
	@Test
	public void testGetKey()
		throws Exception
	{
		String result = testHandler.getKey();
		String expectedResult = getExpectedKey();

		assertEquals(expectedResult, result);
	}

}
