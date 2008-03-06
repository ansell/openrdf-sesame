/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.util.iterator;

import java.util.Iterator;
import java.util.NoSuchElementException;


/**
 * A CloseableIterator that wraps another iterator, applying a filter on the
 * objects that are returned. Subclasses must implement the <tt>accept</tt>
 * method to indicate which objects should be returned.
 */
public abstract class FilterIterator<E> extends IteratorWrapper<E> {

	/*-----------*
	 * Variables *
	 *-----------*/

	private E _nextElement;
	
	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * @param iter
	 */
	public FilterIterator(Iterator<? extends E> iter) {
		super(iter);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public boolean hasNext() {
		_findNextElement();
		
		return _nextElement != null;
	}
	
	public E next() {
		_findNextElement();

		if (_nextElement != null) {
			E result = _nextElement;
			_nextElement = null;
			return result;
		}
		else {
			throw new NoSuchElementException();
		}
	}
	
	private void _findNextElement() {
		while (_nextElement == null && super.hasNext()) {
			E candidate = super.next();
			
			if (accept(candidate)) {
				_nextElement = candidate;
			}
		}		
	}

	/**
	 * Tests whether or not the specified object should be returned by this
	 * iterator. All objects from the wrapped iterator pass through this method
	 * in the same order as they are coming from the wrapped iterator.
	 * 
	 * @param object The object to be tested.
	 * @return <tt>true</tt> if the object should be returned, <tt>false</tt>
	 * otherwise.
	 */
	protected abstract boolean accept(E object);
	
	// overrides IteratorWrapper.close()
	public void close() {
		_nextElement = null;
		super.close();
	}
}
