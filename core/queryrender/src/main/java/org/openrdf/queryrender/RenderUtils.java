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
package org.openrdf.queryrender;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.rio.ntriples.NTriplesUtil;

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

		try {
			NTriplesUtil.append(theValue, aBuffer);
		}
		catch (IOException e) {
			throw new RuntimeException("Failed to serialize value", e);
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
