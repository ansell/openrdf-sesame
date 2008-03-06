/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.model.impl;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

/**
 * Default implementation of the ValueFactory interface that uses the RDF model
 * classes from this package.
 */
public class ValueFactoryImpl extends ValueFactoryBase {

	/*-----------*
	 * Constants *
	 *-----------*/

	private static final String BNODE_PREFIX = "node";

	/*-----------*
	 * Variables *
	 *-----------*/

	private int _lastBNodeID;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new ValueFactoryImpl.
	 */
	public ValueFactoryImpl() {
		_lastBNodeID = 0;
	}

	/*---------*
	 * Methods *
	 *---------*/

	// Implements ValueFactory.createURI(String)
	public URI createURI(String uri) {
		return new URIImpl(uri);
	}

	// Implements ValueFactory.createURI(String, String)
	public URI createURI(String namespace, String localName) {
		return new URIImpl(namespace, localName);
	}

	// Implements ValueFactory.createBNode()
	public BNode createBNode() {
		return new BNodeImpl(BNODE_PREFIX + _lastBNodeID++);
	}

	// Implements ValueFactory.createBNode(String)
	public BNode createBNode(String nodeID) {
		return new BNodeImpl(nodeID);
	}

	// Implements ValueFactory.createLiteral(String)
	public Literal createLiteral(String value) {
		return new LiteralImpl(value);
	}

	// Implements ValueFactory.createLiteral(String, String)
	public Literal createLiteral(String value, String language) {
		return new LiteralImpl(value, language);
	}

	// Implements ValueFactory.createLiteral(String, URI)
	public Literal createLiteral(String value, URI datatype) {
		return new LiteralImpl(value, datatype);
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
