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
package org.openrdf.rio;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import org.openrdf.rio.RDFParser.DatatypeHandling;
import org.openrdf.rio.helpers.BasicParserSettings;

/**
 * Test for ParserConfig to verify that the core operations succeed and are
 * consistent.
 * 
 * @author Peter Ansell
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
		assertFalse(testConfig.isSet(BasicParserSettings.PRESERVE_BNODE_IDS));

		// check that the basic settings all return their expected default values
		assertFalse(testConfig.get(BasicParserSettings.PRESERVE_BNODE_IDS));
		assertFalse(testConfig.isPreserveBNodeIDs());

		// then set to check that changes occur
		testConfig.set(BasicParserSettings.PRESERVE_BNODE_IDS, true);

		// check that the basic settings are now explicitly set
		assertTrue(testConfig.isSet(BasicParserSettings.PRESERVE_BNODE_IDS));

		// check that the basic settings all return their set values
		assertTrue(testConfig.get(BasicParserSettings.PRESERVE_BNODE_IDS));
		assertTrue(testConfig.isPreserveBNodeIDs());

		// reset the values
		testConfig.set(BasicParserSettings.PRESERVE_BNODE_IDS, null);

		// check again that the basic settings all return their expected default
		// values
		assertFalse(testConfig.get(BasicParserSettings.PRESERVE_BNODE_IDS));
		assertFalse(testConfig.isPreserveBNodeIDs());
	}

	/**
	 * Test that the explicit constructor sets all of the basic settings using
	 * the default values.
	 */
	@Test
	public final void testParserConfigSameAsDefaults() {
		ParserConfig testConfig = new ParserConfig(true, true, false, DatatypeHandling.VERIFY);

		// check that the basic settings are explicitly set
		assertTrue(testConfig.isSet(BasicParserSettings.PRESERVE_BNODE_IDS));

		// check that the basic settings all return their expected default values
		assertFalse(testConfig.get(BasicParserSettings.PRESERVE_BNODE_IDS));
		assertFalse(testConfig.isPreserveBNodeIDs());
	}

	/**
	 * Test that the explicit constructor sets all of the basic settings using
	 * non-default values.
	 */
	@Test
	public final void testParserConfigNonDefaults() {
		ParserConfig testConfig = new ParserConfig(false, false, true, DatatypeHandling.IGNORE);

		// check that the basic settings are explicitly set
		assertTrue(testConfig.isSet(BasicParserSettings.PRESERVE_BNODE_IDS));

		// check that the basic settings all return their set values
		assertTrue(testConfig.get(BasicParserSettings.PRESERVE_BNODE_IDS));
		assertTrue(testConfig.isPreserveBNodeIDs());
	}

}
