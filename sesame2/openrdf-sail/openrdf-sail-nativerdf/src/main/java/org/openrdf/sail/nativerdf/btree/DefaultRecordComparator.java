/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.nativerdf.btree;

/**
 * A RecordComparator that compares values with eachother by comparing all
 * of their bytes.
 * 
 * @author Arjohn Kampman
 */
public class DefaultRecordComparator implements RecordComparator {

	// implements RecordComparator.compareBTreeValues()
	public int compareBTreeValues(byte[] key, byte[] data, int offset, int length) {
		int result = 0;
		for (int i = 0; result == 0 && i < length; i++) {
			result = (key[i] & 0xff) - (data[offset + i] & 0xff);
		}
		return result;
	}
}
