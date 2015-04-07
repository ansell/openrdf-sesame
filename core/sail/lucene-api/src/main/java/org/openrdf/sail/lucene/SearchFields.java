package org.openrdf.sail.lucene;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;

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

	protected static final ValueFactory valueFactory = ValueFactoryImpl.getInstance();

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
			return valueFactory.createURI(idString);
		}
	}

	public static String getLiteralPropertyValueAsString(Statement statement) {
		Value object = statement.getObject();
		if (!(object instanceof Literal)) {
			return null;
		}
		return ((Literal)object).getLabel();
	}

	/**
	 * Determines whether the specified field name is a property field name.
	 */
	public static boolean isPropertyField(String fieldName) {
		return !ID_FIELD_NAME.equals(fieldName) && !URI_FIELD_NAME.equals(fieldName)
				&& !TEXT_FIELD_NAME.equals(fieldName) && !CONTEXT_FIELD_NAME.equals(fieldName);
	}

	public static String formIdString(String resourceId, String contextId) {
		StringBuilder idBuilder = new StringBuilder(resourceId);
		idBuilder.append("|");
		idBuilder.append(contextId);
		return idBuilder.toString();
	}
}
