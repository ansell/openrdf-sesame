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
package org.eclipse.rdf4j.sail.lucene;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.URI;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.ValueFactoryImpl;
import org.eclipse.rdf4j.model.vocabulary.GEO;

public final class SearchFields {

	/**
	 * The name of the Document field holding the document identifier. This
	 * consists of the Resource identifier (URI or BNodeID) and the Context ID
	 * (the format is "resourceId|contextId")
	 */
	public static final String ID_FIELD_NAME = "id";
	/**
	 * The name of the Document field holding the Resource identifier. The value
	 * stored in this field is either a URI or a BNode ID.
	 */
	public static final String URI_FIELD_NAME = "uri";
	/**
	 * The name of the Document field that holds multiple text values of a
	 * Resource. The field is called "text", as it contains all text, but was
	 * called "ALL" during the discussion. For each statement-literal of the
	 * resource, the object literal is stored in a field using the
	 * predicate-literal and additionally in a TEXT_FIELD_NAME-literal field. The
	 * reasons are given in the documentation of
	 * {@link #addPropertyFields(String, String, Document)}
	 */
	public static final String TEXT_FIELD_NAME = "text";
	/**
	 * The name of the Document field holding the context identifer(s).
	 */
	public static final String CONTEXT_FIELD_NAME = "context";
	/**
	 * the null context
	 */
	public static final String CONTEXT_NULL = "null";
	/**
	 * String used to prefix BNode IDs with so that we can distinguish BNode
	 * fields from URI fields in Documents. The prefix is chosen so that it is
	 * invalid as a (part of a) URI scheme.
	 */
	public static final String BNODE_ID_PREFIX = "!";

	public static final String HIGHLIGHTER_PRE_TAG = "<B>";
	public static final String HIGHLIGHTER_POST_TAG = "</B>";
	public static final Pattern HIGHLIGHTER_PATTERN = Pattern.compile("("+HIGHLIGHTER_PRE_TAG+".+?"+HIGHLIGHTER_POST_TAG+")");

	private static final ValueFactory valueFactory = ValueFactoryImpl.getInstance();

	private SearchFields() {}

	/**
	 * Returns the String ID corresponding with the specified Resource. The id
	 * string is either the URI or a bnode prefixed with a "!".
	 */
	public static String getResourceID(Resource resource) {
		if (resource instanceof URI) {
			return resource.toString();
		}
		else if (resource instanceof BNode) {
			return BNODE_ID_PREFIX + ((BNode)resource).getID();
		}
		else {
			throw new IllegalArgumentException("Unknown Resource type: " + resource);
		}
	}

	/**
	 * Get the ID for a context. Context can be null, then the "null" string is
	 * returned
	 * 
	 * @param resource
	 *        the context
	 * @return a string
	 */
	public static String getContextID(Resource resource) {
		if (resource == null)
			return CONTEXT_NULL;
		else
			return getResourceID(resource);
	}

	/**
	 * Parses an id-string (a serialized resource) back to a resource Inverse
	 * method of {@link getResourceID}
	 * 
	 * @param idString
	 */
	public static Resource createResource(String idString) {
		if (idString.startsWith(BNODE_ID_PREFIX)) {
			return valueFactory.createBNode(idString.substring(BNODE_ID_PREFIX.length()));
		}
		else {
			return valueFactory.createIRI(idString);
		}
	}

	public static Resource createContext(String idString) {
		if(CONTEXT_NULL.equals(idString)) {
			return null;
		}
		else {
			return createResource(idString);
		}
	}

	public static String getLiteralPropertyValueAsString(Statement statement) {
		Value object = statement.getObject();
		if(object instanceof Literal) {
			return ((Literal) object).getLabel();
		}
		else {
			return null;
		}
	}

	public static String getPropertyField(URI property) {
		return property.toString();
	}

	/**
	 * Determines whether the specified field name is a property field name.
	 */
	public static boolean isPropertyField(String fieldName) {
		return !ID_FIELD_NAME.equals(fieldName) && !URI_FIELD_NAME.equals(fieldName)
				&& !TEXT_FIELD_NAME.equals(fieldName) && !CONTEXT_FIELD_NAME.equals(fieldName)
				// geo/internal fields
				&& fieldName.charAt(0) != '_';
	}

	public static String formIdString(String resourceId, String contextId) {
		StringBuilder idBuilder = new StringBuilder(resourceId);
		idBuilder.append("|");
		idBuilder.append(contextId);
		return idBuilder.toString();
	}

	/**
	 * Returns a score value encoded as a Literal.
	 * 
	 * @param score
	 *        the float score to convert
	 * @return the score as a literal
	 */
	public static Literal scoreToLiteral(float score) {
		return valueFactory.createLiteral(score);
	}

	public static Literal wktToLiteral(String wkt) {
		return valueFactory.createLiteral(wkt, GEO.WKT_LITERAL);
	}

	public static Literal distanceToLiteral(double d) {
		return valueFactory.createLiteral(d);
	}

	public static String getSnippet(String highlightedValue)
	{
		if(highlightedValue.length() > 100) {
			StringBuilder buf = new StringBuilder();
			String separator = "";
			Matcher matcher = HIGHLIGHTER_PATTERN.matcher(highlightedValue);
			for(int i=0; i<2 && matcher.find(); i++) {
				buf.append(separator);
				buf.append(matcher.group());
				separator = "...";
			}
			highlightedValue = buf.toString();
		}
		return highlightedValue;
	}
}