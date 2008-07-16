/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
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
class SortedRecordCache extends RecordCache {

	/*-----------*
	 * Constants *
	 *-----------*/

	protected final BTree btree;

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

	public void discard()
		throws IOException
	{
		btree.delete();
	}

	public void storeRecordInternal(byte[] record)
		throws IOException
	{
		btree.insert(record);
	}

	public RecordIterator getRecordsInternal() {
		return btree.iterateAll();
	}
}
