/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.model;


/**
 * A factory for creating URIs, blank nodes, literals and statements.
 * 
 * @author Arjohn Kampman
 */
public interface ValueFactory extends URIFactory, LiteralFactory {

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
