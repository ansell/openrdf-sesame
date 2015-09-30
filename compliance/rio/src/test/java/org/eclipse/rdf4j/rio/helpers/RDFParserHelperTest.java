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
package org.eclipse.rdf4j.rio.helpers;

import static org.junit.Assert.*;

import java.util.Collections;
import java.util.HashSet;

import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.eclipse.rdf4j.rio.DatatypeHandler;
import org.eclipse.rdf4j.rio.LanguageHandler;
import org.eclipse.rdf4j.rio.ParseErrorListener;
import org.eclipse.rdf4j.rio.ParserConfig;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.rio.RioSetting;
import org.eclipse.rdf4j.rio.helpers.BasicParserSettings;
import org.eclipse.rdf4j.rio.helpers.ParseErrorCollector;
import org.eclipse.rdf4j.rio.helpers.RDFParserHelper;
import org.eclipse.rdf4j.rio.helpers.org;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

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
		valueFactory = SimpleValueFactory.getInstance();
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
	 * {@link org.eclipse.rdf4j.rio.helpers.RDFParserHelper#createLiteral(java.lang.String, java.lang.String, org.eclipse.rdf4j.model.URI, org.eclipse.rdf4j.rio.ParserConfig, org.eclipse.rdf4j.rio.ParseErrorListener, org.eclipse.rdf4j.model.ValueFactory)}
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
	 * {@link org.eclipse.rdf4j.rio.helpers.RDFParserHelper#createLiteral(java.lang.String, java.lang.String, org.eclipse.rdf4j.model.URI, org.eclipse.rdf4j.rio.ParserConfig, org.eclipse.rdf4j.rio.ParseErrorListener, org.eclipse.rdf4j.model.ValueFactory)}
	 * .
	 */
	@Test
	public final void testCreateLiteralLabelOnly()
		throws Exception
	{
		Literal literal = RDFParserHelper.createLiteral(LABEL_TESTA, null, null, parserConfig, errListener,
				valueFactory);

		assertEquals(LABEL_TESTA, literal.getLabel());
		assertFalse(literal.getLanguage().isPresent());
		assertEquals(XMLSchema.STRING, literal.getDatatype());
	}

	/**
	 * Test method for
	 * {@link org.eclipse.rdf4j.rio.helpers.RDFParserHelper#createLiteral(java.lang.String, java.lang.String, org.eclipse.rdf4j.model.URI, org.eclipse.rdf4j.rio.ParserConfig, org.eclipse.rdf4j.rio.ParseErrorListener, org.eclipse.rdf4j.model.ValueFactory)}
	 * .
	 */
	@Test
	public final void testCreateLiteralLabelAndLanguage()
		throws Exception
	{
		Literal literal = RDFParserHelper.createLiteral(LABEL_TESTA, LANG_EN, null, parserConfig, errListener,
				valueFactory);

		assertEquals(LABEL_TESTA, literal.getLabel());
		assertEquals(LANG_EN, literal.getLanguage().orElse(null));
		assertEquals(RDF.LANGSTRING, literal.getDatatype());
	}

	/**
	 * Test method for
	 * {@link org.eclipse.rdf4j.rio.helpers.RDFParserHelper#createLiteral(java.lang.String, java.lang.String, org.eclipse.rdf4j.model.URI, org.eclipse.rdf4j.rio.ParserConfig, org.eclipse.rdf4j.rio.ParseErrorListener, org.eclipse.rdf4j.model.ValueFactory)}
	 * .
	 */
	@Test
	public final void testCreateLiteralLabelAndDatatype()
		throws Exception
	{
		Literal literal = RDFParserHelper.createLiteral(LABEL_TESTA, null, XMLSchema.STRING, parserConfig,
				errListener, valueFactory);

		assertEquals(LABEL_TESTA, literal.getLabel());
		assertFalse(literal.getLanguage().isPresent());
		assertEquals(XMLSchema.STRING, literal.getDatatype());
	}

	/**
	 * Test method for
	 * {@link org.eclipse.rdf4j.rio.helpers.RDFParserHelper#createLiteral(java.lang.String, java.lang.String, org.eclipse.rdf4j.model.URI, org.eclipse.rdf4j.rio.ParserConfig, org.eclipse.rdf4j.rio.ParseErrorListener, org.eclipse.rdf4j.model.ValueFactory)}
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
		assertEquals(LANG_EN, literal.getLanguage().orElse(null));
		assertEquals(RDF.LANGSTRING, literal.getDatatype());
	}

	/**
	 * Test method for
	 * {@link org.eclipse.rdf4j.rio.helpers.RDFParserHelper#createLiteral(java.lang.String, java.lang.String, org.eclipse.rdf4j.model.URI, org.eclipse.rdf4j.rio.ParserConfig, org.eclipse.rdf4j.rio.ParseErrorListener, org.eclipse.rdf4j.model.ValueFactory)}
	 * .
	 * <p>
	 * SES-1803 : Temporary decision to ensure RDF-1.0 backwards compatibility
	 * for Literals created by this method in cases where {@link RDF#LANGSTRING}
	 * is given and there is NO given language.
	 * <p>
	 * SES-2203 : This was inconsistent, so has been changed to verify failure.
	 */
	@Test
	public final void testCreateLiteralLabelNoLanguageWithRDFLangString()
		throws Exception
	{
		thrown.expect(RDFParseException.class);
		RDFParserHelper.createLiteral(LABEL_TESTA, null, RDF.LANGSTRING, parserConfig,
				errListener, valueFactory);
	}

	@Test
	public final void testReportErrorStringFatalActive()
		throws Exception
	{
		parserConfig.set(BasicParserSettings.VERIFY_DATATYPE_VALUES, true);
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
		parserConfig.set(BasicParserSettings.VERIFY_DATATYPE_VALUES, true);
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
		parserConfig.set(BasicParserSettings.VERIFY_DATATYPE_VALUES, true);
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
		parserConfig.set(BasicParserSettings.VERIFY_DATATYPE_VALUES, true);
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
	 * {@link org.eclipse.rdf4j.rio.helpers.RDFParserHelper#reportError(java.lang.Exception, int, int, org.eclipse.rdf4j.rio.RioSetting, org.eclipse.rdf4j.rio.ParserConfig, org.eclipse.rdf4j.rio.ParseErrorListener)}
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
	 * {@link org.eclipse.rdf4j.rio.helpers.RDFParserHelper#reportFatalError(java.lang.String, org.eclipse.rdf4j.rio.ParseErrorListener)}
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
	 * {@link org.eclipse.rdf4j.rio.helpers.RDFParserHelper#reportFatalError(java.lang.String, int, int, org.eclipse.rdf4j.rio.ParseErrorListener)}
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
	 * {@link org.eclipse.rdf4j.rio.helpers.RDFParserHelper#reportFatalError(java.lang.Exception, org.eclipse.rdf4j.rio.ParseErrorListener)}
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
	 * {@link org.eclipse.rdf4j.rio.helpers.RDFParserHelper#reportFatalError(java.lang.Exception, int, int, org.eclipse.rdf4j.rio.ParseErrorListener)}
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
