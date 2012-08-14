/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.model;

import javax.xml.datatype.XMLGregorianCalendar;

/**
 * A factory for creating URIs, blank nodes, literals and statements.
 * 
 * @author Arjohn Kampman
 */
public interface ValueFactory {

	/**
	 * Creates a new URI from the supplied string-representation.
	 * 
	 * @param uri
	 *        A string-representation of a URI.
	 * @return An object representing the URI.
	 * @throws IlllegalArgumentException
	 *         If the supplied string does not resolve to a legal (absolute) URI.
	 */
	public URI createURI(String uri);

	/**
	 * Creates a new URI from the supplied namespace and local name. Calling this
	 * method is funtionally equivalent to calling
	 * {@link #createURI(String) createURI(namespace+localName)}, but allows the
	 * ValueFactory to reuse supplied namespace and local name strings whenever
	 * possible. Note that the values returned by {@link URI#getNamespace()} and
	 * {@link URI#getLocalName()} are not necessarily the same as the values that
	 * are supplied to this method.
	 * 
	 * @param namespace
	 *        The URI's namespace.
	 * @param localName
	 *        The URI's local name.
	 * @throws IllegalArgumentException
	 *         If the supplied namespace and localname do not resolve to a legal
	 *         (absolute) URI.
	 */
	public URI createURI(String namespace, String localName);

	/**
	 * Creates a new bNode.
	 * 
	 * @return An object representing the bNode.
	 */
	public BNode createBNode();

	/**
	 * Creates a new blank node with the given node identifier.
	 * 
	 * @param nodeID
	 *        The blank node identifier.
	 * @return An object representing the blank node.
	 */
	public BNode createBNode(String nodeID);

	/**
	 * Creates a new literal with the supplied label.
	 * 
	 * @param label
	 *        The literal's label.
	 */
	public Literal createLiteral(String label);

	/**
	 * Creates a new literal with the supplied label and language attribute.
	 * 
	 * @param label
	 *        The literal's label.
	 * @param language
	 *        The literal's language attribute, or <tt>null</tt> if the literal
	 *        doesn't have a language.
	 */
	public Literal createLiteral(String label, String language);

	/**
	 * Creates a new literal with the supplied label and datatype.
	 * 
	 * @param label
	 *        The literal's label.
	 * @param datatype
	 *        The literal's datatype, or <tt>null</tt> if the literal doesn't
	 *        have a datatype.
	 */
	public Literal createLiteral(String label, URI datatype);

	/**
	 * Creates a new <tt>xsd:boolean</tt>-typed literal representing the
	 * specified value.
	 * 
	 * @param value
	 *        The value for the literal.
	 * @return An <tt>xsd:boolean</tt>-typed literal for the specified value.
	 */
	public Literal createLiteral(boolean value);

	/**
	 * Creates a new <tt>xsd:byte</tt>-typed literal representing the
	 * specified value.
	 * 
	 * @param value
	 *        The value for the literal.
	 * @return An <tt>xsd:byte</tt>-typed literal for the specified value.
	 */
	public Literal createLiteral(byte value);

	/**
	 * Creates a new <tt>xsd:short</tt>-typed literal representing the
	 * specified value.
	 * 
	 * @param value
	 *        The value for the literal.
	 * @return An <tt>xsd:short</tt>-typed literal for the specified value.
	 */
	public Literal createLiteral(short value);

	/**
	 * Creates a new <tt>xsd:int</tt>-typed literal representing the specified
	 * value.
	 * 
	 * @param value
	 *        The value for the literal.
	 * @return An <tt>xsd:int</tt>-typed literal for the specified value.
	 */
	public Literal createLiteral(int value);

	/**
	 * Creates a new <tt>xsd:long</tt>-typed literal representing the
	 * specified value.
	 * 
	 * @param value
	 *        The value for the literal.
	 * @return An <tt>xsd:long</tt>-typed literal for the specified value.
	 */
	public Literal createLiteral(long value);

	/**
	 * Creates a new <tt>xsd:float</tt>-typed literal representing the
	 * specified value.
	 * 
	 * @param value
	 *        The value for the literal.
	 * @return An <tt>xsd:float</tt>-typed literal for the specified value.
	 */
	public Literal createLiteral(float value);

	/**
	 * Creates a new <tt>xsd:double</tt>-typed literal representing the
	 * specified value.
	 * 
	 * @param value
	 *        The value for the literal.
	 * @return An <tt>xsd:double</tt>-typed literal for the specified value.
	 */
	public Literal createLiteral(double value);

	/**
	 * Creates a new literal representing the specified calendar that is typed
	 * using the appropriate XML Schema date/time datatype.
	 * 
	 * @param calendar
	 *        The value for the literal.
	 * @return An typed literal for the specified calendar.
	 */
	public Literal createLiteral(XMLGregorianCalendar calendar);

	/**
	 * Creates a new statement with the supplied subject, predicate and object.
	 * 
	 * @param subject
	 *        The statement's subject.
	 * @param predicate
	 *        The statement's predicate.
	 * @param object
	 *        The statement's object.
	 * @return The created statement.
	 */
	public Statement createStatement(Resource subject, URI predicate, Value object);

	/**
	 * Creates a new statement with the supplied subject, predicate and object
	 * and associated context.
	 * 
	 * @param subject
	 *        The statement's subject.
	 * @param predicate
	 *        The statement's predicate.
	 * @param object
	 *        The statement's object.
	 * @param context
	 *        The statement's context.
	 * @return The created statement.
	 */
	public Statement createStatement(Resource subject, URI predicate, Value object, Resource context);
}
