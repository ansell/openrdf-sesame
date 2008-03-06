/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.model.impl;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

/**
 * An extension of {@link StatementImpl} that adds a context field.
 */
public class ContextStatementImpl extends StatementImpl {

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * The statement's context, if applicable.
	 */
	private final Resource _context;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new Statement with the supplied subject, predicate and object
	 * for the specified associated context.
	 *
	 * @param subject The statement's subject, must not be <tt>null</tt>.
	 * @param predicate The statement's predicate, must not be <tt>null</tt>.
	 * @param object The statement's object, must not be <tt>null</tt>.
	 * @param context The statement's context, <tt>null</tt> to indicate the
	 * null context.
	 */
	public ContextStatementImpl(Resource subject, URI predicate, Value object, Resource context) {
		super(subject, predicate, object);
		_context = context;
	}

	/*---------*
	 * Methods *
	 *---------*/

	// Overrides StatementImpl.getContext()
	public Resource getContext() {
		return _context;
	}

	// Overrides ContextStatement.toString()
	public String toString() {
		StringBuilder sb = new StringBuilder(256);
		
		sb.append( super.toString() );
		sb.append("[").append(_context).append("]");

		return sb.toString();
	}
}
