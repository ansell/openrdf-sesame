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
package org.openrdf.rio.datatypes;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.util.LiteralUtilException;
import org.openrdf.rio.DatatypeHandler;

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
	 *         {@link DatatypeHandler#isRecognizedDatatype(URI)} and not throw an
	 *         exception if used with a valid value when calling
	 *         {@link DatatypeHandler#verifyDatatype(String, URI)} and
	 *         {@link DatatypeHandler#normalizeDatatype(String, URI, ValueFactory)}
	 *         .
	 */
	protected abstract URI getRecognisedDatatypeUri();

	/**
	 * @return A URI that must return false from
	 *         {@link DatatypeHandler#isRecognizedDatatype(URI)} and throw an
	 *         exception if used with
	 *         {@link DatatypeHandler#verifyDatatype(String, URI)} and
	 *         {@link DatatypeHandler#normalizeDatatype(String, URI, ValueFactory)}
	 *         .
	 */
	protected abstract URI getUnrecognisedDatatypeUri();

	/**
	 * @return A string value that does match the recognised datatype URI, and
	 *         will succeed when used with both
	 *         {@link DatatypeHandler#verifyDatatype(String, URI)} and
	 *         {@link DatatypeHandler#normalizeDatatype(String, URI, ValueFactory)}
	 *         .
	 */
	protected abstract String getValueMatchingRecognisedDatatypeUri();

	/**
	 * @return A string value that does not match the recognised datatype URI,
	 *         and will fail when used with both
	 *         {@link DatatypeHandler#verifyDatatype(String, URI)} and
	 *         {@link DatatypeHandler#normalizeDatatype(String, URI, ValueFactory)}
	 *         .
	 */
	protected abstract String getValueNotMatchingRecognisedDatatypeUri();

	/**
	 * @return An instance of {@link Literal} that is equal to the expected
	 *         output from a successful call to
	 *         {@link DatatypeHandler#normalizeDatatype(String, URI, org.openrdf.model.ValueFactory)}
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
	 * {@link org.openrdf.rio.DatatypeHandler#isRecognizedDatatype(org.openrdf.model.URI)}
	 * .
	 */
	@Test
	public final void testIsRecognizedDatatypeNull()
		throws Exception
	{
		thrown.expect(NullPointerException.class);
		testHandler.isRecognizedDatatype(null);
	}

	/**
	 * Test method for
	 * {@link org.openrdf.rio.DatatypeHandler#isRecognizedDatatype(org.openrdf.model.URI)}
	 * .
	 */
	@Test
	public final void testIsRecognizedDatatypeTrue()
		throws Exception
	{
		assertTrue(testHandler.isRecognizedDatatype(getRecognisedDatatypeUri()));
	}

	/**
	 * Test method for
	 * {@link org.openrdf.rio.DatatypeHandler#isRecognizedDatatype(org.openrdf.model.URI)}
	 * .
	 */
	@Test
	public final void testIsRecognizedDatatypeFalse()
		throws Exception
	{
		assertFalse(testHandler.isRecognizedDatatype(getUnrecognisedDatatypeUri()));
	}

	/**
	 * Test method for
	 * {@link org.openrdf.rio.DatatypeHandler#verifyDatatype(java.lang.String, org.openrdf.model.URI)}
	 * .
	 */
	@Test
	public final void testVerifyDatatypeNullDatatypeUri()
		throws Exception
	{
		thrown.expect(NullPointerException.class);
		testHandler.verifyDatatype(getValueMatchingRecognisedDatatypeUri(), null);
	}

	/**
	 * Test method for
	 * {@link org.openrdf.rio.DatatypeHandler#verifyDatatype(java.lang.String, org.openrdf.model.URI)}
	 * .
	 */
	@Test
	public final void testVerifyDatatypeNullValueRecognised()
		throws Exception
	{
		thrown.expect(NullPointerException.class);
		testHandler.verifyDatatype(null, getRecognisedDatatypeUri());
	}

	/**
	 * Test method for
	 * {@link org.openrdf.rio.DatatypeHandler#verifyDatatype(java.lang.String, org.openrdf.model.URI)}
	 * .
	 */
	@Test
	public final void testVerifyDatatypeNullValueUnrecognised()
		throws Exception
	{
		thrown.expect(LiteralUtilException.class);
		testHandler.verifyDatatype(null, getUnrecognisedDatatypeUri());
	}

	/**
	 * Test method for
	 * {@link org.openrdf.rio.DatatypeHandler#verifyDatatype(java.lang.String, org.openrdf.model.URI)}
	 * .
	 */
	@Test
	public final void testVerifyDatatypeUnrecognisedDatatypeUri()
		throws Exception
	{
		thrown.expect(LiteralUtilException.class);
		testHandler.verifyDatatype(getValueMatchingRecognisedDatatypeUri(), getUnrecognisedDatatypeUri());
	}

	/**
	 * Test method for
	 * {@link org.openrdf.rio.DatatypeHandler#verifyDatatype(java.lang.String, org.openrdf.model.URI)}
	 * .
	 */
	@Test
	public final void testVerifyDatatypeInvalidValue()
		throws Exception
	{
		assertFalse(testHandler.verifyDatatype(getValueNotMatchingRecognisedDatatypeUri(),
				getRecognisedDatatypeUri()));
	}

	/**
	 * Test method for
	 * {@link org.openrdf.rio.DatatypeHandler#verifyDatatype(java.lang.String, org.openrdf.model.URI)}
	 * .
	 */
	@Test
	public final void testVerifyDatatypeValidValue()
		throws Exception
	{
		assertTrue(testHandler.verifyDatatype(getValueMatchingRecognisedDatatypeUri(),
				getRecognisedDatatypeUri()));
	}

	/**
	 * Test method for
	 * {@link org.openrdf.rio.DatatypeHandler#normalizeDatatype(java.lang.String, org.openrdf.model.URI, org.openrdf.model.ValueFactory)}
	 * .
	 */
	@Test
	public final void testNormalizeDatatypeNullDatatypeUri()
		throws Exception
	{
		thrown.expect(NullPointerException.class);
		testHandler.normalizeDatatype(getValueMatchingRecognisedDatatypeUri(), null, vf);
	}

	/**
	 * Test method for
	 * {@link org.openrdf.rio.DatatypeHandler#normalizeDatatype(java.lang.String, org.openrdf.model.URI, org.openrdf.model.ValueFactory)}
	 * .
	 */
	@Test
	public final void testNormalizeDatatypeNullValue()
		throws Exception
	{
		thrown.expect(NullPointerException.class);
		testHandler.normalizeDatatype(null, getRecognisedDatatypeUri(), vf);
	}

	/**
	 * Test method for
	 * {@link org.openrdf.rio.DatatypeHandler#normalizeDatatype(java.lang.String, org.openrdf.model.URI, org.openrdf.model.ValueFactory)}
	 * .
	 */
	@Test
	public final void testNormalizeDatatypeUnrecognisedDatatypeUri()
		throws Exception
	{
		thrown.expect(LiteralUtilException.class);
		testHandler.normalizeDatatype(getValueMatchingRecognisedDatatypeUri(), getUnrecognisedDatatypeUri(), vf);
	}

	/**
	 * Test method for
	 * {@link org.openrdf.rio.DatatypeHandler#normalizeDatatype(java.lang.String, org.openrdf.model.URI, org.openrdf.model.ValueFactory)}
	 * .
	 */
	@Test
	public final void testNormalizeDatatypeInvalidValue()
		throws Exception
	{
		thrown.expect(LiteralUtilException.class);
		testHandler.normalizeDatatype(getValueNotMatchingRecognisedDatatypeUri(), getRecognisedDatatypeUri(),
				vf);
	}

	/**
	 * Test method for
	 * {@link org.openrdf.rio.DatatypeHandler#normalizeDatatype(java.lang.String, org.openrdf.model.URI, org.openrdf.model.ValueFactory)}
	 * .
	 */
	@Test
	public final void testNormalizeDatatypeValidValue()
		throws Exception
	{
		Literal result = testHandler.normalizeDatatype(getValueMatchingRecognisedDatatypeUri(),
				getRecognisedDatatypeUri(), vf);
		Literal expectedResult = getNormalisedLiteralForRecognisedDatatypeAndValue();

		assertNotNull(expectedResult.getDatatype());
		assertNotNull(expectedResult.getLabel());
		assertNull(expectedResult.getLanguage());

		assertEquals(expectedResult.getDatatype(), result.getDatatype());
		assertEquals(expectedResult.getLabel(), result.getLabel());
		assertEquals(expectedResult.getLanguage(), result.getLanguage());
	}

	/**
	 * Test method for {@link org.openrdf.rio.DatatypeHandler#getKey()}.
	 */
	@Test
	public final void testGetKey()
		throws Exception
	{
		String result = testHandler.getKey();
		String expectedResult = getExpectedKey();

		assertEquals(expectedResult, result);
	}

}
