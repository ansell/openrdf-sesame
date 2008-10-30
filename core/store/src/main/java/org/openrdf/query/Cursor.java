/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query;

import org.openrdf.store.StoreException;

/**
 * A Cursor is an Iterator-like object that can lazily iterate over a set of
 * resources. Cursors must be closed to free any resources that it is keeping
 * hold of. Any code using the cursor should be placed in a try-finally block,
 * closing the cursor in the finally, e.g.:
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
 *        The type of object that the cursor iterates over.
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
