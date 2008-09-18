/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query;

import org.openrdf.StoreException;

/**
 * An Cursor is an Iterator-like object that is closeable and can throw
 * StoreExceptions while iterating. This is used in cases where the iteration is
 * lazy and evaluates over a (remote) connection, for example accessing a
 * database. In such cases an error can occur at any time and needs to be
 * communicated through a checked exception, something
 * {@link java.util.Iterator} can not do (it can only throw
 * {@link RuntimeException)s.
 * <p>An {@link Cursor} must be closed to free resources that it is holding. Any
 * code using the cursor should be placed in a try-catch-finally block, closing
 * the cursor in the finally, e.g.:
 * 
 * <pre>
 * Cursor&lt;Object&gt; cursor = ...
 * try {
 *    Object next;
 *    while ((next = cursor.next()) != null) {
 *    	// read objects from the cursor
 *    }
 * }
 * catch(StoreException e) {
 *   // process the exception that can be thrown while processing.
 * }
 * finally {
 *    cursor.close();
 * }
 * </pre>
 * 
 * @param <E>
 *        Object type of objects contained in the cursor.
 * @author James Leigh
 */
public interface Cursor<E> {

	/**
	 * Returns the next element in the iteration if the cursor has more elements.
	 * 
	 * @return the next element in the iteration if the cursor has more elements,
	 *         null otherwise.
	 */
	public E next()
		throws StoreException;

	/**
	 * Closes this cursor, freeing any resources that it is holding. If the
	 * cursor has already been closed then invoking this method has no effect.
	 */
	public void close()
		throws StoreException;

	/**
	 * Describes this and any wrapped cursors.
	 */
	public String toString();
}
