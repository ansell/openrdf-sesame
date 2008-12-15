/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.cursors;

import org.openrdf.model.Statement;
import org.openrdf.results.Cursor;
import org.openrdf.results.base.FilteringCursor;

/**
 * Named contexts are matched by retrieving all statements from the store and
 * filtering out the statements that do not have a context.
 * 
 * @author James Leigh
 */
public class NamedContextCursor extends FilteringCursor<Statement> {

	public NamedContextCursor(Cursor<? extends Statement> delegate) {
		super(delegate);
	}

	@Override
	protected boolean accept(Statement next) {
		return next.getContext() != null;
	}

}
