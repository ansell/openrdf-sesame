/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.model.impl;

import javax.xml.datatype.XMLGregorianCalendar;

import org.openrdf.model.BNode;
import org.openrdf.model.BNodeFactory;
import org.openrdf.model.Literal;
import org.openrdf.model.LiteralFactory;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.URIFactory;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;

/**
 * Default implementation of the ValueFactory interface that uses the RDF model
 * classes from this package.
 * 
 * @author Arjohn Kampman
 * @author James Leigh
 */
public class ValueFactoryImpl extends LiteralFactoryImpl implements ValueFactory {

	/*-----------*
	 * Constants *
	 *-----------*/

	private static final ValueFactoryImpl sharedInstance = new ValueFactoryImpl();

	public static ValueFactoryImpl getInstance() {
		return sharedInstance;
	}

	/*-----------*
	 * Variables *
	 *-----------*/

	private BNodeFactory bnodes;

	private URIFactory uris;

	private LiteralFactory literals;

	public ValueFactoryImpl() {
		this(new BNodeFactoryImpl(), new URIFactoryImpl(), new LiteralFactoryImpl());
	}

	public ValueFactoryImpl(URIFactory uris, LiteralFactory literals) {
		this(null, uris, literals);
	}

	public ValueFactoryImpl(BNodeFactory bnodes, ValueFactory values) {
		this(bnodes, values, values);
	}

	public ValueFactoryImpl(BNodeFactory bnodes, URIFactory uris, LiteralFactory literals) {
		this.bnodes = bnodes;
		this.uris = uris;
		this.literals = literals;
	}

	/*---------*
	 * Methods *
	 *---------*/

	public BNodeFactory getBNodeFactory() {
		return bnodes;
	}

	public URIFactory getURIFactory() {
		return uris;
	}

	public LiteralFactory getLiteralFactory() {
		return literals;
	}

	public BNode createBNode() {
		if (bnodes == null) {
			throw new UnsupportedOperationException();
		}
		return bnodes.createBNode();
	}

	public BNode createBNode(String nodeID) {
		if (bnodes == null) {
			throw new UnsupportedOperationException();
		}
		return bnodes.createBNode(nodeID);
	}

	public Statement createStatement(Resource subject, URI predicate, Value object) {
		return new StatementImpl(subject, predicate, object);
	}

	public Statement createStatement(Resource subject, URI predicate, Value object, Resource context) {
		return new StatementImpl(subject, predicate, object, context);
	}

	public URI createURI(String uri) {
		return uris.createURI(uri);
	}

	public URI createURI(String namespace, String localName) {
		return uris.createURI(namespace, localName);
	}

	@Override
	public Literal createLiteral(boolean value) {
		return literals.createLiteral(value);
	}

	@Override
	public Literal createLiteral(byte value) {
		return literals.createLiteral(value);
	}

	@Override
	public Literal createLiteral(double value) {
		return literals.createLiteral(value);
	}

	@Override
	public Literal createLiteral(float value) {
		return literals.createLiteral(value);
	}

	@Override
	public Literal createLiteral(int value) {
		return literals.createLiteral(value);
	}

	@Override
	public Literal createLiteral(long value) {
		return literals.createLiteral(value);
	}

	@Override
	public Literal createLiteral(short value) {
		return literals.createLiteral(value);
	}

	@Override
	public Literal createLiteral(String label, String language) {
		return literals.createLiteral(label, language);
	}

	@Override
	public Literal createLiteral(String label, URI datatype) {
		return literals.createLiteral(label, datatype);
	}

	@Override
	public Literal createLiteral(String label) {
		return literals.createLiteral(label);
	}

	@Override
	public Literal createLiteral(XMLGregorianCalendar calendar) {
		return literals.createLiteral(calendar);
	}

}
