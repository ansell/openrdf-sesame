/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.nativerdf.btree;

/**
 * @author Arjohn Kampman
 */
public interface BTreeValueComparator {

	/**
	 * Compares the supplied <tt>key</tt> to the value of length
	 * <tt>length</tt>, starting at offset <tt>offset</tt> in the supplied
	 * <tt>data</tt> array.
	 * 
	 * @param key
	 *        A byte array representing the search key.
	 * @param data
	 *        A byte array containing the value to compare the key to.
	 * @param offset
	 *        The offset (0-based) of the value in <tt>data</tt>.
	 * @param length
	 *        The length of the value.
	 * @return A negative integer when the key is smaller than the value, a
	 *         positive integer when the key is larger than the value, or
	 *         <tt>0</tt> when the key is equal to the value.
	 */
	public int compareBTreeValues(byte[] key, byte[] data, int offset, int length);
}
