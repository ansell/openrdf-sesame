/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.nativerdf.btree;

import java.io.IOException;

/**
 * An iterator that iterates over the values in a BTree.
 *
 * @see BTree
 */
public interface BTreeIterator {

	/**
	 * Returns the next value in the BTree.
	 *
	 * @return A value that is stored in the BTree, or <tt>null</tt> if all
	 * values have been returned.
	 * @exception IOException In case an I/O error occurred.
	 */
	public byte[] next()
		throws IOException;

	/**
	 * Closes the iterator, freeing any resources that it uses. Once closes, the
	 * iterator will not return any more values.
	 *
	 * @exception IOException In case an I/O error occurred.
	 */
	public void close()
		throws IOException;
}
