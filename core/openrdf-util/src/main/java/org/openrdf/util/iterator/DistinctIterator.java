/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.util.iterator;

import java.util.HashSet;
import java.util.Iterator;

/**
 * An Iterator that filters any duplicate elements from an underlying iterator.
 */
public class DistinctIterator<E> extends FilterIterator<E> {

	/*-----------*
	 * Variables *
	 *-----------*/
	
	/**
	 * The elements that have already been returned.
	 */
	private HashSet<E> _excludeSet;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new DistinctIterator.
	 *
	 * @param iter The underlying iterator.
	 */
	public DistinctIterator(Iterator<E> iter) {
		super(iter);

		_excludeSet = new HashSet<E>();
	}

	/*---------*
	 * Methods *
	 *---------*/

	/**
	 * Returns <tt>true</tt> if the specified object hasn't been seen before.
	 */
	protected boolean accept(E object) {
		if (_excludeSet.contains(object)) {
			// object has already been returned
			return false;
		}
		else {
			_excludeSet.add(object);
			return true;
		}
	}

	// overrides FilterIterator.close()
	public void close() {
		if (!isClosed()) {
			_excludeSet.clear();
		}
			
		super.close();
	}
}
