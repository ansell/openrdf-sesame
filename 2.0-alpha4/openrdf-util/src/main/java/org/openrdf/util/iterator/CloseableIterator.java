/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.util.iterator;

import java.io.Closeable;
import java.util.Iterator;

/**
 * An iterator that can be closed to free resources that it is holding.
 * CloseableIterators are automatically closed when exhausted. If not read until
 * exhaustion or if you want to make sure the iterator is properly closed, any
 * code using the iterator should be placed in a try-finally block, closing the
 * iterator in the finally, e.g.:
 * 
 * <pre>
 * CloseableIterator&lt;Object&gt; iter = ...
 * try {
 *    // read objects from the iterator
 * }
 * finally {
 *    iter.close();
 * }
 * </pre>
 */
public interface CloseableIterator<E> extends Iterator<E>, Closeable {

	/**
	 * Closes this iterator, freeing any resources that it is holding. If the
	 * iterator is already closed then invoking this method has no effect.
	 */
	public void close();
}
