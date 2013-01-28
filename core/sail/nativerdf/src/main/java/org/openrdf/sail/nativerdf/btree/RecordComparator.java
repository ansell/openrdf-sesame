/* 
 * Licensed to Aduna under one or more contributor license agreements.  
 * See the NOTICE.txt file distributed with this work for additional 
 * information regarding copyright ownership. 
 *
 * Aduna licenses this file to you under the terms of the Aduna BSD 
 * License (the "License"); you may not use this file except in compliance 
 * with the License. See the LICENSE.txt file distributed with this work 
 * for the full License.
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package org.openrdf.sail.nativerdf.btree;

/**
 * @author Arjohn Kampman
 */
public interface RecordComparator {

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
