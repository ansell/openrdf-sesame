/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.memory.model;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.BNodeImpl;
import org.openrdf.model.impl.ContextStatementImpl;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.impl.ValueFactoryBase;
import org.openrdf.sail.Namespace;
import org.openrdf.sail.helpers.NamespaceImpl;

/**
 * A factory for MemValue objects that keeps track of created objects to prevent
 * the creation of duplicate objects, minimizing memory usage as a result.
 */
public class MemValueFactory extends ValueFactoryBase {

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * A value representing the null context.
	 */
	private MemNullContext _nullContext;

	/**
	 * A Map containing identical MemURI objects as key and value. This Map is
	 * used for sharing MemURI objects: a shared MemURI can be retrieved by using
	 * a URI or MemURI object as search key.
	 */
	private WeakObjectRegistry<MemURI> _uriRegistry = new WeakObjectRegistry<MemURI>();

	/**
	 * A Map containing identical MemBNode objects as key and value. This Map is
	 * used for sharing MemBNode objects: a shared MemBNode can be retrieved by
	 * using a BNode or MemBNode object as search key.
	 */
	private WeakObjectRegistry<MemBNode> _bnodeRegistry = new WeakObjectRegistry<MemBNode>();

	/**
	 * A Map containing identical MemLiteral objects as key and value. This Map
	 * is used for sharing MemLiteral objects: a shared MemLiteral can be
	 * retrieved by using a Literal or MemLiteral object as search key.
	 */
	private WeakObjectRegistry<MemLiteral> _literalRegistry = new WeakObjectRegistry<MemLiteral>();

	/**
	 * Registry for namespaces stored by their name that is used to share the
	 * (relatively large) namespace-name strings.
	 */
	private Map<String, NamespaceImpl> _namespacesTable = new LinkedHashMap<String, NamespaceImpl>();

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
		_nullContext = new MemNullContext(this);
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

	public Collection<? extends Namespace> getNamespaces() {
		return Collections.unmodifiableCollection(_namespacesTable.values());
	}

	public Namespace getNamespace(String namespaceName) {
		return _namespacesTable.get(namespaceName);
	}

	public boolean hasNamespace(String namespaceName) {
		return _namespacesTable.containsKey(namespaceName);
	}

	public void registerNamespace(String prefix, String name) {
		NamespaceImpl namespace = new NamespaceImpl(prefix, name);
		_namespacesTable.put(name, namespace);
	}

	public void setNamespace(String prefix, String name) {
		// Check if prefix is already used for another namespace
		for (NamespaceImpl ns : _namespacesTable.values()) {
			if (ns.getPrefix().equals(prefix) && !ns.getName().equals(name)) {
				throw new IllegalArgumentException("Prefix '" + prefix
						+ "' is already used for another namespace");
			}
		}

		NamespaceImpl n = _namespacesTable.get(name);
		if (n != null) {
			// update the prefix
			n.setPrefix(prefix);
		}
		else {
			registerNamespace(prefix, name);
		}
	}

	public void removeNamespace(String prefix) {
		Iterator<NamespaceImpl> iter = _namespacesTable.values().iterator();

		while (iter.hasNext()) {
			NamespaceImpl ns = iter.next();

			if (ns.getPrefix().equals(prefix)) {
				iter.remove();
				break;
			}
		}
	}

	/**
	 * Gets the MemNullContext object that is used as the context node for
	 * statements that are in the null context.
	 */
	public MemNullContext getNullContext() {
		return _nullContext;
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
		if (resource instanceof URI) {
			return getMemURI((URI)resource);
		}
		else if (resource instanceof BNode) {
			return getMemBNode((BNode)resource);
		}
		else if (resource instanceof MemNullContext && _isOwnMemValue(resource)) {
			return (MemNullContext)resource;
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
		NamespaceImpl n = _namespacesTable.get(namespace);

		if (n != null) {
			// Use the shared namespace
			namespace = n.getName();
		}

		// Create a new MemURI
		MemURI memURI = new MemURI(this, namespace, uri.getLocalName());

		boolean wasNew = _uriRegistry.add(memURI);

		if (!wasNew) {
			throw new IllegalArgumentException("Created a duplicate MemURI for URI " + uri);
		}

		return memURI;
	}

	/**
	 * See createMemValue() for description.
	 */
	public MemBNode createMemBNode(BNode bnode) {
		MemBNode memBNode = new MemBNode(this, bnode.getID());

		boolean wasNew = _bnodeRegistry.add(memBNode);

		if (!wasNew) {
			throw new IllegalArgumentException("Created a duplicate MemBNode for bnode " + bnode);
		}

		return memBNode;
	}

	/**
	 * See createMemValue() for description.
	 */
	public MemLiteral createMemLiteral(Literal literal) {
		MemLiteral memLiteral = null;

		if (literal.getDatatype() != null) {
			memLiteral = new MemLiteral(this, literal.getLabel(), literal.getDatatype());
		}
		else if (literal.getLanguage() != null) {
			memLiteral = new MemLiteral(this, literal.getLabel(), literal.getLanguage());
		}
		else {
			memLiteral = new MemLiteral(this, literal.getLabel());
		}

		boolean wasNew = _literalRegistry.add(memLiteral);

		if (!wasNew) {
			throw new IllegalArgumentException("Created a duplicate MemLiteral for literal " + literal);
		}

		return memLiteral;
	}

	// Implements ValueFactory.createURI(String)
	public URI createURI(String uri) {
		URI tempURI = new URIImpl(uri);
		MemURI memURI = getMemURI(tempURI);

		if (memURI == null) {
			memURI = createMemURI(tempURI);
		}

		return memURI;
	}

	// Implements ValueFactory.createURI(String,String)
	public URI createURI(String namespace, String localName) {
		URI tempURI = new URIImpl(namespace, localName);
		MemURI memURI = getMemURI(tempURI);

		if (memURI == null) {
			memURI = createMemURI(tempURI);
		}

		return memURI;
	}

	// Implements ValueFactory.createBNode()
	public BNode createBNode() {
		if (_nextBNodeID == Integer.MAX_VALUE) {
			// Start with a new bnode prefix
			_updateBNodePrefix();
		}

		return createBNode(_bnodePrefix + _nextBNodeID++);
	}

	// Implements ValueFactory.createBNode(String)
	public BNode createBNode(String nodeID) {
		BNode tempBNode = new BNodeImpl(nodeID);
		MemBNode memBNode = getMemBNode(tempBNode);

		if (memBNode == null) {
			memBNode = createMemBNode(tempBNode);
		}

		return memBNode;
	}

	// Implements ValueFactory.createLiteral(String)
	public Literal createLiteral(String value) {
		Literal tempLiteral = new LiteralImpl(value);
		MemLiteral memLiteral = getMemLiteral(tempLiteral);

		if (memLiteral == null) {
			memLiteral = createMemLiteral(tempLiteral);
		}

		return memLiteral;
	}

	// Implements ValueFactory.createLiteral(String, String)
	public Literal createLiteral(String value, String language) {
		Literal tempLiteral = new LiteralImpl(value, language);
		MemLiteral memLiteral = getMemLiteral(tempLiteral);

		if (memLiteral == null) {
			memLiteral = createMemLiteral(tempLiteral);
		}

		return memLiteral;
	}

	// Implements ValueFactory.createLiteral(String, URI)
	public Literal createLiteral(String value, URI datatype) {
		Literal tempLiteral = new LiteralImpl(value, datatype);
		MemLiteral memLiteral = getMemLiteral(tempLiteral);

		if (memLiteral == null) {
			memLiteral = createMemLiteral(tempLiteral);
		}

		return memLiteral;
	}

	// Implements ValueFactory.createStatement(Resource, URI, Value)
	public Statement createStatement(Resource subject, URI predicate, Value object) {
		return new StatementImpl(subject, predicate, object);
	}

	// Implements ValueFactory.createStatement(Resource, URI, Value, Resource)
	public Statement createStatement(Resource subject, URI predicate, Value object, Resource context) {
		return new ContextStatementImpl(subject, predicate, object, context);
	}
}
