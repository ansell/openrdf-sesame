/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.results;

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
	 * Returns the next element from this cursor.
	 * 
	 * @return the next element from this cursor, or <tt>null</tt> if the cursor
	 *         has no more elements.
	 */
	public E next()
		throws StoreException;

	/**
	 * Closes this cursor, freeing any resources that it is holding. If the
	 * cursor has already been closed then invoking this method has no effect.
	 * After closing a cursor, any subsequent calls to {@link #next()} will
	 * return <tt>null</tt>.
	 * <p>
	 * Note to implementors: this method is also used to abort long running
	 * evaluations. It should be implemented in such a way that it can be called
	 * concurrently with {@link #next()} and that it stops evaluation as soon as
	 * possible. Calls to {@link #next()} that are already in progress are
	 * allowed to still return results, but after returning from {@link #close()}
	 * , the cursor must not produce any more results.
	 */
	public void close()
		throws StoreException;

	/**
	 * Describes this cursor (recursively for any wrapped cursors).
	 */
	public String toString();
}
