/*******************************************************************************
 * Copyright (c) 2015, Eclipse Foundation, Inc. and its licensors.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * - Neither the name of the Eclipse Foundation, Inc. nor the names of its
 *   contributors may be used to endorse or promote products derived from this
 *   software without specific prior written permission. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
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
