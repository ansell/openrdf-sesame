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
package org.openrdf.sail.nativerdf;

import java.io.File;
import java.io.IOException;

import org.openrdf.sail.nativerdf.btree.BTree;
import org.openrdf.sail.nativerdf.btree.RecordComparator;
import org.openrdf.sail.nativerdf.btree.RecordIterator;

/**
 * A cache for fixed size byte array records. This cache uses a temporary file
 * to store the records. This file is deleted upon calling {@link #discard()}.
 * 
 * @author Arjohn Kampman
 */
final class SortedRecordCache extends RecordCache {

	/*------------*
	 * Attributes *
	 *------------*/

	private final BTree btree;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public SortedRecordCache(File cacheDir, int recordSize, RecordComparator comparator)
		throws IOException
	{
		this(cacheDir, recordSize, Long.MAX_VALUE, comparator);
	}

	public SortedRecordCache(File cacheDir, int recordSize, long maxRecords, RecordComparator comparator)
		throws IOException
	{
		super(maxRecords);
		btree = new BTree(cacheDir, "txncache", 4096, recordSize, comparator);
	}

	/*---------*
	 * Methods *
	 *---------*/

	@Override
	protected void storeRecordInternal(byte[] record)
		throws IOException
	{
		btree.insert(record);
	}

	@Override
	protected RecordIterator getRecordsInternal() {
		return btree.iterateAll();
	}

	@Override
	protected void clearInternal()
		throws IOException
	{
		btree.clear();
	}

	@Override
	public void discard()
		throws IOException
	{
		btree.delete();
	}
}
