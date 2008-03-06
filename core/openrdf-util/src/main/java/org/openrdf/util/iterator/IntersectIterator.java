/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.util.iterator;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * An iterator that returns the intersection of the results of two iterators.
 * Optionally, the iterator can be configured to filter duplicates from the
 * returned elements.
 * <p>
 * Note that duplicates can also be filtered by wrapping this iterator in a
 * {@link DistinctIterator}, but that has a bit more overhead as it adds a
 * second hash table lookup.  
 */
public class IntersectIterator<E> extends FilterIterator<E> {

	/*-----------*
	 * Variables *
	 *-----------*/
	
	private Iterator<? extends E> _arg2;
	private boolean _distinct;
	private boolean _initialized;
	
	private Set<E> _includeSet;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new IntersectIterator that returns the intersection of the
	 * results of two iterators. By default, duplicates are <em>not</em>
	 * filtered from the results.
	 *
	 * @param arg1 An iterator containing the first set of elements.
	 * @param arg2 An iterator containing the second set of elements.
	 */
	public IntersectIterator(Iterator<? extends E> arg1, Iterator<? extends E> arg2) {
		this(arg1, arg2, false);
	}
	
	/**
	 * Creates a new IntersectIterator that returns the intersection of the
	 * results of two iterators.
	 * 
	 * @param arg1 An iterator containing the first set of elements.
	 * @param arg2 An iterator containing the second set of elements.
	 * @param distinct Flag indicating whether duplicate elements should be
	 * filtered from the result.
	 */
	public IntersectIterator(
		Iterator<? extends E> arg1, Iterator<? extends E> arg2, boolean distinct)
	{
		super(arg1);
		
		assert arg2 != null;
		
		_arg2 = arg2;
		_distinct = distinct;
		_initialized = false;
	}

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Returns <tt>true</tt> if the object is in the set of elements of the
	 * second argument.
	 */
	protected boolean accept(E object) {
		if (!_initialized) {
			// Build set of elements-to-include from second argument
			_includeSet = Iterators.addAll(_arg2, new HashSet<E>());
			_initialized = true;
		}

		if (_includeSet.contains(object)) {
			// Element is part of the result

			if (_distinct) {
				// Prevent duplicates from being returned by
				// removing the element from the include set
				_includeSet.remove(object);
			}
			
			return true;
		}

		return false;
	}

	// overrides FilterIterator.close()
	public void close() {
		if (!isClosed()) {
			if (_initialized) {
				_includeSet.clear();
			}
			else {
				Iterators.closeCloseable(_arg2);
			}
		}

		super.close();
	}
}
