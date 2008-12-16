/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.util;

import javax.xml.datatype.XMLGregorianCalendar;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.LiteralFactory;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.URIFactory;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.BNodeImpl;
import org.openrdf.model.impl.StatementImpl;


/**
 *
 * @author James Leigh
 */
public class URILiteralFactory implements ValueFactory {

	private URIFactory uris;

	private LiteralFactory literals;

	public URILiteralFactory(URIFactory uris, LiteralFactory literals) {
		this.uris = uris;
		this.literals = literals;
	}

	/**
	 * @param value
	 * @return
	 * @see org.openrdf.model.LiteralFactory#createLiteral(boolean)
	 */
	public Literal createLiteral(boolean value) {
		return literals.createLiteral(value);
	}

	/**
	 * @param value
	 * @return
	 * @see org.openrdf.model.LiteralFactory#createLiteral(byte)
	 */
	public Literal createLiteral(byte value) {
		return literals.createLiteral(value);
	}

	/**
	 * @param value
	 * @return
	 * @see org.openrdf.model.LiteralFactory#createLiteral(double)
	 */
	public Literal createLiteral(double value) {
		return literals.createLiteral(value);
	}

	/**
	 * @param value
	 * @return
	 * @see org.openrdf.model.LiteralFactory#createLiteral(float)
	 */
	public Literal createLiteral(float value) {
		return literals.createLiteral(value);
	}

	/**
	 * @param value
	 * @return
	 * @see org.openrdf.model.LiteralFactory#createLiteral(int)
	 */
	public Literal createLiteral(int value) {
		return literals.createLiteral(value);
	}

	/**
	 * @param value
	 * @return
	 * @see org.openrdf.model.LiteralFactory#createLiteral(long)
	 */
	public Literal createLiteral(long value) {
		return literals.createLiteral(value);
	}

	/**
	 * @param value
	 * @return
	 * @see org.openrdf.model.LiteralFactory#createLiteral(short)
	 */
	public Literal createLiteral(short value) {
		return literals.createLiteral(value);
	}

	/**
	 * @param label
	 * @param language
	 * @return
	 * @see org.openrdf.model.LiteralFactory#createLiteral(java.lang.String, java.lang.String)
	 */
	public Literal createLiteral(String label, String language) {
		return literals.createLiteral(label, language);
	}

	/**
	 * @param label
	 * @param datatype
	 * @return
	 * @see org.openrdf.model.LiteralFactory#createLiteral(java.lang.String, org.openrdf.model.URI)
	 */
	public Literal createLiteral(String label, URI datatype) {
		return literals.createLiteral(label, datatype);
	}

	/**
	 * @param label
	 * @return
	 * @see org.openrdf.model.LiteralFactory#createLiteral(java.lang.String)
	 */
	public Literal createLiteral(String label) {
		return literals.createLiteral(label);
	}

	/**
	 * @param calendar
	 * @return
	 * @see org.openrdf.model.LiteralFactory#createLiteral(javax.xml.datatype.XMLGregorianCalendar)
	 */
	public Literal createLiteral(XMLGregorianCalendar calendar) {
		return literals.createLiteral(calendar);
	}

	/**
	 * @param uri
	 * @return
	 * @see org.openrdf.model.URIFactory#createURI(java.lang.String)
	 */
	public URI createURI(String uri) {
		return uris.createURI(uri);
	}

	/**
	 * @param namespace
	 * @param localName
	 * @return
	 * @see org.openrdf.model.URIFactory#createURI(java.lang.String, java.lang.String)
	 */
	public URI createURI(String namespace, String localName) {
		return uris.createURI(namespace, localName);
	}

	public BNode createBNode() {
		throw new UnsupportedOperationException();
	}

	public BNode createBNode(String nodeID) {
		return new BNodeImpl(nodeID);
	}

	public Statement createStatement(Resource subject, URI predicate, Value object) {
		return new StatementImpl(subject, predicate, object);
	}

	public Statement createStatement(Resource subject, URI predicate, Value object, Resource context) {
		return new StatementImpl(subject, predicate, object, context);
	}

}
