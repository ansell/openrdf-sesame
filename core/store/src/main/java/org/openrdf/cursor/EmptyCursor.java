/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.cursor;


/**
 * @author James Leigh
 */
public class EmptyCursor<E> implements Cursor<E> {

	private static final EmptyCursor<?> singleton = new EmptyCursor<Object>();

	@SuppressWarnings("unchecked")
	public static <E> Cursor<E> getInstance() {
		return (Cursor<E>)singleton;
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
