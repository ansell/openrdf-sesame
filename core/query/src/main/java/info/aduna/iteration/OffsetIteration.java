/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */

package info.aduna.iteration;

/**
 * An Iteration that skips the first <tt>offset</tt> elements from an
 * underlying Iteration.
 */
public class OffsetIteration<E, X extends Exception> extends FilterIteration<E, X> {

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * The offset (0-based) of the first element to return.
	 */
	private final long offset;

	/**
	 * The number of elements that have been dropped so far.
	 */
	private long droppedResults;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new OffsetIteration.
	 * 
	 * @param iter
	 *        The underlying Iteration, must not be <tt>null</tt>.
	 * @param offset
	 *        The number of elements to skip, must be larger than or equal to 0.
	 */
	public OffsetIteration(Iteration<? extends E, X> iter, long offset) {
		super(iter);

		assert offset >= 0;

		this.offset = offset;
		this.droppedResults = 0;
	}

	/*---------*
	 * Methods *
	 *---------*/

	/**
	 * Returns <tt>false</tt> for the first OFFSET objects.
	 */
	protected boolean accept(E object) {
		if (droppedResults < offset) {
			droppedResults++;
			return false;
		}
		else {
			return true;
		}
	}
}
