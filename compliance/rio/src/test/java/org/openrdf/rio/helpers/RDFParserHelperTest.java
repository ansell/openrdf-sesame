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
package org.openrdf.rio.helpers;

import static org.junit.Assert.*;

import java.util.Collections;
import java.util.HashSet;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import org.openrdf.model.Literal;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.rio.DatatypeHandler;
import org.openrdf.rio.LanguageHandler;
import org.openrdf.rio.ParseErrorListener;
import org.openrdf.rio.ParserConfig;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RioSetting;

/**
 * Tests for {@link RDFParserHelper} methods.
 * 
 * @author Peter Ansell
 */
public class RDFParserHelperTest {

	private static final String TEST_MESSAGE_FOR_FAILURE = "Test message for failure.";

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	private static final String LABEL_TESTA = "test-a";

	private static final String LANG_EN = "en";

	private ParserConfig parserConfig;

	private ParseErrorCollector errListener;

	private ValueFactory valueFactory;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp()
		throws Exception
	{
		parserConfig = new ParserConfig();
		// By default we wipe out the SPI loaded datatype and language handlers
		parserConfig.set(BasicParserSettings.DATATYPE_HANDLERS, Collections.<DatatypeHandler> emptyList());
		parserConfig.set(BasicParserSettings.LANGUAGE_HANDLERS, Collections.<LanguageHandler> emptyList());
		// Ensure that the set of non-fatal errors is empty by default
		parserConfig.setNonFatalErrors(new HashSet<RioSetting<?>>());
		errListener = new ParseErrorCollector();
		valueFactory = new ValueFactoryImpl();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown()
		throws Exception
	{
	}

	/**
	 * Test method for
	 * {@link org.openrdf.rio.helpers.RDFParserHelper#createLiteral(java.lang.String, java.lang.String, org.openrdf.model.URI, org.openrdf.rio.ParserConfig, org.openrdf.rio.ParseErrorListener, org.openrdf.model.ValueFactory)}
	 * .
	 */
	@Test
	public final void testCreateLiteralLabelNull()
		throws Exception
	{
		thrown.expect(NullPointerException.class);
		thrown.expectMessage("Cannot create a literal using a null label");
		RDFParserHelper.createLiteral(null, null, null, parserConfig, errListener, valueFactory);
	}

	/**
	 * Test method for
	 * {@link org.openrdf.rio.helpers.RDFParserHelper#createLiteral(java.lang.String, java.lang.String, org.openrdf.model.URI, org.openrdf.rio.ParserConfig, org.openrdf.rio.ParseErrorListener, org.openrdf.model.ValueFactory)}
	 * .
	 */
	@Test
	public final void testCreateLiteralLabelOnly()
		throws Exception
	{
		Literal literal = RDFParserHelper.createLiteral(LABEL_TESTA, null, null, parserConfig, errListener,
				valueFactory);

		assertEquals(LABEL_TESTA, literal.getLabel());
		assertNull(literal.getLanguage());
		assertNull(literal.getDatatype());
	}

	/**
	 * Test method for
	 * {@link org.openrdf.rio.helpers.RDFParserHelper#createLiteral(java.lang.String, java.lang.String, org.openrdf.model.URI, org.openrdf.rio.ParserConfig, org.openrdf.rio.ParseErrorListener, org.openrdf.model.ValueFactory)}
	 * .
	 */
	@Test
	public final void testCreateLiteralLabelAndLanguage()
		throws Exception
	{
		Literal literal = RDFParserHelper.createLiteral(LABEL_TESTA, LANG_EN, null, parserConfig, errListener,
				valueFactory);

		assertEquals(LABEL_TESTA, literal.getLabel());
		assertEquals(LANG_EN, literal.getLanguage());
		assertNull(literal.getDatatype());
	}

	/**
	 * Test method for
	 * {@link org.openrdf.rio.helpers.RDFParserHelper#createLiteral(java.lang.String, java.lang.String, org.openrdf.model.URI, org.openrdf.rio.ParserConfig, org.openrdf.rio.ParseErrorListener, org.openrdf.model.ValueFactory)}
	 * .
	 */
	@Test
	public final void testCreateLiteralLabelAndDatatype()
		throws Exception
	{
		Literal literal = RDFParserHelper.createLiteral(LABEL_TESTA, null, XMLSchema.STRING, parserConfig,
				errListener, valueFactory);

		assertEquals(LABEL_TESTA, literal.getLabel());
		assertNull(literal.getLanguage());
		assertEquals(XMLSchema.STRING, literal.getDatatype());
	}

	/**
	 * Test method for
	 * {@link org.openrdf.rio.helpers.RDFParserHelper#createLiteral(java.lang.String, java.lang.String, org.openrdf.model.URI, org.openrdf.rio.ParserConfig, org.openrdf.rio.ParseErrorListener, org.openrdf.model.ValueFactory)}
	 * .
	 * <p>
	 * SES-1803 : Temporary decision to ensure RDF-1.0 backwards compatibility
	 * for Literals created by this method in cases where {@link RDF#LANGSTRING}
	 * is given and there is a language.
	 */
	@Test
	public final void testCreateLiteralLabelAndLanguageWithRDFLangString()
		throws Exception
	{
		Literal literal = RDFParserHelper.createLiteral(LABEL_TESTA, LANG_EN, RDF.LANGSTRING, parserConfig,
				errListener, valueFactory);

		assertEquals(LABEL_TESTA, literal.getLabel());
		assertEquals(LANG_EN, literal.getLanguage());
		assertNull(literal.getDatatype());
	}

	/**
	 * Test method for
	 * {@link org.openrdf.rio.helpers.RDFParserHelper#createLiteral(java.lang.String, java.lang.String, org.openrdf.model.URI, org.openrdf.rio.ParserConfig, org.openrdf.rio.ParseErrorListener, org.openrdf.model.ValueFactory)}
	 * .
	 * <p>
	 * SES-1803 : Temporary decision to ensure RDF-1.0 backwards compatibility
	 * for Literals created by this method in cases where {@link RDF#LANGSTRING}
	 * is given and there is NO given language.
	 */
	@Test
	public final void testCreateLiteralLabelNoLanguageWithRDFLangString()
		throws Exception
	{
		Literal literal = RDFParserHelper.createLiteral(LABEL_TESTA, null, RDF.LANGSTRING, parserConfig,
				errListener, valueFactory);

		assertEquals(LABEL_TESTA, literal.getLabel());
		assertNull(literal.getLanguage());
		assertEquals(RDF.LANGSTRING, literal.getDatatype());
	}

	@Test
	public final void testReportErrorStringFatalActive()
		throws Exception
	{
		assertTrue(parserConfig.get(BasicParserSettings.VERIFY_DATATYPE_VALUES));
		thrown.expect(RDFParseException.class);
		thrown.expectMessage(TEST_MESSAGE_FOR_FAILURE);
		try {
			RDFParserHelper.reportError(TEST_MESSAGE_FOR_FAILURE, BasicParserSettings.VERIFY_DATATYPE_VALUES,
					parserConfig, errListener);
		}
		finally {
			assertErrorListener(0, 1, 0);
		}
	}

	@Test
	public final void testReportErrorStringNonFatalActive()
		throws Exception
	{
		assertTrue(parserConfig.get(BasicParserSettings.VERIFY_DATATYPE_VALUES));
		parserConfig.addNonFatalError(BasicParserSettings.VERIFY_DATATYPE_VALUES);
		RDFParserHelper.reportError(TEST_MESSAGE_FOR_FAILURE, BasicParserSettings.VERIFY_DATATYPE_VALUES,
				parserConfig, errListener);
		assertErrorListener(0, 1, 0);
	}

	@Test
	public final void testReportErrorStringFatalInactive()
		throws Exception
	{
		assertFalse(parserConfig.get(BasicParserSettings.FAIL_ON_UNKNOWN_DATATYPES));
		RDFParserHelper.reportError(TEST_MESSAGE_FOR_FAILURE, BasicParserSettings.FAIL_ON_UNKNOWN_DATATYPES,
				parserConfig, errListener);
		assertErrorListener(0, 0, 0);
	}

	@Test
	public final void testReportErrorStringNonFatalInactive()
		throws Exception
	{
		assertFalse(parserConfig.get(BasicParserSettings.FAIL_ON_UNKNOWN_DATATYPES));
		parserConfig.addNonFatalError(BasicParserSettings.FAIL_ON_UNKNOWN_DATATYPES);
		RDFParserHelper.reportError(TEST_MESSAGE_FOR_FAILURE, BasicParserSettings.FAIL_ON_UNKNOWN_DATATYPES,
				parserConfig, errListener);
		assertErrorListener(0, 0, 0);
	}

	@Test
	public final void testReportErrorStringIntIntFatalActive()
		throws Exception
	{
		assertTrue(parserConfig.get(BasicParserSettings.VERIFY_DATATYPE_VALUES));
		thrown.expect(RDFParseException.class);
		thrown.expectMessage(TEST_MESSAGE_FOR_FAILURE);
		try {
			RDFParserHelper.reportError(TEST_MESSAGE_FOR_FAILURE, 1, 1,
					BasicParserSettings.VERIFY_DATATYPE_VALUES, parserConfig, errListener);
		}
		finally {
			assertErrorListener(0, 1, 0);
		}
	}

	@Test
	public final void testReportErrorStringIntIntNonFatalActive()
		throws Exception
	{
		assertTrue(parserConfig.get(BasicParserSettings.VERIFY_DATATYPE_VALUES));
		parserConfig.addNonFatalError(BasicParserSettings.VERIFY_DATATYPE_VALUES);
		RDFParserHelper.reportError(TEST_MESSAGE_FOR_FAILURE, 1, 1, BasicParserSettings.VERIFY_DATATYPE_VALUES,
				parserConfig, errListener);
		assertErrorListener(0, 1, 0);
	}

	@Test
	public final void testReportErrorStringIntIntFatalInactive()
		throws Exception
	{
		assertFalse(parserConfig.get(BasicParserSettings.FAIL_ON_UNKNOWN_DATATYPES));
		RDFParserHelper.reportError(TEST_MESSAGE_FOR_FAILURE, 1, 1,
				BasicParserSettings.FAIL_ON_UNKNOWN_DATATYPES, parserConfig, errListener);
		assertErrorListener(0, 0, 0);
	}

	@Test
	public final void testReportErrorStringIntIntNonFatalInactive()
		throws Exception
	{
		assertFalse(parserConfig.get(BasicParserSettings.FAIL_ON_UNKNOWN_DATATYPES));
		parserConfig.addNonFatalError(BasicParserSettings.FAIL_ON_UNKNOWN_DATATYPES);
		RDFParserHelper.reportError(TEST_MESSAGE_FOR_FAILURE, 1, 1,
				BasicParserSettings.FAIL_ON_UNKNOWN_DATATYPES, parserConfig, errListener);
		assertErrorListener(0, 0, 0);
	}

	/**
	 * Test method for
	 * {@link org.openrdf.rio.helpers.RDFParserHelper#reportError(java.lang.Exception, int, int, org.openrdf.rio.RioSetting, org.openrdf.rio.ParserConfig, org.openrdf.rio.ParseErrorListener)}
	 * .
	 */
	@Ignore
	@Test
	public final void testReportErrorExceptionIntInt()
		throws Exception
	{
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link org.openrdf.rio.helpers.RDFParserHelper#reportFatalError(java.lang.String, org.openrdf.rio.ParseErrorListener)}
	 * .
	 */
	@Ignore
	@Test
	public final void testReportFatalErrorString()
		throws Exception
	{
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link org.openrdf.rio.helpers.RDFParserHelper#reportFatalError(java.lang.String, int, int, org.openrdf.rio.ParseErrorListener)}
	 * .
	 */
	@Ignore
	@Test
	public final void testReportFatalErrorStringIntInt()
		throws Exception
	{
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link org.openrdf.rio.helpers.RDFParserHelper#reportFatalError(java.lang.Exception, org.openrdf.rio.ParseErrorListener)}
	 * .
	 */
	@Ignore
	@Test
	public final void testReportFatalErrorException()
		throws Exception
	{
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link org.openrdf.rio.helpers.RDFParserHelper#reportFatalError(java.lang.Exception, int, int, org.openrdf.rio.ParseErrorListener)}
	 * .
	 */
	@Ignore
	@Test
	public final void testReportFatalErrorExceptionIntInt()
		throws Exception
	{
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Private method for verifying the number of errors that were logged to the
	 * {@link ParseErrorListener}.
	 * 
	 * @param fatalErrors
	 *        Expected number of fatal errors logged by error listener.
	 * @param errors
	 *        Expected number of errors logged by error listener.
	 * @param warnings
	 *        Expected number of warnings logged by error listener.
	 */
	private void assertErrorListener(int fatalErrors, int errors, int warnings) {
		assertEquals(fatalErrors, errListener.getFatalErrors().size());
		assertEquals(errors, errListener.getErrors().size());
		assertEquals(warnings, errListener.getWarnings().size());
	}
}
