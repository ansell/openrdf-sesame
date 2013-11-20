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
package org.openrdf.repository.config;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * @author Dale Visser
 */
public class TestConfigTemplate {

	@Test
	public final void testNonEscapeOfAlternateMultilineDelimiter() {
		String value = "I contain a '''multiline\nstring''' that shouldn't be escaped.";
		assertEquals(ConfigTemplate.escapeMultilineQuotes("\"\"\"", value), value);
		value = "I contain a \"\"\"multiline\nstring\"\"\" that shouldn't be escaped.";
		assertEquals(ConfigTemplate.escapeMultilineQuotes("'''", value), value);
	}

	@Test
	public final void testEscapeOfSpecifiedMultilineDelimiter() {
		String value = "I contain a '''multiline\nstring''' that should be escaped.";
		assertEquals(ConfigTemplate.escapeMultilineQuotes("'''", value),
				"I contain a \\'\\'\\'multiline\nstring\\'\\'\\' that should be escaped.");
		value = "I contain a \"\"\"multiline\nstring\"\"\" that should be escaped.";
		assertEquals(ConfigTemplate.escapeMultilineQuotes("\"\"\"", value),
				"I contain a \\\"\\\"\\\"multiline\nstring\\\"\\\"\\\" that should be escaped.");
	}

	@Test
	public final void testNonEscapeOfShorterSequences() {
		String value = "' '' ''' ''''";
		assertEquals(ConfigTemplate.escapeMultilineQuotes("'''", value), "' '' \\'\\'\\' \\'\\'\\''");
		value = "\" \"\" \"\"\" \"\"\"\"";
		assertEquals(ConfigTemplate.escapeMultilineQuotes("\"\"\"", value),
				"\" \"\" \\\"\\\"\\\" \\\"\\\"\\\"\"");
	}

	@Test(expected = IllegalArgumentException.class)
	public final void testInvalidDelimiterThrowsException() {
		ConfigTemplate.escapeMultilineQuotes("'", "any value");
	}
}
