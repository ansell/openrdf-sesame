/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */

package info.aduna.iteration;

import java.util.HashSet;

/**
 * An Iteration that filters any duplicate elements from an underlying iterator.
 */
public class DistinctIteration<E, X extends Exception> extends FilterIteration<E, X> {

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * The elements that have already been returned.
	 */
	private final HashSet<E> excludeSet;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new DistinctIterator.
	 * 
	 * @param iter
	 *        The underlying iterator.
	 */
	public DistinctIteration(Iteration<? extends E, ? extends X> iter) {
		super(iter);

		excludeSet = new HashSet<E>();
	}

	/*---------*
	 * Methods *
	 *---------*/

	/**
	 * Returns <tt>true</tt> if the specified object hasn't been seen before.
	 */
	protected boolean accept(E object) {
		if (excludeSet.contains(object)) {
			// object has already been returned
			return false;
		}
		else {
			excludeSet.add(object);
			return true;
		}
	}
}
