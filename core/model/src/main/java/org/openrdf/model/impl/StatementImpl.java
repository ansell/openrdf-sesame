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
public class StatementImpl extends StatementBase {

	/*-----------*
	 * Constants *
	 *-----------*/

	private static final long serialVersionUID = 8707542157460228077L;

	/**
	 * The statement's subject.
	 */
	private final Resource subject;

	/**
	 * The statement's predicate.
	 */
	private final URI predicate;

	/**
	 * The statement's object.
	 */
	private final Value object;

	/**
	 * The statement's context, if applicable.
	 */
	private final Resource context;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new Statement with the supplied subject, predicate and object.
	 * 
	 * @param subject
	 *        The statement's subject, must not be <tt>null</tt>.
	 * @param predicate
	 *        The statement's predicate, must not be <tt>null</tt>.
	 * @param object
	 *        The statement's object, must not be <tt>null</tt>.
	 */
	public StatementImpl(Resource subject, URI predicate, Value object) {
		assert (subject != null);
		assert (predicate != null);
		assert (object != null);

		this.subject = subject;
		this.predicate = predicate;
		this.object = object;
		this.context = null;
	}

	/**
	 * Creates a new Statement with the supplied subject, predicate and object
	 * for the specified associated context.
	 * 
	 * @param subject
	 *        The statement's subject, must not be <tt>null</tt>.
	 * @param predicate
	 *        The statement's predicate, must not be <tt>null</tt>.
	 * @param object
	 *        The statement's object, must not be <tt>null</tt>.
	 * @param context
	 *        The statement's context, <tt>null</tt> to indicate no context is
	 *        associated.
	 */
	public StatementImpl(Resource subject, URI predicate, Value object, Resource context) {
		assert (subject != null);
		assert (predicate != null);
		assert (object != null);

		this.subject = subject;
		this.predicate = predicate;
		this.object = object;
		this.context = context;
	}

	/*---------*
	 * Methods *
	 *---------*/

	// Implements Statement.getSubject()
	public Resource getSubject() {
		return subject;
	}

	// Implements Statement.getPredicate()
	public URI getPredicate() {
		return predicate;
	}

	// Implements Statement.getObject()
	public Value getObject() {
		return object;
	}

	// Implements Statement.getContext()
	public Resource getContext() {
		return context;
	}
}
