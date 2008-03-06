/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.util.iterator;

import java.util.Iterator;

/**
 * An iterator that limits the amount of elements that it returns from an
 * underlying iterator to a fixed amount. This class returns the first
 * <tt>limit</tt> elements from the underlying iterator and drops the rest.
 */
public class LimitIterator<E> extends IteratorWrapper<E> {

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * The amount of elements to return.
	 */
	private int _limit;

	/**
	 * The number of elements that have been returned so far.
	 */
	private int _returnCount;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new LimitIterator.
	 *
	 * @param iter The underlying iterator, must not be <tt>null</tt>.
	 * @param limit The number of query answers to return, must be larger than
	 * 0.
	 */
	public LimitIterator(Iterator<? extends E> iter, int limit) {
		super(iter);
	
		assert iter != null;
		assert limit > 0;
		
		_limit = limit;
		_returnCount = 0;
	}

	/*---------*
	 * Methods *
	 *---------*/
	
	// overrides IteratorWrapper.next()
	public E next() {
		E result = super.next();

		_returnCount++;
			
		if (_returnCount == _limit) {
			close();
		}
			
		return result;
	}
}
