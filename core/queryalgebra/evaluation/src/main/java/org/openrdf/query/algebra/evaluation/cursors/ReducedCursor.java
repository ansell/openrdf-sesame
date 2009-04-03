/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008-2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.cursors;

import org.openrdf.cursor.Cursor;
import org.openrdf.cursor.FilteringCursor;

/**
 * Removes consecutive duplicates from the object stream.
 * 
 * @author James Leigh
 * @author Arjohn Kampman
 */
public class ReducedCursor<E> extends FilteringCursor<E> {

	private E previousObject;

	public ReducedCursor(Cursor<? extends E> delegate) {
		super(delegate);
	}

	@Override
	protected boolean accept(E nextObject) {
		if (nextObject.equals(previousObject)) {
			return false;
		}
		else {
			previousObject = nextObject;
			return true;
		}
	}
}
