/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */

package info.aduna.iteration;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * An Iteration that can convert an {@link Iterator} to a
 * {@link CloseableIteration}.
 */
public class CloseableIteratorIteration<E, X extends Exception> extends CloseableIterationBase<E, X> {

	/*-----------*
	 * Variables *
	 *-----------*/

	private Iterator<? extends E> iter;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates an uninitialized CloseableIteratorIteration, needs to be
	 * initialized by calling {@link #setIterator(Iterator)} before it can be
	 * used.
	 */
	public CloseableIteratorIteration() {
	}

	/**
	 * Creates a CloseableIteratorIteration that wraps the supplied iterator.
	 */
	public CloseableIteratorIteration(Iterator<? extends E> iter) {
		setIterator(iter);
	}

	/*---------*
	 * Methods *
	 *---------*/

	protected void setIterator(Iterator<? extends E> iter) {
		this.iter = iter;
	}

	public boolean hasNext()
		throws X
	{
		return !isClosed() && iter.hasNext();
	}

	public E next()
		throws X
	{
		if (isClosed()) {
			throw new NoSuchElementException("Iteration has been closed");
		}

		return iter.next();
	}

	public void remove()
		throws X
	{
		if (isClosed()) {
			throw new IllegalStateException("Iteration has been closed");
		}

		iter.remove();
	}
}
