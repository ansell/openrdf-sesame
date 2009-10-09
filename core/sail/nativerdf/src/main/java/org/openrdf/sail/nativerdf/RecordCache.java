/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.nativerdf;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;

import org.openrdf.sail.nativerdf.btree.RecordIterator;

/**
 * A cache for fixed size byte array records. This cache uses a temporary file
 * to store the records. This file is deleted upon calling {@link #discard()}.
 * 
 * @author Arjohn Kampman
 */
abstract class RecordCache {

	/*-----------*
	 * Variables *
	 *-----------*/

	private final long maxRecords;

	private final AtomicLong recordCount;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public RecordCache()
		throws IOException
	{
		this(Long.MAX_VALUE);
	}

	public RecordCache(long maxRecords)
		throws IOException
	{
		this.maxRecords = maxRecords;
		this.recordCount = new AtomicLong();
	}

	/*---------*
	 * Methods *
	 *---------*/

	public long getMaxRecords() {
		return maxRecords;
	}

	/**
	 * Gets the number of records currently stored in the cache, throwing an
	 * {@link IllegalStateException} if the cache is no longer {@link #isValid()
	 * valid}.
	 * 
	 * @return
	 * @throws IllegalStateException
	 *         If the cache is not/no longer {@link #isValid() valid}.
	 */
	public final long getRecordCount() {
		if (isValid()) {
			return recordCount.get();
		}

		throw new IllegalStateException();
	}

	/**
	 * Stores a record in the cache.
	 * 
	 * @param data
	 *        The record to store.
	 */
	public final void storeRecord(byte[] data)
		throws IOException
	{
		if (isValid()) {
			storeRecordInternal(data);
			recordCount.incrementAndGet();
		}
	}

	/**
	 * Stores the records from the supplied cache into this cache.
	 * 
	 * @param otherCache
	 *        The cache to copy the records from.
	 */
	public final void storeRecords(RecordCache otherCache)
		throws IOException
	{
		if (isValid()) {
			RecordIterator recIter = otherCache.getRecords();
			try {
				byte[] record;

				while ((record = recIter.next()) != null) {
					storeRecordInternal(record);
				}
			}
			finally {
				recIter.close();
			}
		}
	}

	protected abstract void storeRecordInternal(byte[] data)
		throws IOException;

	/**
	 * Gets all records that are stored in the cache, throwing an
	 * {@link IllegalStateException} if the cache is no longer {@link #isValid()
	 * valid}.
	 * 
	 * @return An iterator over all records.
	 * @throws IllegalStateException
	 *         If the cache is not/no longer {@link #isValid() valid}.
	 */
	public final RecordIterator getRecords() {
		if (isValid()) {
			return getRecordsInternal();
		}

		throw new IllegalStateException();
	}

	protected abstract RecordIterator getRecordsInternal();

	/**
	 * Checks whether the cache is still valid. Caches are valid if the number of
	 * stored records is smaller than the {@link #getMaxRecords() maximum number
	 * of records}.
	 */
	public final boolean isValid() {
		return recordCount.get() < maxRecords;
	}

	/**
	 * Discards the cache, deleting any allocated files.
	 */
	public abstract void discard()
		throws IOException;
}
