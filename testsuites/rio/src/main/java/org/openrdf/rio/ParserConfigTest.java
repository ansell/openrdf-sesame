/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2013.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.rio;

import static org.junit.Assert.*;

import org.junit.Test;

import org.openrdf.rio.RDFParser.DatatypeHandling;
import org.openrdf.rio.helpers.BasicParserSettings;

/**
 * Test for ParserConfig to verify that the core operations succeed and are
 * consistent.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 */
public class ParserConfigTest {

	/**
	 * Test the default constructor does not set any settings, but still returns
	 * the default values for basic settings.
	 */
	@Test
	public final void testParserConfig() {
		ParserConfig testConfig = new ParserConfig();

		// check that the basic settings are not set
		assertFalse(testConfig.isSet(BasicParserSettings.DATATYPE_HANDLING));
		assertFalse(testConfig.isSet(BasicParserSettings.PRESERVE_BNODE_IDS));
		assertFalse(testConfig.isSet(BasicParserSettings.STOP_AT_FIRST_ERROR));
		assertFalse(testConfig.isSet(BasicParserSettings.VERIFY_DATA));

		// check that the basic settings all return their expected default values
		assertEquals(DatatypeHandling.VERIFY, testConfig.get(BasicParserSettings.DATATYPE_HANDLING));
		assertEquals(DatatypeHandling.VERIFY, testConfig.datatypeHandling());
		assertFalse(testConfig.get(BasicParserSettings.PRESERVE_BNODE_IDS));
		assertFalse(testConfig.isPreserveBNodeIDs());
		assertTrue(testConfig.get(BasicParserSettings.STOP_AT_FIRST_ERROR));
		assertTrue(testConfig.stopAtFirstError());
		assertTrue(testConfig.get(BasicParserSettings.VERIFY_DATA));
		assertTrue(testConfig.verifyData());

		// then set to check that changes occur
		testConfig.set(BasicParserSettings.DATATYPE_HANDLING, DatatypeHandling.NORMALIZE);
		testConfig.set(BasicParserSettings.PRESERVE_BNODE_IDS, true);
		testConfig.set(BasicParserSettings.STOP_AT_FIRST_ERROR, false);
		testConfig.set(BasicParserSettings.VERIFY_DATA, false);

		// check that the basic settings are now explicitly set
		assertTrue(testConfig.isSet(BasicParserSettings.DATATYPE_HANDLING));
		assertTrue(testConfig.isSet(BasicParserSettings.PRESERVE_BNODE_IDS));
		assertTrue(testConfig.isSet(BasicParserSettings.STOP_AT_FIRST_ERROR));
		assertTrue(testConfig.isSet(BasicParserSettings.VERIFY_DATA));

		// check that the basic settings all return their set values
		assertEquals(DatatypeHandling.NORMALIZE, testConfig.get(BasicParserSettings.DATATYPE_HANDLING));
		assertEquals(DatatypeHandling.NORMALIZE, testConfig.datatypeHandling());
		assertTrue(testConfig.get(BasicParserSettings.PRESERVE_BNODE_IDS));
		assertTrue(testConfig.isPreserveBNodeIDs());
		assertFalse(testConfig.get(BasicParserSettings.STOP_AT_FIRST_ERROR));
		assertFalse(testConfig.stopAtFirstError());
		assertFalse(testConfig.get(BasicParserSettings.VERIFY_DATA));
		assertFalse(testConfig.verifyData());

		// reset the values
		testConfig.set(BasicParserSettings.DATATYPE_HANDLING, null);
		testConfig.set(BasicParserSettings.PRESERVE_BNODE_IDS, null);
		testConfig.set(BasicParserSettings.STOP_AT_FIRST_ERROR, null);
		testConfig.set(BasicParserSettings.VERIFY_DATA, null);

		// check again that the basic settings all return their expected default
		// values
		assertEquals(DatatypeHandling.VERIFY, testConfig.get(BasicParserSettings.DATATYPE_HANDLING));
		assertEquals(DatatypeHandling.VERIFY, testConfig.datatypeHandling());
		assertFalse(testConfig.get(BasicParserSettings.PRESERVE_BNODE_IDS));
		assertFalse(testConfig.isPreserveBNodeIDs());
		assertTrue(testConfig.get(BasicParserSettings.STOP_AT_FIRST_ERROR));
		assertTrue(testConfig.stopAtFirstError());
		assertTrue(testConfig.get(BasicParserSettings.VERIFY_DATA));
		assertTrue(testConfig.verifyData());
	}

	/**
	 * Test that the explicit constructor sets all of the basic settings using
	 * the default values.
	 */
	@Test
	public final void testParserConfigSameAsDefaults() {
		ParserConfig testConfig = new ParserConfig(true, true, false, DatatypeHandling.VERIFY);

		// check that the basic settings are explicitly set
		assertTrue(testConfig.isSet(BasicParserSettings.DATATYPE_HANDLING));
		assertTrue(testConfig.isSet(BasicParserSettings.PRESERVE_BNODE_IDS));
		assertTrue(testConfig.isSet(BasicParserSettings.STOP_AT_FIRST_ERROR));
		assertTrue(testConfig.isSet(BasicParserSettings.VERIFY_DATA));

		// check that the basic settings all return their expected default values
		assertEquals(DatatypeHandling.VERIFY, testConfig.get(BasicParserSettings.DATATYPE_HANDLING));
		assertEquals(DatatypeHandling.VERIFY, testConfig.datatypeHandling());
		assertFalse(testConfig.get(BasicParserSettings.PRESERVE_BNODE_IDS));
		assertFalse(testConfig.isPreserveBNodeIDs());
		assertTrue(testConfig.get(BasicParserSettings.STOP_AT_FIRST_ERROR));
		assertTrue(testConfig.stopAtFirstError());
		assertTrue(testConfig.get(BasicParserSettings.VERIFY_DATA));
		assertTrue(testConfig.verifyData());
	}

	/**
	 * Test that the explicit constructor sets all of the basic settings using
	 * non-default values.
	 */
	@Test
	public final void testParserConfigNonDefaults() {
		ParserConfig testConfig = new ParserConfig(false, false, true, DatatypeHandling.IGNORE);

		// check that the basic settings are explicitly set
		assertTrue(testConfig.isSet(BasicParserSettings.DATATYPE_HANDLING));
		assertTrue(testConfig.isSet(BasicParserSettings.PRESERVE_BNODE_IDS));
		assertTrue(testConfig.isSet(BasicParserSettings.STOP_AT_FIRST_ERROR));
		assertTrue(testConfig.isSet(BasicParserSettings.VERIFY_DATA));

		// check that the basic settings all return their set values
		assertEquals(DatatypeHandling.IGNORE, testConfig.get(BasicParserSettings.DATATYPE_HANDLING));
		assertEquals(DatatypeHandling.IGNORE, testConfig.datatypeHandling());
		assertTrue(testConfig.get(BasicParserSettings.PRESERVE_BNODE_IDS));
		assertTrue(testConfig.isPreserveBNodeIDs());
		assertFalse(testConfig.get(BasicParserSettings.STOP_AT_FIRST_ERROR));
		assertFalse(testConfig.stopAtFirstError());
		assertFalse(testConfig.get(BasicParserSettings.VERIFY_DATA));
		assertFalse(testConfig.verifyData());
	}

}
