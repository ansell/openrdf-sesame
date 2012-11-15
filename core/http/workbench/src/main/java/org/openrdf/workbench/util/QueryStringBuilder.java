/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2012.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.workbench.util;

import java.net.URL;
import java.util.regex.Pattern;

import org.openrdf.query.QueryLanguage;

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
	 * Replace the user name variable with the current user.
	 * 
	 * @param userName
	 *        the current user
	 */
	protected void replaceUserName(final String userName) {
		replace("$<userName>", quote(userName));
	}

	/**
	 * Replace the repository variable with the current repository URL.
	 * 
	 * @param url
	 *        the current repository URL
	 */
	protected void replaceRepository(final String url) {
		replaceURIParameter("$<repository>", url);
	}
	
	protected void replaceQueryReference(final String urn) {
		replaceURIParameter("$<query>", urn);
	}
	
	private void replaceURIParameter(final String parameter, final String uri) {
		replace(parameter, QueryStringBuilder.uriQuote(uri));
	}
	
	protected void replaceQueryName(final String queryName) {
		replace("$<queryName>", QueryStringBuilder.quote(queryName));
	}

	/**
	 * Replace first instance of the old text with a copy of the new text.
	 * 
	 * @param oldText
	 *        the old text
	 * @param newText
	 *        the new text
	 */
	protected void replace(final String oldText, final String newText) {
		final int start = builder.indexOf(oldText);
		builder.replace(start, start + oldText.length(), newText);
	}

	/**
	 * Perform replacement on several common fields for update operations.
	 * 
	 * @param userName
	 *        the name of the current user
	 * @param shared
	 *        whether the saved query is to be shared with other users
	 * @param queryLanguage
	 *        the language of the saved query
	 * @param queryText
	 *        the actual text of the query to save
	 * @param rowsPerPage
	 *        the rows per page to display for results
	 */
	protected void replaceUpdateFields(final String userName, final boolean shared,
			final QueryLanguage queryLanguage, final String queryText, final int rowsPerPage)
	{
		replaceUserName(userName);
		replace("$<shared>", xsdQuote(String.valueOf(shared), "boolean"));
		replace("$<queryLanguage>", quote(queryLanguage.toString()));

		// Quoting the query with ''' assuming all string literals in the query
		// are of the STRING_LITERAL1, STRING_LITERAL2 or STRING_LITERAL_LONG2
		// types
		if (queryText.indexOf("'''") > 0) {
			throw new IllegalArgumentException("queryText may not contain '''-quoted strings.");
		}
		replace("$<queryText>", quote(queryText, "'''", "'''"));
		replace("$<rowsPerPage>", xsdQuote(String.valueOf(rowsPerPage), "unsignedByte"));
	}

	/**
	 * Place double quotes around the given string.
	 * 
	 * @param value
	 *        the string to add quotes to
	 * @return a copy of the given strings quoted with double quotes
	 */
	protected static String quote(final String value) {
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
	 * @param uri an object whose toString() returns a URI or URL
	 * @return a string quoting the given URI with angle brackets
	 */
	private static String uriQuote(final Object uri) {
		return quote(uri.toString(), "<", ">");
	}

	private static String quote(final String value, final String left, final String right) {
		return left + value + right;
	}
}
