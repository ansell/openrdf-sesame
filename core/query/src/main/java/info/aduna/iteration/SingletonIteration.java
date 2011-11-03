/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */

package info.aduna.iteration;

import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicReference;

/**
 * An Iteration that contains exactly one element.
 */
public class SingletonIteration<E, X extends Exception> extends CloseableIterationBase<E, X> {

	/*-----------*
	 * Variables *
	 *-----------*/

	private final AtomicReference<E> value;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new EmptyIteration.
	 */
	public SingletonIteration(E value) {
		this.value = new AtomicReference<E>(value);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public boolean hasNext() {
		return value.get() != null;
	}

	public E next() {
		E result = value.getAndSet(null);
		if (result == null) {
			throw new NoSuchElementException();
		}
		return result;
	}

	public void remove() {
		throw new UnsupportedOperationException();
	}

	@Override
	protected void handleClose()
		throws X
	{
		super.handleClose();
		value.set(null);
	}
}
