/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */

package info.aduna.iteration;

import java.util.Iterator;

/**
 * An Iteration that can convert an {@link Iterator} to a {@link Iteration}.
 */
public class IteratorIteration<E, X extends Exception> implements Iteration<E, X> {

	private final Iterator<? extends E> iter;

	public IteratorIteration(Iterator<? extends E> iter) {
		assert iter != null;
		this.iter = iter;
	}

	public boolean hasNext() {
		return iter.hasNext();
	}

	public E next() {
		return iter.next();
	}

	public void remove() {
		iter.remove();
	}
}
