/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.nativerdf.btree;

import java.io.IOException;

/**
 * An iterator that iterates over records, for example those in a BTree.
 * 
 * @see BTree
 * @author Arjohn Kampman
 */
public interface RecordIterator {

	/**
	 * Returns the next record in the BTree.
	 * 
	 * @return A record that is stored in the BTree, or <tt>null</tt> if all
	 *         records have been returned.
	 * @exception IOException
	 *            In case an I/O error occurred.
	 */
	public byte[] next()
		throws IOException;

	/**
	 * Replaces the last record returned by {@link #next} with the specified
	 * record.
	 * 
	 * @exception IOException
	 *            In case an I/O error occurred.
	 */
	public void set(byte[] record)
		throws IOException;

	/**
	 * Closes the iterator, freeing any resources that it uses. Once closed, the
	 * iterator will not return any more records.
	 * 
	 * @exception IOException
	 *            In case an I/O error occurred.
	 */
	public void close()
		throws IOException;
}
