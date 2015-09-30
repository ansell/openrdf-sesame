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
package org.openrdf.workbench.util;

import java.util.regex.Pattern;

/**
 * Helper class for substituting in variables to query templates for the purpose
 * of saving and retrieving user queries to a repository local to the workbench.
 * 
 * @author Dale Visser
 */
public class QueryStringBuilder {

	private final StringBuilder builder;

	private static final Pattern VAR_PATTERN = Pattern.compile("\\$<\\w+>");

	/**
	 * Creates a new builder from the given template.
	 * 
	 * @param template
	 */
	public QueryStringBuilder(final String template) {
		if (null == template || template.isEmpty()) {
			throw new IllegalArgumentException("Template is null or length is zero.");
		}
		if (!VAR_PATTERN.matcher(template).find()) {
			throw new IllegalArgumentException("Template did not contain variables.");
		}
		this.builder = new StringBuilder(template);
	}

	/**
	 * Returns the internal string being constructed.
	 */
	@Override
	public String toString() {
		return this.builder.toString();
	}

	/**
	 * Replace the repository variable with the current repository URL.
	 * 
	 * @param paramText
	 *        the $<...> formatted parameter name
	 * @param uri
	 *        any object who's toString() returns a valid URI
	 */
	protected void replaceURI(final String paramText, final Object uri) {
		replace(paramText, QueryStringBuilder.uriQuote(uri.toString()));
	}

	/**
	 * Replace instances of the old text with a copy of the new text.
	 * 
	 * @param paramText
	 *        parameter in the form "$<paramName>"
	 * @param newText
	 *        the new text
	 */
	protected void replace(final String paramText, final String newText) {
		int loc = builder.indexOf(paramText);
		while (loc >= 0) {
			builder.replace(loc, loc + paramText.length(), newText);
			loc = builder.indexOf(paramText);
		}
	}

	protected void replaceQuote(final String paramText, final String newText) {
		this.replace(paramText, quote(newText));
	}

	/**
	 * Place double quotes around the given string.
	 * 
	 * @param value
	 *        the string to add quotes to
	 * @return a copy of the given strings quoted with double quotes
	 */
	private static String quote(final String value) {
		return quote(value, "\"", "\"");
	}

	/**
	 * Place double quotes around the given string and append an XSD data type.
	 * 
	 * @param value
	 *        the value to quote
	 * @param type
	 *        the XSD data type name
	 * @return a copy of the given string quoted with XSD data type appended
	 */
	protected static String xsdQuote(final String value, final String type) {
		return quote(value, "\"", "\"^^xsd:" + type);
	}

	/**
	 * Place angle brackets around a URI or URL.
	 * 
	 * @param uri
	 *        an object whose toString() returns a URI or URL
	 * @return a string quoting the given URI with angle brackets
	 */
	private static String uriQuote(final Object uri) {
		return quote(uri.toString(), "<", ">");
	}

	protected static String quote(final String value, final String left, final String right) {
		return left + value + right;
	}
}