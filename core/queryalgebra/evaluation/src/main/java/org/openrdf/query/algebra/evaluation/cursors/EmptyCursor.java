/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.cursors;

import org.openrdf.query.Cursor;

/**
 * @author James Leigh
 */
public class EmptyCursor<E> implements Cursor<E> {

	private static EmptyCursor<?> emptyCursor = new EmptyCursor<Object>();

	@SuppressWarnings("unchecked")
	public static <E> Cursor<E> emptyCursor() {
		return (Cursor<E>) emptyCursor;
	}

	public void close() {
		// no-op
	}

	public E next() {
		return null;
	}

	@Override
	public String toString() {
		return "Empty";
	}

}
