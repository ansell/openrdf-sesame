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
 * An iterator that returns the results of an iterator (the left argument) minus
 * the results of another iterator (the right argument). Optionally, the
 * iterator can be configured to filter duplicates from the returned elements.
 * <p>
 * Note that duplicates can also be filtered by wrapping this iterator in a
 * {@link DistinctIterator}, but that has a bit more overhead as it adds a
 * second hash table lookup.  
 */
public class MinusIterator<E> extends FilterIterator<E> {

	/*-----------*
	 * Variables *
	 *-----------*/

	private Iterator<? extends E> _rightArg;
	private boolean _distinct;
	private boolean _initialized;

	private Set<E> _excludeSet;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new MinusIterator that returns the results of the left argument
	 * minus the results of the right argument. By default, duplicates are
	 * <em>not</em> filtered from the results.
	 * 
	 * @param leftArg An iterator containing the main set of elements.
	 * @param rightArg An iterator containing the set of elements that should be
	 * filtered from the main set.
	 */
	public MinusIterator(Iterator<? extends E> leftArg, Iterator<? extends E> rightArg) {
		this(leftArg, rightArg, false);
	}
	
	/**
	 * Creates a new MinusIterator that returns the results of the left argument
	 * minus the results of the right argument.
	 * 
	 * @param leftArg An iterator containing the main set of elements.
	 * @param rightArg An iterator containing the set of elements that should be
	 * filtered from the main set.
	 * @param distinct Flag indicating whether duplicate elements should be
	 * filtered from the result.
	 */
	public MinusIterator(
		Iterator<? extends E> leftArg, Iterator<? extends E> rightArg, boolean distinct)
	{
		super(leftArg);
		
		assert rightArg != null;
		
		_rightArg = rightArg;
		_distinct = distinct;
		_initialized = false;
	}

	/*--------------*
	 * Constructors *
	 *--------------*/

	// implements LookAheadIterator.getNextElement() 
	protected boolean accept(E object) {
		if (!_initialized) {
			// Build set of elements-to-exclude from right argument
			_excludeSet = Iterators.addAll(_rightArg, new HashSet<E>());			
			_initialized = true;
		}

		if (!_excludeSet.contains(object)) {
			// Object is part of the result

			if (_distinct) {
				// Prevent duplicates from being returned by
				// adding the object to the exclude set
				_excludeSet.add(object);
			}
				
			return true;
		}

		return false;
	}
	
	// overrides LookAheadIterator.close()
	public void close() {
		if (!isClosed()) {
			if (_initialized) {
				_excludeSet.clear();
			}
			else {
				Iterators.closeCloseable(_rightArg);
			}
		}
			
		super.close();
	}
}
