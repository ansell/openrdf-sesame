/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.model.impl;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

/**
 * An implementation of the {@link Statement} interface for statements that
 * don't have an associated context. For statements that do have an associated
 * context, {@link ContextStatementImpl} can be used.
 */
public class StatementImpl implements Statement {

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * The statement's subject.
	 */
    private final Resource _subject;

	/**
	 * The statement's predicate.
	 */
    private final URI _predicate;

	/**
	 * The statement's object.
	 */
	private final Value _object;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new Statement with the supplied subject, predicate and object.
	 *
	 * @param subject The statement's subject, must not be <tt>null</tt>.
	 * @param predicate The statement's predicate, must not be <tt>null</tt>.
	 * @param object The statement's object, must not be <tt>null</tt>.
	 */
	public StatementImpl(Resource subject, URI predicate, Value object) {
		assert(subject != null);
		assert(predicate != null);
		assert(object != null);
		
		_subject = subject;
		_predicate = predicate;
		_object = object;
	}
	
	/*---------*
	 * Methods *
	 *---------*/

	// Implements Statement.getSubject()
	public Resource getSubject() {
		return _subject;
	}

	// Implements Statement.getPredicate()
	public URI getPredicate() {
		return _predicate;
	}

	// Implements Statement.getObject()
	public Value getObject() {
		return _object;
	}
	
	// Implements Statement.getContext()
	public Resource getContext() {
		return null;
	}
	
	// Overrides Object.equals(Object), implements Statement.equals(Object)
    public boolean equals(Object other) {
		if (this == other) {
			return true;
		}

        if (other instanceof Statement) {
			Statement otherSt = (Statement)other;

			// The object is potentially the cheapest to check, as types
			// of these references might be different.

			// In general the number of different predicates in sets of
			// statements is the smallest, so predicate equality is checked
			// last.
			return
				_object.equals(otherSt.getObject()) &&
				_subject.equals(otherSt.getSubject()) &&
				_predicate.equals(otherSt.getPredicate());
        }

        return false;
    }

    // Overrides Object.hashCode(), implements Statement.hashCode()
    public int hashCode() {
		return
			961 * _subject.hashCode() +
			31 * _predicate.hashCode() +
			_object.hashCode();
	}

	/**
	 * Gives a String-representation of this Statement that can be used for
	 * debugging.
	 */
	public String toString() {
		StringBuilder sb = new StringBuilder(256);

		sb.append("(");
		sb.append(_subject);
		sb.append(", ");
		sb.append(_predicate);
		sb.append(", ");
		sb.append(_object);
		sb.append(")");

		return sb.toString();
	}
}
