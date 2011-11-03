/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */

package info.aduna.iteration;

import java.util.NoSuchElementException;

/**
 * A CloseableIteration that wraps another Iteration, applying a filter on the
 * objects that are returned. Subclasses must implement the <tt>accept</tt>
 * method to indicate which objects should be returned.
 */
public abstract class FilterIteration<E, X extends Exception> extends IterationWrapper<E, X> {

	/*-----------*
	 * Variables *
	 *-----------*/

	private E nextElement;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * @param iter
	 */
	public FilterIteration(Iteration<? extends E, ? extends X> iter) {
		super(iter);
	}

	/*---------*
	 * Methods *
	 *---------*/

	@Override
	public boolean hasNext()
		throws X
	{
		findNextElement();

		return nextElement != null;
	}

	@Override
	public E next()
		throws X
	{
		findNextElement();

		E result = nextElement;

		if (result != null) {
			nextElement = null;
			return result;
		}
		else {
			throw new NoSuchElementException();
		}
	}

	private void findNextElement()
		throws X
	{
		while (nextElement == null && super.hasNext()) {
			E candidate = super.next();

			if (accept(candidate)) {
				nextElement = candidate;
			}
		}
	}

	/**
	 * Tests whether or not the specified object should be returned by this
	 * Iteration. All objects from the wrapped Iteration pass through this method
	 * in the same order as they are coming from the wrapped Iteration.
	 * 
	 * @param object
	 *        The object to be tested.
	 * @return <tt>true</tt> if the object should be returned, <tt>false</tt>
	 *         otherwise.
	 * @throws X
	 */
	protected abstract boolean accept(E object)
		throws X;

	@Override
	protected void handleClose()
		throws X
	{
		super.handleClose();
		nextElement = null;
	}
}
