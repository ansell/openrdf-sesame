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
