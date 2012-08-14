/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */

package info.aduna.iteration;

import java.util.NoSuchElementException;

/**
 * A CloseableIteration that converts an arbitrary iteration to an iteration
 * with exceptions of type <tt>X</tt>. Subclasses need to override
 * {@link #convert(Exception)} to do the conversion.
 */
public abstract class ExceptionConvertingIteration<E, X extends Exception> extends
		CloseableIterationBase<E, X>
{

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * The underlying Iteration.
	 */
	private final Iteration<? extends E, ? extends Exception> iter;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new ExceptionConvertingIteration that operates on the supplied
	 * iteration.
	 * 
	 * @param iter
	 *        The Iteration that this <tt>ExceptionConvertingIteration</tt>
	 *        operates on, must not be <tt>null</tt>.
	 */
	public ExceptionConvertingIteration(Iteration<? extends E, ? extends Exception> iter) {
		assert iter != null;
		this.iter = iter;
	}

	/*---------*
	 * Methods *
	 *---------*/

	/**
	 * Converts an exception from the underlying iteration to an exception of
	 * type <tt>X</tt>.
	 */
	protected abstract X convert(Exception e);

	/**
	 * Checks whether the underlying Iteration contains more elements.
	 * 
	 * @return <tt>true</tt> if the underlying Iteration contains more
	 *         elements, <tt>false</tt> otherwise.
	 * @throws X
	 */
	public boolean hasNext()
		throws X
	{
		try {
			return iter.hasNext();
		}
		catch (Exception e) {
			throw convert(e);
		}
	}

	/**
	 * Returns the next element from the wrapped Iteration.
	 * 
	 * @throws X
	 * @throws java.util.NoSuchElementException
	 *         If all elements have been returned.
	 * @throws IllegalStateException
	 *         If the Iteration has been closed.
	 */
	public E next()
		throws X
	{
		try {
			return iter.next();
		}
		catch (NoSuchElementException e) {
			throw e;
		}
		catch (IllegalStateException e) {
			throw e;
		}
		catch (Exception e) {
			throw convert(e);
		}
	}

	/**
	 * Calls <tt>remove()</tt> on the underlying Iteration.
	 * 
	 * @throws UnsupportedOperationException
	 *         If the wrapped Iteration does not support the <tt>remove</tt>
	 *         operation.
	 * @throws IllegalStateException
	 *         If the Iteration has been closed, or if {@link #next} has not yet
	 *         been called, or {@link #remove} has already been called after the
	 *         last call to {@link #next}.
	 */
	public void remove()
		throws X
	{
		try {
			iter.remove();
		}
		catch (UnsupportedOperationException e) {
			throw e;
		}
		catch (IllegalStateException e) {
			throw e;
		}
		catch (Exception e) {
			throw convert(e);
		}
	}

	/**
	 * Closes this Iteration as well as the wrapped Iteration if it happens to be
	 * a {@link CloseableIteration}.
	 */
	@Override
	protected void handleClose()
		throws X
	{
		super.handleClose();

		try {
			Iterations.closeCloseable(iter);
		}
		catch (Exception e) {
			throw convert(e);
		}
	}
}
