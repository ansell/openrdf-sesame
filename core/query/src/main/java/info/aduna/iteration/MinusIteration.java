/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */

package info.aduna.iteration;

import java.util.HashSet;
import java.util.Set;

/**
 * An Iteration that returns the results of an Iteration (the left argument)
 * minus the results of another Iteration (the right argument). Optionally, the
 * Iteration can be configured to filter duplicates from the returned elements.
 * <p>
 * Note that duplicates can also be filtered by wrapping this Iteration in a
 * {@link DistinctIteration}, but that has a bit more overhead as it adds a
 * second hash table lookup.
 */
public class MinusIteration<E, X extends Exception> extends FilterIteration<E, X> {

	/*-----------*
	 * Variables *
	 *-----------*/

	private final Iteration<? extends E, X> rightArg;

	private final boolean distinct;

	private boolean initialized;

	private Set<E> excludeSet;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new MinusIteration that returns the results of the left argument
	 * minus the results of the right argument. By default, duplicates are
	 * <em>not</em> filtered from the results.
	 * 
	 * @param leftArg
	 *        An Iteration containing the main set of elements.
	 * @param rightArg
	 *        An Iteration containing the set of elements that should be filtered
	 *        from the main set.
	 */
	public MinusIteration(Iteration<? extends E, X> leftArg, Iteration<? extends E, X> rightArg) {
		this(leftArg, rightArg, false);
	}

	/**
	 * Creates a new MinusIteration that returns the results of the left argument
	 * minus the results of the right argument.
	 * 
	 * @param leftArg
	 *        An Iteration containing the main set of elements.
	 * @param rightArg
	 *        An Iteration containing the set of elements that should be filtered
	 *        from the main set.
	 * @param distinct
	 *        Flag indicating whether duplicate elements should be filtered from
	 *        the result.
	 */
	public MinusIteration(Iteration<? extends E, X> leftArg, Iteration<? extends E, X> rightArg,
			boolean distinct)
	{
		super(leftArg);

		assert rightArg != null;

		this.rightArg = rightArg;
		this.distinct = distinct;
		this.initialized = false;
	}

	/*--------------*
	 * Constructors *
	 *--------------*/

	// implements LookAheadIteration.getNextElement()
	protected boolean accept(E object)
		throws X
	{
		if (!initialized) {
			// Build set of elements-to-exclude from right argument
			excludeSet = Iterations.addAll(rightArg, new HashSet<E>());
			initialized = true;
		}

		if (!excludeSet.contains(object)) {
			// Object is part of the result

			if (distinct) {
				// Prevent duplicates from being returned by
				// adding the object to the exclude set
				excludeSet.add(object);
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
		Iterations.closeCloseable(rightArg);
	}
}
