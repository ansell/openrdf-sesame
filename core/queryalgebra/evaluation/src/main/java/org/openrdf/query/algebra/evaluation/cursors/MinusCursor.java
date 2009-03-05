/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.cursors;

import java.util.HashSet;
import java.util.Set;

import org.openrdf.cursor.Cursor;
import org.openrdf.cursor.FilteringCursor;
import org.openrdf.store.StoreException;

/**
 * A cursor that returns the results of some cursor (the left argument) minus
 * the results of another cursor (the right argument). Optionally, the cursor
 * can be configured to filter duplicates from the returned elements.
 * <p>
 * Note that duplicates can also be filtered by wrapping this cursor in a
 * {@link DistinctCursor}, but that has a bit more overhead as it adds a second
 * hash table lookup.
 * 
 * @author Arjohn Kampman
 * @author James Leigh
 */
public class MinusCursor<E> extends FilteringCursor<E> {

	/*-----------*
	 * Variables *
	 *-----------*/

	private Cursor<? extends E> rightArg;

	private boolean distinct;

	private boolean initialized;

	private Set<E> excludeSet;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new MinusCursor that returns the results of the left argument
	 * minus the results of the right argument. By default, duplicates are
	 * <em>not</em> filtered from the results.
	 * 
	 * @param leftArg
	 *        A cursor containing the main set of elements.
	 * @param rightArg
	 *        A cursor containing the set of elements that should be filtered
	 *        from the main set.
	 */
	public MinusCursor(Cursor<? extends E> leftArg, Cursor<? extends E> rightArg) {
		this(leftArg, rightArg, false);
	}

	/**
	 * Creates a new MinusCursor that returns the results of the left argument
	 * minus the results of the right argument.
	 * 
	 * @param leftArg
	 *        A cursor containing the main set of elements.
	 * @param rightArg
	 *        A cursor containing the set of elements that should be filtered
	 *        from the main set.
	 * @param distinct
	 *        Flag indicating whether duplicate elements should be filtered from
	 *        the result.
	 */
	public MinusCursor(Cursor<? extends E> leftArg, Cursor<? extends E> rightArg, boolean distinct) {
		super(leftArg);

		assert rightArg != null;

		this.rightArg = rightArg;
		this.distinct = distinct;
		this.initialized = false;
	}

	/*--------------*
	 * Constructors *
	 *--------------*/

	protected boolean accept(E object)
		throws StoreException
	{
		if (!initialized) {
			// Build set of elements-to-exclude from right argument
			excludeSet = new HashSet<E>();
			E next;
			while ((next = rightArg.next()) != null) {
				excludeSet.add(next);
			}
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
	public void close()
		throws StoreException
	{
		super.close();
		rightArg.close();
	}

	@Override
	public String getName() {
		return "Minus\n" + rightArg.toString();
	}

}
