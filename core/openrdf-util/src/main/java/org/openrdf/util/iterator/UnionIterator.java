/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.util.iterator;

import java.util.Arrays;
import java.util.Iterator;

/**
 * An iterator that returns the union of the results of a number of iterators.
 */
public class UnionIterator<E> extends LookAheadIterator<E> {

	/*-----------*
	 * Variables *
	 *-----------*/
	
	private Iterator<Iterator<? extends E>> _argIter;
	
	private Iterator<? extends E> _currentIter;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new UnionIterator that returns the intersection of the results
	 * of a number of iterators.
	 *
	 * @param args The iterators containing the elements to iterate over.
	 */
	public UnionIterator(Iterator<? extends E>... args) {
		this(Arrays.asList(args));
	}
	
	/**
	 * Creates a new UnionIterator that returns the intersection of the results
	 * of a number of iterators.
	 * 
	 * @param args The iterators containing the elements to iterate over.
	 */
	public UnionIterator(Iterable<Iterator<? extends E>> args) {
		_argIter = args.iterator();
	}

	/*--------------*
	 * Constructors *
	 *--------------*/
	
	// implements LookAheadIterator.getNextElement() 
	protected E getNextElement() {
		if (_currentIter != null && _currentIter.hasNext()) {
			return _currentIter.next();
		}
		else if (_argIter.hasNext()) {
			// Current iterator exhausted, continue with the next one
			if (_currentIter != null) {
				Iterators.closeCloseable(_currentIter);
			}
			
			_currentIter = _argIter.next();

			return getNextElement();
		}
		else {
			// All elements have been returned
			return null;
		}
	}

	// overrides LookAheadIterator.close()
	public void close() {
		if (!isClosed()) {
			if (_currentIter != null) {
				Iterators.closeCloseable(_currentIter);
			}
			Iterators.closeCloseable(_argIter);
		}
		
		super.close();
	}
}
