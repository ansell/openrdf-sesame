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
	private WeakObjectRegistry<MemURI> _uriRegistry = new WeakObjectRegistry<MemURI>();

	/**
	 * Registry containing the set of MemBNode objects as used by a MemoryStore.
	 * This registry enables the reuse of objects, minimizing the number of
	 * objects in main memory.
	 */
	private WeakObjectRegistry<MemBNode> _bnodeRegistry = new WeakObjectRegistry<MemBNode>();

	/**
	 * Registry containing the set of MemLiteral objects as used by a
	 * MemoryStore. This registry enables the reuse of objects, minimizing the
	 * number of objects in main memory.
	 */
	private WeakObjectRegistry<MemLiteral> _literalRegistry = new WeakObjectRegistry<MemLiteral>();

	/**
	 * Registry containing the set of namespce strings as used by MemURI objects
	 * in a MemoryStore. This registry enables the reuse of objects, minimizing
	 * the number of objects in main memory.
	 */
	private WeakObjectRegistry<String> _namespaceRegistry = new WeakObjectRegistry<String>();

	/**
	 * The ID for the next bnode that is created.
	 */
	private int _nextBNodeID;

	/**
	 * The prefix for any new bnode IDs.
	 */
	private String _bnodePrefix;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new MemValueFactory.
	 */
	public MemValueFactory() {
		_updateBNodePrefix();
	}

	/*---------*
	 * Methods *
	 *---------*/

	/**
	 * Generates a new bnode prefix based on the current time and resets
	 * <tt>_nextBNodeID</tt>.
	 */
	private void _updateBNodePrefix() {
		// BNode prefix is based on currentTimeMillis(). Combined with a
		// sequential number per session, this gives a unique identifier.
		_bnodePrefix = "node" + Long.toString(System.currentTimeMillis(), 32) + "x";
		_nextBNodeID = 1;
	}

	/**
	 * Returns a previously created MemValue that is equal to the supplied value,
	 * or <tt>null</tt> if the supplied value is a new value.
	 * 
	 * @param value
	 *        A Value object.
	 * @return A previously created MemValue that is equal to v, or null if no
	 *         such value exists.
	 */
	public MemValue getMemValue(Value value) {
		if (value instanceof Resource) {
			return getMemResource((Resource)value);
		}
		else if (value instanceof Literal) {
			return getMemLiteral((Literal)value);
		}
		else {
			throw new IllegalArgumentException("value is not a Resource or Literal: " + value);
		}
	}

	/**
	 * See getMemValue() for description.
	 */
	public MemResource getMemResource(Resource resource) {
		if (resource == null) {
			return (MemResource)null;
		}
		else if (resource instanceof URI) {
			return getMemURI((URI)resource);
		}
		else if (resource instanceof BNode) {
			return getMemBNode((BNode)resource);
		}
		else {
			throw new IllegalArgumentException("resource is not a URI, BNode or MemNullContext: " + resource);
		}
	}

	/**
	 * See getMemValue() for description.
	 */
	public MemURI getMemURI(URI uri) {
		if (_isOwnMemValue(uri)) {
			return (MemURI)uri;
		}
		else {
			return _uriRegistry.get(uri);
		}
	}

	/**
	 * See getMemValue() for description.
	 */
	public MemBNode getMemBNode(BNode bnode) {
		if (_isOwnMemValue(bnode)) {
			return (MemBNode)bnode;
		}
		else {
			return _bnodeRegistry.get(bnode);
		}
	}

	/**
	 * See getMemValue() for description.
	 */
	public MemLiteral getMemLiteral(Literal literal) {
		if (_isOwnMemValue(literal)) {
			return (MemLiteral)literal;
		}
		else {
			return _literalRegistry.get(literal);
		}
	}

	/**
	 * Checks whether the supplied value is an instance of <tt>MemValue</tt>
	 * and whether it has been created by this MemValueFactory.
	 */
	private boolean _isOwnMemValue(Value value) {
		return value instanceof MemValue && ((MemValue)value).getCreator() == this;
	}

	/**
	 * Gets all URIs that are managed by this value factory.
	 * 
	 * @return An unmodifiable Set of MemURI objects.
	 */
	public Set<MemURI> getMemURIs() {
		return Collections.unmodifiableSet(_uriRegistry);
	}

	/**
	 * Gets all bnodes that are managed by this value factory.
	 * 
	 * @return An unmodifiable Set of MemBNode objects.
	 */
	public Set<MemBNode> getMemBNodes() {
		return Collections.unmodifiableSet(_bnodeRegistry);
	}

	/**
	 * Gets all literals that are managed by this value factory.
	 * 
	 * @return An unmodifiable Set of MemURI objects.
	 */
	public Set<MemLiteral> getMemLiterals() {
		return Collections.unmodifiableSet(_literalRegistry);
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
	public MemURI createMemURI(URI uri) {
		// Namespace strings are relatively large objects and are shared
		// between uris
		String namespace = uri.getNamespace();
		String sharedNamespace = _namespaceRegistry.get(namespace);

		if (sharedNamespace == null) {
			// New namespace, add it to the register.
			_namespaceRegistry.add(namespace);
		}
		else {
			// Use the shared namespace
			namespace = sharedNamespace;
		}

		// Create a new MemURI and add it to the registry
		MemURI memURI = new MemURI(this, namespace, uri.getLocalName());

		boolean wasNew = _uriRegistry.add(memURI);
		assert wasNew : "Created a duplicate MemURI for URI " + uri;

		return memURI;
	}

	/**
	 * See createMemValue() for description.
	 */
	public MemBNode createMemBNode(BNode bnode) {
		MemBNode memBNode = new MemBNode(this, bnode.getID());

		boolean wasNew = _bnodeRegistry.add(memBNode);
		assert wasNew : "Created a duplicate MemBNode for bnode " + bnode;

		return memBNode;
	}

	/**
	 * See createMemValue() for description.
	 */
	public MemLiteral createMemLiteral(Literal literal) {
		MemLiteral memLiteral = null;

		URI datatype = literal.getDatatype();
		if (datatype != null) {
			try {
				if (XMLDatatypeUtil.isIntegerDatatype(datatype)) {
					memLiteral = new IntegerMemLiteral(this, literal.integerValue(), datatype);
				}
				else if (datatype.equals(XMLSchema.DECIMAL)) {
					memLiteral = new DecimalMemLiteral(this, literal.decimalValue(), datatype);
				}
				else if (datatype.equals(XMLSchema.FLOAT)) {
					memLiteral = new NumericMemLiteral(this, literal.floatValue(), datatype);
				}
				else if (datatype.equals(XMLSchema.DOUBLE)) {
					memLiteral = new NumericMemLiteral(this, literal.doubleValue(), datatype);
				}
				else if (datatype.equals(XMLSchema.BOOLEAN)) {
					memLiteral = new BooleanMemLiteral(this, literal.booleanValue());
				}
				else if (datatype.equals(XMLSchema.DATETIME)) {
					memLiteral = new CalendarMemLiteral(this, literal.calendarValue());
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

		boolean wasNew = _literalRegistry.add(memLiteral);
		assert wasNew : "Created a duplicate MemLiteral for literal " + literal;

		return memLiteral;
	}

	public URI createURI(String uri) {
		URI tempURI = new URIImpl(uri);
		MemURI memURI = getMemURI(tempURI);

		if (memURI == null) {
			memURI = createMemURI(tempURI);
		}

		return memURI;
	}

	public URI createURI(String namespace, String localName) {
		// TODO: reuse supplied namespace and localname if possible
		URI tempURI = new URIImpl(namespace + localName);
		MemURI memURI = getMemURI(tempURI);

		if (memURI == null) {
			memURI = createMemURI(tempURI);
		}

		return memURI;
	}

	public BNode createBNode() {
		if (_nextBNodeID == Integer.MAX_VALUE) {
			// Start with a new bnode prefix
			_updateBNodePrefix();
		}

		return createBNode(_bnodePrefix + _nextBNodeID++);
	}

	public BNode createBNode(String nodeID) {
		BNode tempBNode = new BNodeImpl(nodeID);
		MemBNode memBNode = getMemBNode(tempBNode);

		if (memBNode == null) {
			memBNode = createMemBNode(tempBNode);
		}

		return memBNode;
	}

	public Literal createLiteral(String value) {
		Literal tempLiteral = new LiteralImpl(value);
		MemLiteral memLiteral = _literalRegistry.get(tempLiteral);

		if (memLiteral == null) {
			memLiteral = createMemLiteral(tempLiteral);
		}

		return memLiteral;
	}

	public Literal createLiteral(String value, String language) {
		Literal tempLiteral = new LiteralImpl(value, language);
		MemLiteral memLiteral = _literalRegistry.get(tempLiteral);

		if (memLiteral == null) {
			memLiteral = createMemLiteral(tempLiteral);
		}

		return memLiteral;
	}

	public Literal createLiteral(String value, URI datatype) {
		Literal tempLiteral = new LiteralImpl(value, datatype);
		MemLiteral memLiteral = _literalRegistry.get(tempLiteral);

		if (memLiteral == null) {
			memLiteral = createMemLiteral(tempLiteral);
		}

		return memLiteral;
	}

	@Override
	public Literal createLiteral(boolean value)
	{
		MemLiteral newLiteral = new BooleanMemLiteral(this, value);
		return _getSharedLiteral(newLiteral);
	}

	@Override
	protected Literal createIntegerLiteral(Number n, URI datatype)
	{
		MemLiteral newLiteral = new IntegerMemLiteral(this, BigInteger.valueOf(n.longValue()), datatype);
		return _getSharedLiteral(newLiteral);
	}

	@Override
	protected Literal createFPLiteral(Number n, URI datatype)
	{
		MemLiteral newLiteral = new NumericMemLiteral(this, n, datatype);
		return _getSharedLiteral(newLiteral);
	}

	@Override
	public Literal createLiteral(XMLGregorianCalendar calendar)
	{
		MemLiteral newLiteral = new CalendarMemLiteral(this, calendar);
		return _getSharedLiteral(newLiteral);
	}

	private Literal _getSharedLiteral(MemLiteral newLiteral) {
		MemLiteral sharedLiteral = _literalRegistry.get(newLiteral);

		if (sharedLiteral == null) {
			boolean wasNew = _literalRegistry.add(newLiteral);
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
