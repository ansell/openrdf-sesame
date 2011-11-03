/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */

package info.aduna.iteration;

import java.util.NoSuchElementException;

/**
 * An iterator that does not contain any elements.
 */
public class EmptyIteration<E, X extends Exception> extends CloseableIterationBase<E, X> {

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new EmptyIteration.
	 */
	public EmptyIteration() {
	}

	/*---------*
	 * Methods *
	 *---------*/

	public boolean hasNext() {
		return false;
	}

	public E next() {
		throw new NoSuchElementException();
	}

	public void remove() {
		throw new IllegalStateException("Empty iterator does not contain any elements");
	}
}
