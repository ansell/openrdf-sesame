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
