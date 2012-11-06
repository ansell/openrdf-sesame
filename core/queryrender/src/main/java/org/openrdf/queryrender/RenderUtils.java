/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2012.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.queryrender;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

/**
 * @author Michael Grove
 * @since 2.7.0
 */
public final class RenderUtils {

	/**
	 * No instances
	 */
	private RenderUtils() {
	}

	/**
	 * Return the query string rendering of the {@link org.openrdf.model.Value}
	 * 
	 * @param theValue
	 *        the value to render
	 * @return the value rendered in its query string representation
	 */
	public static String getSPARQLQueryString(Value theValue) {
		StringBuilder aBuffer = new StringBuilder();

		if (theValue instanceof URI) {
			URI aURI = (URI)theValue;
			aBuffer.append("<").append(aURI.toString()).append(">");
		}
		else if (theValue instanceof BNode) {
			aBuffer.append("_:").append(((BNode)theValue).getID());
		}
		else if (theValue instanceof Literal) {
			Literal aLit = (Literal)theValue;

			aBuffer.append("\"\"\"").append(escape(aLit.getLabel())).append("\"\"\"").append(
					aLit.getLanguage() != null ? "@" + aLit.getLanguage() : "");

			if (aLit.getDatatype() != null) {
				aBuffer.append("^^<").append(aLit.getDatatype().toString()).append(">");
			}
		}

		return aBuffer.toString();
	}

	/**
	 * Return the query string rendering of the {@link Value}
	 * 
	 * @param theValue
	 *        the value to render
	 * @return the value rendered in its query string representation
	 */
	public static String getSerqlQueryString(Value theValue) {
		StringBuilder aBuffer = new StringBuilder();

		if (theValue instanceof URI) {
			URI aURI = (URI)theValue;
			aBuffer.append("<").append(aURI.toString()).append(">");
		}
		else if (theValue instanceof BNode) {
			aBuffer.append("_:").append(((BNode)theValue).getID());
		}
		else if (theValue instanceof Literal) {
			Literal aLit = (Literal)theValue;

			aBuffer.append("\"").append(escape(aLit.getLabel())).append("\"").append(
					aLit.getLanguage() != null ? "@" + aLit.getLanguage() : "");

			if (aLit.getDatatype() != null) {
				aBuffer.append("^^<").append(aLit.getDatatype().toString()).append(">");
			}
		}

		return aBuffer.toString();
	}

	/**
	 * Properly escape out any special characters in the query string. Replaces
	 * unescaped double quotes with \" and replaces slashes '\' which are not a
	 * valid escape sequence such as \t or \n with a double slash '\\' so they
	 * are unescaped correctly by a SPARQL parser.
	 * 
	 * @param theString
	 *        the query string to escape chars in
	 * @return the escaped query string
	 */
	public static String escape(String theString) {
		theString = theString.replaceAll("\"", "\\\\\"");

		StringBuffer aBuffer = new StringBuffer();
		Matcher aMatcher = Pattern.compile("\\\\([^tnrbf\"'\\\\])").matcher(theString);
		while (aMatcher.find()) {
			aMatcher.appendReplacement(aBuffer, String.format("\\\\\\\\%s", aMatcher.group(1)));
		}
		aMatcher.appendTail(aBuffer);

		return aBuffer.toString();
	}
}
