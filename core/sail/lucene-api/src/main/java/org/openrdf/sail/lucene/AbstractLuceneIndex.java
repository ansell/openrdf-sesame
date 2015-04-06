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
package org.openrdf.sail.lucene;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.BNodeImpl;
import org.openrdf.model.impl.URIImpl;

public abstract class AbstractLuceneIndex implements SearchIndex {
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

	private static final List<String> REJECTED_DATATYPES = new ArrayList<String>();

	static {
		REJECTED_DATATYPES.add("http://www.w3.org/2001/XMLSchema#float");
	}

	/**
	 * keep a lit of old monitors that are still iterating but not closed (open
	 * iterators), will be all closed on shutdown items are removed from list by
	 * ReaderMnitor.endReading() when closing
	 */
	protected final Collection<AbstractReaderMonitor> oldmonitors = new LinkedList<AbstractReaderMonitor>();

	protected abstract AbstractReaderMonitor getCurrentMonitor();

	@Override
	public void beginReading()
	{
		getCurrentMonitor().beginReading();
	}

	@Override
	public void endReading() throws IOException
	{
		getCurrentMonitor().endReading();
	}


	/**
	 * Returns whether the provided literal is accepted by the LuceneIndex to be
	 * indexed. It for instance does not make much since to index xsd:float.
	 * 
	 * @param literal
	 *        the literal to be accepted
	 * @return true if the given literal will be indexed by this LuceneIndex
	 */
	@Override
	public boolean accept(Literal literal) {
		// we reject null literals
		if (literal == null)
			return false;

		// we reject literals that are in the list of rejected data types
		if ((literal.getDatatype() != null)
				&& (REJECTED_DATATYPES.contains(literal.getDatatype().stringValue())))
			return false;

		return true;
	}

	/**
	 * Returns the String ID corresponding with the specified Resource. The id
	 * string is either the URI or a bnode prefixed with a "!".
	 */
	protected final String getResourceID(Resource resource) {
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
	protected final String getContextID(Resource resource) {
		if (resource == null)
			return CONTEXT_NULL;
		else
			return getResourceID(resource);
	}

	/**
	 * Parses an id-string (a serialized resource) back to a resource Inverse
	 * method of {@link #getResourceID(Resource)}
	 * 
	 * @param idString
	 */
	protected final Resource getResource(String idString) {
		if (idString.startsWith(BNODE_ID_PREFIX)) {
			return new BNodeImpl(idString.substring(BNODE_ID_PREFIX.length()));
		}
		else {
			return new URIImpl(idString);
		}
	}

	protected final String getLiteralPropertyValueAsString(Statement statement) {
		Value object = statement.getObject();
		if (!(object instanceof Literal)) {
			return null;
		}
		return ((Literal)object).getLabel();
	}

	/**
	 * Determines whether the specified field name is a property field name.
	 */
	protected final boolean isPropertyField(String fieldName) {
		return !ID_FIELD_NAME.equals(fieldName) && !URI_FIELD_NAME.equals(fieldName)
				&& !TEXT_FIELD_NAME.equals(fieldName) && !CONTEXT_FIELD_NAME.equals(fieldName);
	}

	protected final String formIdString(String resourceId, String contextId) {
		StringBuilder idBuilder = new StringBuilder(resourceId);
		idBuilder.append("|");
		idBuilder.append(contextId);
		return idBuilder.toString();
	}
}
