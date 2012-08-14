/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */

package info.aduna.iteration;

import java.util.HashSet;
import java.util.Set;

/**
 * An Iteration that returns the intersection of the results of two Iterations.
 * Optionally, the Iteration can be configured to filter duplicates from the
 * returned elements.
 * <p>
 * Note that duplicates can also be filtered by wrapping this Iteration in a
 * {@link DistinctIteration}, but that has a bit more overhead as it adds a
 * second hash table lookup.
 */
public class IntersectIteration<E, X extends Exception> extends FilterIteration<E, X> {

	/*-----------*
	 * Variables *
	 *-----------*/

	private final Iteration<? extends E, ? extends X> arg2;

	private final boolean distinct;

	private boolean initialized;

	private Set<E> includeSet;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new IntersectIteration that returns the intersection of the
	 * results of two Iterations. By default, duplicates are <em>not</em>
	 * filtered from the results.
	 * 
	 * @param arg1
	 *        An Iteration containing the first set of elements.
	 * @param arg2
	 *        An Iteration containing the second set of elements.
	 */
	public IntersectIteration(Iteration<? extends E, ? extends X> arg1,
			Iteration<? extends E, ? extends X> arg2)
	{
		this(arg1, arg2, false);
	}

	/**
	 * Creates a new IntersectIteration that returns the intersection of the
	 * results of two Iterations.
	 * 
	 * @param arg1
	 *        An Iteration containing the first set of elements.
	 * @param arg2
	 *        An Iteration containing the second set of elements.
	 * @param distinct
	 *        Flag indicating whether duplicate elements should be filtered from
	 *        the result.
	 */
	public IntersectIteration(Iteration<? extends E, ? extends X> arg1,
			Iteration<? extends E, ? extends X> arg2, boolean distinct)
	{
		super(arg1);

		assert arg2 != null;

		this.arg2 = arg2;
		this.distinct = distinct;
		this.initialized = false;
	}

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Returns <tt>true</tt> if the object is in the set of elements of the
	 * second argument.
	 */
	protected boolean accept(E object)
		throws X
	{
		if (!initialized) {
			// Build set of elements-to-include from second argument
			includeSet = Iterations.addAll(arg2, new HashSet<E>());
			initialized = true;
		}

		if (includeSet.contains(object)) {
			// Element is part of the result

			if (distinct) {
				// Prevent duplicates from being returned by
				// removing the element from the include set
				includeSet.remove(object);
			}

			return true;
		}

		return false;
	}

	@Override
	protected void handleClose()
		throws X
	{
		super.handleClose();
		Iterations.closeCloseable(arg2);
	}
}
