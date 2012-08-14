/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */

package info.aduna.iteration;

import java.util.NoSuchElementException;

/**
 * An Iteration that looks one element ahead, if necessary, to handle calls to
 * {@link #hasNext}. This is a convenient super class for Iterations that have
 * no easy way to tell if there are any more results, but still should implement
 * the <tt>java.util.Iteration</tt> interface.
 */
public abstract class LookAheadIteration<E, X extends Exception> extends CloseableIterationBase<E, X> {

	/*-----------*
	 * Variables *
	 *-----------*/

	private E nextElement;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public LookAheadIteration() {
	}

	/*---------*
	 * Methods *
	 *---------*/

	/**
	 * Gets the next element. Subclasses should implement this method so that it
	 * returns the next element.
	 * 
	 * @return The next element, or <tt>null</tt> if no more elements are
	 *         available.
	 */
	protected abstract E getNextElement()
		throws X;

	public final boolean hasNext()
		throws X
	{
		lookAhead();

		return nextElement != null;
	}

	public final E next()
		throws X
	{
		lookAhead();

		E result = nextElement;

		if (result != null) {
			nextElement = null;
			return result;
		}
		else {
			throw new NoSuchElementException();
		}
	}

	/**
	 * Fetches the next element if it hasn't been fetched yet and stores it in
	 * {@link #nextElement}.
	 * 
	 * @throws X
	 */
	private void lookAhead()
		throws X
	{
		if (nextElement == null && !isClosed()) {
			nextElement = getNextElement();

			if (nextElement == null) {
				close();
			}
		}
	}

	/**
	 * Throws an {@link UnsupportedOperationException}.
	 */
	public void remove() {
		throw new UnsupportedOperationException();
	}

	@Override
	protected void handleClose()
		throws X
	{
		super.handleClose();
		nextElement = null;
	}
}
