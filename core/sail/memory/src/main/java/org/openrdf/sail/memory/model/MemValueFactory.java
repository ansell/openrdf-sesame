/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.memory.model;

import java.math.BigInteger;
import java.util.Collections;
import java.util.Set;

import javax.xml.datatype.XMLGregorianCalendar;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.datatypes.XMLDatatypeUtil;
import org.openrdf.model.impl.BNodeImpl;
import org.openrdf.model.impl.ContextStatementImpl;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.impl.ValueFactoryBase;
import org.openrdf.model.util.URIUtil;
import org.openrdf.model.vocabulary.XMLSchema;

/**
 * A factory for MemValue objects that keeps track of created objects to prevent
 * the creation of duplicate objects, minimizing memory usage as a result.
 * 
 * @author Arjohn Kampman
 * @author David Huynh
 */
public class MemValueFactory extends ValueFactoryBase {

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * Registry containing the set of MemURI objects as used by a MemoryStore.
	 * This registry enables the reuse of objects, minimizing the number of
	 * objects in main memory.
	 */
	private final WeakObjectRegistry<MemURI> uriRegistry = new WeakObjectRegistry<MemURI>();

	/**
	 * Registry containing the set of MemBNode objects as used by a MemoryStore.
	 * This registry enables the reuse of objects, minimizing the number of
	 * objects in main memory.
	 */
	private final WeakObjectRegistry<MemBNode> bnodeRegistry = new WeakObjectRegistry<MemBNode>();

	/**
	 * Registry containing the set of MemLiteral objects as used by a
	 * MemoryStore. This registry enables the reuse of objects, minimizing the
	 * number of objects in main memory.
	 */
	private final WeakObjectRegistry<MemLiteral> literalRegistry = new WeakObjectRegistry<MemLiteral>();

	/**
	 * Registry containing the set of namespce strings as used by MemURI objects
	 * in a MemoryStore. This registry enables the reuse of objects, minimizing
	 * the number of objects in main memory.
	 */
	private final WeakObjectRegistry<String> namespaceRegistry = new WeakObjectRegistry<String>();

	/*---------*
	 * Methods *
	 *---------*/

	public void clear() {
		uriRegistry.clear();
		bnodeRegistry.clear();
		literalRegistry.clear();
		namespaceRegistry.clear();
	}

	/**
	 * Returns a previously created MemValue that is equal to the supplied value,
	 * or <tt>null</tt> if the supplied value is a new value or is equal to
	 * <tt>null</tt>.
	 * 
	 * @param value
	 *        The MemValue equivalent of the supplied value, or <tt>null</tt>.
	 * @return A previously created MemValue that is equal to <tt>value</tt>, or
	 *         <tt>null</tt> if no such value exists or if <tt>value</tt> is
	 *         equal to <tt>null</tt>.
	 */
	public MemValue getMemValue(Value value) {
		if (value instanceof Resource) {
			return getMemResource((Resource)value);
		}
		else if (value instanceof Literal) {
			return getMemLiteral((Literal)value);
		}
		else if (value == null) {
			return null;
		}
		else {
			throw new IllegalArgumentException("value is not a Resource or Literal: " + value);
		}
	}

	/**
	 * See getMemValue() for description.
	 */
	public MemResource getMemResource(Resource resource) {
		if (resource instanceof URI) {
			return getMemURI((URI)resource);
		}
		else if (resource instanceof BNode) {
			return getMemBNode((BNode)resource);
		}
		else if (resource == null) {
			return null;
		}
		else {
			throw new IllegalArgumentException("resource is not a URI or BNode: " + resource);
		}
	}

	/**
	 * See getMemValue() for description.
	 */
	public synchronized MemURI getMemURI(URI uri) {
		if (isOwnMemValue(uri)) {
			return (MemURI)uri;
		}
		else {
			return uriRegistry.get(uri);
		}
	}

	/**
	 * See getMemValue() for description.
	 */
	public synchronized MemBNode getMemBNode(BNode bnode) {
		if (isOwnMemValue(bnode)) {
			return (MemBNode)bnode;
		}
		else {
			return bnodeRegistry.get(bnode);
		}
	}

	/**
	 * See getMemValue() for description.
	 */
	public synchronized MemLiteral getMemLiteral(Literal literal) {
		if (isOwnMemValue(literal)) {
			return (MemLiteral)literal;
		}
		else {
			return literalRegistry.get(literal);
		}
	}

	/**
	 * Checks whether the supplied value is an instance of <tt>MemValue</tt> and
	 * whether it has been created by this MemValueFactory.
	 */
	private boolean isOwnMemValue(Value value) {
		return value instanceof MemValue && ((MemValue)value).getCreator() == this;
	}

	/**
	 * Gets all URIs that are managed by this value factory.
	 * <p>
	 * <b>Warning:</b> This method is not synchronized. To iterate over the
	 * returned set in a thread-safe way, this method should only be called while
	 * synchronizing on this object.
	 * 
	 * @return An unmodifiable Set of MemURI objects.
	 */
	public Set<MemURI> getMemURIs() {
		return Collections.unmodifiableSet(uriRegistry);
	}

	/**
	 * Gets all bnodes that are managed by this value factory.
	 * <p>
	 * <b>Warning:</b> This method is not synchronized. To iterate over the
	 * returned set in a thread-safe way, this method should only be called while
	 * synchronizing on this object.
	 * 
	 * @return An unmodifiable Set of MemBNode objects.
	 */
	public Set<MemBNode> getMemBNodes() {
		return Collections.unmodifiableSet(bnodeRegistry);
	}

	/**
	 * Gets all literals that are managed by this value factory.
	 * <p>
	 * <b>Warning:</b> This method is not synchronized. To iterate over the
	 * returned set in a thread-safe way, this method should only be called while
	 * synchronizing on this object.
	 * 
	 * @return An unmodifiable Set of MemURI objects.
	 */
	public Set<MemLiteral> getMemLiterals() {
		return Collections.unmodifiableSet(literalRegistry);
	}

	/**
	 * Creates a MemValue for the supplied Value. The supplied value should not
	 * already have an associated MemValue. The created MemValue is returned.
	 * 
	 * @param value
	 *        A Resource or Literal.
	 * @return The created MemValue.
	 */
	public MemValue createMemValue(Value value) {
		if (value instanceof Resource) {
			return createMemResource((Resource)value);
		}
		else if (value instanceof Literal) {
			return createMemLiteral((Literal)value);
		}
		else {
			throw new IllegalArgumentException("value is not a Resource or Literal: " + value);
		}
	}

	/**
	 * See createMemValue() for description.
	 */
	public MemResource createMemResource(Resource resource) {
		if (resource instanceof URI) {
			return createMemURI((URI)resource);
		}
		else if (resource instanceof BNode) {
			return createMemBNode((BNode)resource);
		}
		else {
			throw new IllegalArgumentException("resource is not a URI or BNode: " + resource);
		}
	}

	/**
	 * See createMemValue() for description.
	 */
	public synchronized MemURI createMemURI(URI uri) {
		// Namespace strings are relatively large objects and are shared
		// between uris
		String namespace = uri.getNamespace();
		String sharedNamespace = namespaceRegistry.get(namespace);

		if (sharedNamespace == null) {
			// New namespace, add it to the registry
			namespaceRegistry.add(namespace);
		}
		else {
			// Use the shared namespace
			namespace = sharedNamespace;
		}

		// Create a MemURI and add it to the registry
		MemURI memURI;
		if (isOwnMemValue(uri) && namespace == uri.getNamespace()) {
			// Supplied parameter is a MemURI that can be reused
			memURI = (MemURI)uri;
		}
		else {
			memURI = new MemURI(this, namespace, uri.getLocalName());
		}

		boolean wasNew = uriRegistry.add(memURI);
		assert wasNew : "Created a duplicate MemURI for URI " + uri;

		return memURI;
	}

	/**
	 * See createMemValue() for description.
	 */
	public synchronized MemBNode createMemBNode(BNode bnode) {
		MemBNode memBNode = new MemBNode(this, bnode.getID());

		boolean wasNew = bnodeRegistry.add(memBNode);
		assert wasNew : "Created a duplicate MemBNode for bnode " + bnode;

		return memBNode;
	}

	/**
	 * See createMemValue() for description.
	 */
	public synchronized MemLiteral createMemLiteral(Literal literal) {
		MemLiteral memLiteral = null;

		String label = literal.getLabel();
		URI datatype = literal.getDatatype();
		if (datatype != null) {
			try {
				if (XMLDatatypeUtil.isIntegerDatatype(datatype)) {
					memLiteral = new IntegerMemLiteral(this, label, literal.integerValue(), datatype);
				}
				else if (datatype.equals(XMLSchema.DECIMAL)) {
					memLiteral = new DecimalMemLiteral(this, label, literal.decimalValue(), datatype);
				}
				else if (datatype.equals(XMLSchema.FLOAT)) {
					memLiteral = new NumericMemLiteral(this, label, literal.floatValue(), datatype);
				}
				else if (datatype.equals(XMLSchema.DOUBLE)) {
					memLiteral = new NumericMemLiteral(this, label, literal.doubleValue(), datatype);
				}
				else if (datatype.equals(XMLSchema.BOOLEAN)) {
					memLiteral = new BooleanMemLiteral(this, label, literal.booleanValue());
				}
				else if (datatype.equals(XMLSchema.DATETIME)) {
					memLiteral = new CalendarMemLiteral(this, label, literal.calendarValue());
				}
				else {
					memLiteral = new MemLiteral(this, literal.getLabel(), datatype);
				}
			}
			catch (IllegalArgumentException e) {
				// Unable to parse literal label to primitive type
				memLiteral = new MemLiteral(this, literal.getLabel(), datatype);
			}
		}
		else if (literal.getLanguage() != null) {
			memLiteral = new MemLiteral(this, literal.getLabel(), literal.getLanguage());
		}
		else {
			memLiteral = new MemLiteral(this, literal.getLabel());
		}

		boolean wasNew = literalRegistry.add(memLiteral);
		assert wasNew : "Created a duplicate MemLiteral for literal " + literal;

		return memLiteral;
	}

	public synchronized URI createURI(String uri) {
		URI tempURI = new URIImpl(uri);
		MemURI memURI = getMemURI(tempURI);

		if (memURI == null) {
			memURI = createMemURI(tempURI);
		}

		return memURI;
	}

	public synchronized URI createURI(String namespace, String localName) {
		URI tempURI = null;

		// Reuse supplied namespace and local name strings if possible
		if (URIUtil.isCorrectURISplit(namespace, localName)) {
			if (namespace.indexOf(':') == -1) {
				throw new IllegalArgumentException("Not a valid (absolute) URI: " + namespace + localName);
			}

			tempURI = new MemURI(this, namespace, localName);
		}
		else {
			tempURI = new URIImpl(namespace + localName);
		}

		MemURI memURI = uriRegistry.get(tempURI);

		if (memURI == null) {
			memURI = createMemURI(tempURI);
		}

		return memURI;
	}

	public synchronized BNode createBNode(String nodeID) {
		BNode tempBNode = new BNodeImpl(nodeID);
		MemBNode memBNode = getMemBNode(tempBNode);

		if (memBNode == null) {
			memBNode = createMemBNode(tempBNode);
		}

		return memBNode;
	}

	public synchronized Literal createLiteral(String value) {
		Literal tempLiteral = new LiteralImpl(value);
		MemLiteral memLiteral = literalRegistry.get(tempLiteral);

		if (memLiteral == null) {
			memLiteral = createMemLiteral(tempLiteral);
		}

		return memLiteral;
	}

	public synchronized Literal createLiteral(String value, String language) {
		Literal tempLiteral = new LiteralImpl(value, language);
		MemLiteral memLiteral = literalRegistry.get(tempLiteral);

		if (memLiteral == null) {
			memLiteral = createMemLiteral(tempLiteral);
		}

		return memLiteral;
	}

	public synchronized Literal createLiteral(String value, URI datatype) {
		Literal tempLiteral = new LiteralImpl(value, datatype);
		MemLiteral memLiteral = literalRegistry.get(tempLiteral);

		if (memLiteral == null) {
			memLiteral = createMemLiteral(tempLiteral);
		}

		return memLiteral;
	}

	@Override
	public synchronized Literal createLiteral(boolean value) {
		MemLiteral newLiteral = new BooleanMemLiteral(this, value);
		return getSharedLiteral(newLiteral);
	}

	@Override
	protected synchronized Literal createIntegerLiteral(Number n, URI datatype) {
		MemLiteral newLiteral = new IntegerMemLiteral(this, BigInteger.valueOf(n.longValue()), datatype);
		return getSharedLiteral(newLiteral);
	}

	@Override
	protected synchronized Literal createFPLiteral(Number n, URI datatype) {
		MemLiteral newLiteral = new NumericMemLiteral(this, n, datatype);
		return getSharedLiteral(newLiteral);
	}

	@Override
	public synchronized Literal createLiteral(XMLGregorianCalendar calendar) {
		MemLiteral newLiteral = new CalendarMemLiteral(this, calendar);
		return getSharedLiteral(newLiteral);
	}

	private Literal getSharedLiteral(MemLiteral newLiteral) {
		MemLiteral sharedLiteral = literalRegistry.get(newLiteral);

		if (sharedLiteral == null) {
			boolean wasNew = literalRegistry.add(newLiteral);
			assert wasNew : "Created a duplicate MemLiteral for literal " + newLiteral;
			sharedLiteral = newLiteral;
		}

		return sharedLiteral;
	}

	public Statement createStatement(Resource subject, URI predicate, Value object) {
		return new StatementImpl(subject, predicate, object);
	}

	public Statement createStatement(Resource subject, URI predicate, Value object, Resource context) {
		if (context == null) {
			return new StatementImpl(subject, predicate, object);
		}
		else {
			return new ContextStatementImpl(subject, predicate, object, context);
		}
	}
}
