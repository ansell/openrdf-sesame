/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.cursors;

import java.util.Arrays;
import java.util.Iterator;

import org.openrdf.cursor.Cursor;
import org.openrdf.cursor.EmptyCursor;
import org.openrdf.store.StoreException;

/**
 * A cursor that returns the bag union of the results of a number of cursors.
 * 'Bag union' means that the UnionCursor does not filter duplicate objects.
 * 
 * @author Arjohn Kampman
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
	 * Creates a new UnionCursor that returns the bag union of the results of a
	 * number of cursors.
	 * 
	 * @param args
	 *        The cursors containing the elements to iterate over.
	 */
	public UnionCursor(Cursor<? extends E>... args) {
		this(Arrays.asList(args));
	}

	/**
	 * Creates a new UnionCursor that returns the bag union of the results of a
	 * number of cursors.
	 * 
	 * @param args
	 *        The cursors containing the elements to iterate over.
	 */
	public UnionCursor(Iterable<? extends Cursor<? extends E>> args) {
		this.args = args;
		argIter = args.iterator();

		// Initialize with empty cursor so that var is not null
		currentIter = EmptyCursor.getInstance();
	}

	/*--------------*
	 * Constructors *
	 *--------------*/

	public E next()
		throws StoreException
	{
		E next = currentIter.next();
		if (next == null) {
			// Current cursor exhausted, continue with the next one
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
