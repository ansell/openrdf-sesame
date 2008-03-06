/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.util.iterator;

import java.util.Iterator;

/**
 * An iterator that skips the first <tt>offset</tt> elements from an underlying
 * iterator.
 */
public class OffsetIterator<E> extends FilterIterator<E> {

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * The offset (0-based) of the first element to return.
	 */
	private int _offset;

	/**
	 * The number of elements that have been dropped so far.
	 */
	private int _droppedResults;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new OffsetIterator.
	 *
	 * @param iter The underlying iterator, must not be <tt>null</tt>.
	 * @param offset The number of elements to skip, must be larger than or
	 * equal to 0.
	 */
	public OffsetIterator(Iterator<E> iter, int offset) {
		super(iter);
		
		assert offset >= 0;
		
		_offset = offset;
		_droppedResults = 0;
	}

	/*---------*
	 * Methods *
	 *---------*/

	/**
	 * Returns <tt>false</tt> for the first OFFSET objects.
	 */
	protected boolean accept(E object) {
		if (_droppedResults < _offset) {
			_droppedResults++;
			return false;
		}
		else {
			return true;
		}
	}
}
