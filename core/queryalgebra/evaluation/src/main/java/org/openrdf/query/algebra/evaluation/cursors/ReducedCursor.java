/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.cursors;

import org.openrdf.results.Cursor;
import org.openrdf.results.base.FilteringCursor;


/**
 * Remove duplicates that appear next to each other.
 * 
 * @author James Leigh
 */
public class ReducedCursor<E> extends FilteringCursor<E> {
	private E last;

	public ReducedCursor(Cursor<? extends E> delegate) {
		super(delegate);
	}

	@Override
	protected boolean accept(E next) {
		boolean accept = !next.equals(last);
		last = next;
		return accept;
	}

}
