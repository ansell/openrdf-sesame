/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.model;

import java.io.Serializable;

/**
 * An RDF statement, with optional associated context. A statement can have an
 * associated context in specific cases, for example when fetched from a
 * repository. The context field <em>does</em> influence statement equality; a
 * statement is equal to another statement if the subjects, predicates,
 * objects, and contexts are equal.
 */
public interface Statement extends Serializable {

	/**
	 * Gets the subject of this statement.
	 * 
	 * @return The statement's subject.
	 */
	public Resource getSubject();

	/**
	 * Gets the predicate of this statement.
	 * 
	 * @return The statement's predicate.
	 */
	public URI getPredicate();

	/**
	 * Gets the object of this statement.
	 * 
	 * @return The statement's object.
	 */
	public Value getObject();

	/**
	 * Gets the context of this statement.
	 * 
	 * @return The statement's context, or <tt>null</tt>.
	 */
	public Resource getContext();

	/**
	 * Compares a statement object to another object.
	 * 
	 * @param other
	 *        The object to compare this statement to.
	 * @return <tt>true</tt> if the other object is an instance of
	 *         {@link Statement} and if their subjects, predicates and objects
	 *         are equal.
	 */
	public boolean equalsIgnoreContext(Object other);

	/**
	 * Compares a statement object to another object.
	 * 
	 * @param other
	 *        The object to compare this statement to.
	 * @return <tt>true</tt> if the other object is an instance of
	 *         {@link Statement} and if their subjects, predicates, objects,
	 *         and contexts are equal.
	 */
	public boolean equals(Object other);

	/**
	 * The hash code of a statement is defined as:
	 * <tt>961 * subject.hashCode() + 31 * predicate.hashCode() + object.hashCode()</tt>.
	 * This is similar to how {@link String#hashCode String.hashCode()} is
	 * defined.
	 * 
	 * @return A hash code for the statement.
	 */
	public int hashCode();
}
