/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.cursors;

import java.util.Arrays;
import java.util.Iterator;

import org.openrdf.StoreException;
import org.openrdf.query.Cursor;

/**
 * An Iteration that returns the bag union of the results of a number of
 * Iterations. 'Bag union' means that the UnionIteration does not filter
 * duplicate objects.
 *
 * @author James Leigh
 */
public class UnionCursor<E> implements Cursor<E> {
	/*-----------*
	 * Variables *
	 *-----------*/

	private Iterable<? extends Cursor<? extends E>> args;

	private Iterator<? extends Cursor<? extends E>> argIter;

	private Cursor<? extends E> currentIter;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new UnionIteration that returns the bag union of the results of
	 * a number of Iterations.
	 * 
	 * @param args
	 *        The Iterations containing the elements to iterate over.
	 */
	public UnionCursor(Cursor<? extends E>... args) {
		this(Arrays.asList(args));
	}

	/**
	 * Creates a new UnionIteration that returns the bag union of the results of
	 * a number of Iterations.
	 * 
	 * @param args
	 *        The Iterations containing the elements to iterate over.
	 */
	public UnionCursor(Iterable<? extends Cursor<? extends E>> args) {
		this.args = args;
		argIter = args.iterator();

		// Initialize with empty iteration so that var is not null
		currentIter = new EmptyCursor<E>();
	}

	/*--------------*
	 * Constructors *
	 *--------------*/

	public E next()
		throws StoreException
	{
		E next = currentIter.next();
		if (next == null) {
			// Current Iteration exhausted, continue with the next one
			currentIter.close();

			if (argIter.hasNext()) {
				currentIter = argIter.next();
				return next();
			}
		}
		return next;
	}

	public void close()
		throws StoreException
	{
		while (argIter.hasNext()) {
			argIter.next().close();
		}

		currentIter.close();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Union");
		for (Cursor<?> cursor : args) {
			String arg = cursor.toString().replace("\n", "\n\t");
			sb.append("\n\t").append(arg);
		}
		return sb.toString();
	}
}
