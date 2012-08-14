/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */

package info.aduna.iteration;

import java.util.NoSuchElementException;

/**
 * An Iteration that limits the amount of elements that it returns from an
 * underlying Iteration to a fixed amount. This class returns the first
 * <tt>limit</tt> elements from the underlying Iteration and drops the rest.
 */
public class LimitIteration<E, X extends Exception> extends IterationWrapper<E, X> {

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * The amount of elements to return.
	 */
	private final long limit;

	/**
	 * The number of elements that have been returned so far.
	 */
	private long returnCount;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new LimitIteration.
	 * 
	 * @param iter
	 *        The underlying Iteration, must not be <tt>null</tt>.
	 * @param limit
	 *        The number of query answers to return, must be &gt;= 0.
	 */
	public LimitIteration(Iteration<? extends E, X> iter, long limit) {
		super(iter);

		assert iter != null;
		assert limit >= 0;

		this.limit = limit;
		this.returnCount = 0;
	}

	/*---------*
	 * Methods *
	 *---------*/

	@Override
	public boolean hasNext()
		throws X
	{
		return returnCount < limit && super.hasNext();
	}

	@Override
	public E next()
		throws X
	{
		if (returnCount >= limit) {
			throw new NoSuchElementException("limit reached");
		}

		returnCount++;
		return super.next();
	}
}
