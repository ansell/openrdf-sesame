/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.nativerdf.datastore;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;

/**
 * Class supplying access to a hash file.
 * 
 * @author Arjohn Kampman
 */
public class HashFile {

	/*-----------*
	 * Constants *
	 *-----------*/

	// The size of an item (32-bit hash + 32-bit ID), in bytes
	private static final int ITEM_SIZE = 8;

	/**
	 * Magic number "Native Hash File" to detect whether the file is actually a
	 * hash file. The first three bytes of the file should be equal to this magic
	 * number.
	 */
	private static final byte[] MAGIC_NUMBER = new byte[] { 'n', 'h', 'f' };

	/**
	 * File format version, stored as the fourth byte in hash files.
	 */
	private static final byte FILE_FORMAT_VERSION = 1;

	/**
	 * The size of the file header in bytes. The file header contains the
	 * following data: magic number (3 bytes) file format version (1 byte),
	 * number of buckets (4 bytes), bucket size (4 bytes) and number of stored
	 * items (4 bytes).
	 */
	private static final long HEADER_LENGTH = 16;

	private static final int INIT_BUCKET_COUNT = 64;

	private static final int INIT_BUCKET_SIZE = 8;

	/*-----------*
	 * Variables *
	 *-----------*/

	private File file;

	private RandomAccessFile raf;

	private FileChannel fileChannel;

	private boolean forceSync;

	// The number of (non-overflow) buckets in the hash file
	private int bucketCount;

	// The number of items that can be stored in a bucket
	private int bucketSize;

	// The number of items in the hash file
	private int itemCount;

	// Load factor (fixed, for now)
	private float loadFactor = 0.75f;

	// _recordSize = ITEM_SIZE * _bucketSize + 4
	private int recordSize;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public HashFile(File file)
		throws IOException
	{
		this(file, false);
	}

	public HashFile(File file, boolean forceSync)
		throws IOException
	{
		this.file = file;
		this.forceSync = forceSync;

		if (!file.exists()) {
			boolean created = file.createNewFile();
			if (!created) {
				throw new IOException("Failed to create file: " + file);
			}
		}

		// Open a read/write channel to the file
		raf = new RandomAccessFile(file, "rw");
		fileChannel = raf.getChannel();

		if (fileChannel.size() == 0L) {
			// Empty file, insert bucket count, bucket size
			// and item count at the start of the file
			bucketCount = INIT_BUCKET_COUNT;
			bucketSize = INIT_BUCKET_SIZE;
			itemCount = 0;
			recordSize = ITEM_SIZE * bucketSize + 4;

			// Initialize the file by writing <_bucketCount> empty buckets
			writeEmptyBuckets(HEADER_LENGTH, bucketCount);

			sync();
		}
		else {
			// Read bucket count, bucket size and item count from the file
			readFileHeader();

			recordSize = ITEM_SIZE * bucketSize + 4;
		}
	}

	/*---------*
	 * Methods *
	 *---------*/

	public File getFile() {
		return file;
	}

	public FileChannel getFileChannel() {
		return fileChannel;
	}

	public int getBucketCount() {
		return bucketCount;
	}

	public int getBucketSize() {
		return bucketSize;
	}

	public int getItemCount() {
		return itemCount;
	}

	public int getRecordSize() {
		return recordSize;
	}

	/**
	 * Gets an iterator that iterates over the IDs with hash codes that match the
	 * specified hash code.
	 */
	public IDIterator getIDIterator(int hash)
		throws IOException
	{
		return new IDIterator(hash);
	}

	/**
	 * Stores ID under the specified hash code in this hash file.
	 */
	public void storeID(int hash, int id)
		throws IOException
	{
		// Calculate bucket offset for initial bucket
		long bucketOffset = getBucketOffset(hash);

		_storeID(bucketOffset, hash, id);

		itemCount++;

		if (itemCount >= loadFactor * bucketCount * bucketSize) {
			increaseHashTable();
		}
	}

	private void _storeID(long bucketOffset, int hash, int id)
		throws IOException
	{
		boolean idStored = false;
		ByteBuffer bucket = ByteBuffer.allocate(recordSize);

		while (!idStored) {
			fileChannel.read(bucket, bucketOffset);

			// Find first empty slot in bucket
			int slotID = findEmptySlotInBucket(bucket);

			if (slotID >= 0) {
				// Empty slot found, store dataOffset in it
				bucket.putInt(ITEM_SIZE * slotID, hash);
				bucket.putInt(ITEM_SIZE * slotID + 4, id);
				bucket.rewind();
				fileChannel.write(bucket, bucketOffset);
				idStored = true;
			}
			else {
				// No empty slot found, check if bucket has an overflow bucket
				int overflowID = bucket.getInt(ITEM_SIZE * bucketSize);

				if (overflowID == 0) {
					// No overflow bucket yet, create one
					overflowID = createOverflowBucket();

					// Link overflow bucket to current bucket
					bucket.putInt(ITEM_SIZE * bucketSize, overflowID);
					bucket.rewind();
					fileChannel.write(bucket, bucketOffset);
				}

				// Continue searching for an empty slot in the overflow bucket
				bucketOffset = getOverflowBucketOffset(overflowID);
				bucket.clear();
			}
		}
	}

	public void clear()
		throws IOException
	{
		// Truncate the file to remove any overflow buffers
		fileChannel.truncate(HEADER_LENGTH + (long)bucketCount * recordSize);

		// Overwrite normal buckets with empty ones
		writeEmptyBuckets(HEADER_LENGTH, bucketCount);

		itemCount = 0;
	}

	/**
	 * Syncs any unstored data to the hash file.
	 */
	public void sync()
		throws IOException
	{
		// Update the file header
		writeFileHeader();

		if (forceSync) {
			fileChannel.force(false);
		}
	}

	public void close()
		throws IOException
	{
		raf.close();
		raf = null;
		fileChannel = null;
	}

	/*-----------------*
	 * Utility methods *
	 *-----------------*/

	private RandomAccessFile createEmptyFile(File file)
		throws IOException
	{
		// Make sure the file exists
		if (!file.exists()) {
			boolean created = file.createNewFile();
			if (!created) {
				throw new IOException("Failed to create file " + file);
			}
		}

		// Open the file in read-write mode and make sure the file is empty
		RandomAccessFile raf = new RandomAccessFile(file, "rw");
		raf.setLength(0L);

		return raf;
	}

	/**
	 * Writes the bucket count, bucket size and item count to the file header.
	 */
	private void writeFileHeader()
		throws IOException
	{
		ByteBuffer buf = ByteBuffer.allocate((int)HEADER_LENGTH);
		buf.put(MAGIC_NUMBER);
		buf.put(FILE_FORMAT_VERSION);
		buf.putInt(bucketCount);
		buf.putInt(bucketSize);
		buf.putInt(itemCount);
		buf.rewind();

		fileChannel.write(buf, 0L);
	}

	/**
	 * Reads the bucket count, bucket size and item count from the file header.
	 */
	private void readFileHeader()
		throws IOException
	{
		ByteBuffer buf = ByteBuffer.allocate((int)HEADER_LENGTH);
		fileChannel.read(buf, 0L);
		buf.rewind();

		if (buf.remaining() < HEADER_LENGTH) {
			throw new IOException("File too short to be a compatible hash file");
		}

		byte[] magicNumber = new byte[MAGIC_NUMBER.length];
		buf.get(magicNumber);
		byte version = buf.get();
		bucketCount = buf.getInt();
		bucketSize = buf.getInt();
		itemCount = buf.getInt();

		if (!Arrays.equals(MAGIC_NUMBER, magicNumber)) {
			throw new IOException("File doesn't contain compatible hash file data");
		}

		if (version > FILE_FORMAT_VERSION) {
			throw new IOException("Unable to read hash file; it uses a newer file format");
		}
		else if (version != FILE_FORMAT_VERSION) {
			throw new IOException("Unable to read hash file; invalid file format version: " + version);
		}
	}

	/**
	 * Returns the offset of the bucket for the specified hash code.
	 */
	private long getBucketOffset(int hash) {
		int bucketNo = hash % bucketCount;
		if (bucketNo < 0) {
			bucketNo += bucketCount;
		}
		return HEADER_LENGTH + (long)bucketNo * recordSize;
	}

	/**
	 * Returns the offset of the overflow bucket with the specified ID.
	 */
	private long getOverflowBucketOffset(int bucketID) {
		return HEADER_LENGTH + ((long)bucketCount + (long)bucketID - 1L) * recordSize;
	}

	/**
	 * Creates a new overflow bucket and returns its ID.
	 */
	private int createOverflowBucket()
		throws IOException
	{
		long offset = fileChannel.size();
		writeEmptyBuckets(offset, 1);
		return (int)((offset - HEADER_LENGTH) / recordSize) - bucketCount + 1;
	}

	private void writeEmptyBuckets(long fileOffset, int bucketCount)
		throws IOException
	{
		ByteBuffer emptyBucket = ByteBuffer.allocate(recordSize);

		for (int i = 0; i < bucketCount; i++) {
			fileChannel.write(emptyBucket, fileOffset + i * (long)recordSize);
			emptyBucket.rewind();
		}
	}

	private int findEmptySlotInBucket(ByteBuffer bucket) {
		for (int slotNo = 0; slotNo < bucketSize; slotNo++) {
			// Check for offsets that are equal to 0
			if (bucket.getInt(ITEM_SIZE * slotNo + 4) == 0) {
				return slotNo;
			}
		}

		return -1;
	}

	/**
	 * Double the number of buckets in the hash file and rehashes the stored
	 * items.
	 */
	private void increaseHashTable()
		throws IOException
	{
		// System.out.println("Increasing hash table to " + (2*_bucketCount) + "
		// buckets...");
		// long startTime = System.currentTimeMillis();

		long oldTableSize = HEADER_LENGTH + (long)bucketCount * recordSize;
		long newTableSize = HEADER_LENGTH + (long)bucketCount * recordSize * 2;
		long oldFileSize = fileChannel.size(); // includes overflow buckets

		// Move any overflow buckets out of the way to a temporary file
		File tmpFile = new File(file.getParentFile(), "rehash_" + file.getName());
		RandomAccessFile tmpRaf = createEmptyFile(tmpFile);
		FileChannel tmpChannel = tmpRaf.getChannel();

		// Transfer the overflow buckets to the temp file
		fileChannel.transferTo(oldTableSize, oldFileSize, tmpChannel);

		// Increase hash table by factor 2
		writeEmptyBuckets(oldTableSize, bucketCount);
		bucketCount *= 2;

		// Discard any remaining overflow buffers
		fileChannel.truncate(newTableSize);

		ByteBuffer bucket = ByteBuffer.allocate(recordSize);
		ByteBuffer newBucket = ByteBuffer.allocate(recordSize);

		// Rehash items in 'normal' buckets, half of these will move to a new
		// location, but none of them will trigger the creation of new overflow
		// buckets. Any (now deprecated) references to overflow buckets are
		// removed too.

		// All items that are moved to a new location end up in one and the same
		// new and empty bucket. All items are divided between the old and the new
		// bucket and the changes to the buckets are written to disk only once.
		for (long bucketOffset = HEADER_LENGTH; bucketOffset < oldTableSize; bucketOffset += recordSize) {
			fileChannel.read(bucket, bucketOffset);

			boolean bucketChanged = false;
			long newBucketOffset = 0L;

			for (int slotNo = 0; slotNo < bucketSize; slotNo++) {
				int id = bucket.getInt(ITEM_SIZE * slotNo + 4);

				if (id != 0) {
					// Slot is not empty
					int hash = bucket.getInt(ITEM_SIZE * slotNo);
					long newOffset = getBucketOffset(hash);

					if (newOffset != bucketOffset) {
						// Move this item to new bucket...
						newBucket.putInt(hash);
						newBucket.putInt(id);

						// ...and remove it from the current bucket
						bucket.putInt(ITEM_SIZE * slotNo, 0);
						bucket.putInt(ITEM_SIZE * slotNo + 4, 0);

						bucketChanged = true;
						newBucketOffset = newOffset;
					}
				}
			}

			if (bucketChanged) {
				// Some of the items were moved to the new bucket, write it to the
				// file
				newBucket.flip();
				fileChannel.write(newBucket, newBucketOffset);
				newBucket.clear();
			}

			// Reset overflow ID in the old bucket to 0 if necessary
			if (bucket.getInt(ITEM_SIZE * bucketSize) != 0) {
				bucket.putInt(ITEM_SIZE * bucketSize, 0);
				bucketChanged = true;
			}

			if (bucketChanged) {
				// Some of the items were moved to the new bucket or the overflow
				// ID has been reset; write the bucket back to the file
				bucket.rewind();
				fileChannel.write(bucket, bucketOffset);
			}

			bucket.clear();
		}

		// Rehash items in overflow buckets. This might trigger the creation of
		// new overflow buckets so we can't optimize this in the same way as we
		// rehash the normal buckets.
		long tmpFileSize = tmpChannel.size();
		for (long bucketOffset = 0L; bucketOffset < tmpFileSize; bucketOffset += recordSize) {
			tmpChannel.read(bucket, bucketOffset);

			for (int slotNo = 0; slotNo < bucketSize; slotNo++) {
				int id = bucket.getInt(ITEM_SIZE * slotNo + 4);

				if (id != 0) {
					// Slot is not empty
					int hash = bucket.getInt(ITEM_SIZE * slotNo);
					long newBucketOffset = getBucketOffset(hash);

					// Move this item to new location...
					_storeID(newBucketOffset, hash, id);

					// ...and remove it from the current bucket
					bucket.putInt(ITEM_SIZE * slotNo, 0);
					bucket.putInt(ITEM_SIZE * slotNo + 4, 0);
				}
			}

			bucket.clear();
		}

		// Discard the temp file
		tmpRaf.close();
		tmpFile.delete();

		// long endTime = System.currentTimeMillis();
		// System.out.println("Hash table rehashed in " + (endTime-startTime) + "
		// ms");
	}

	public void dumpContents(PrintStream out)
		throws IOException
	{
		out.println();
		out.println("*** hash file contents ***");

		out.println("_bucketCount=" + bucketCount);
		out.println("_bucketSize=" + bucketSize);
		out.println("_itemCount=" + itemCount);

		ByteBuffer buf = ByteBuffer.allocate(recordSize);
		fileChannel.position(HEADER_LENGTH);

		out.println("---Buckets---");

		for (int bucketNo = 1; bucketNo <= bucketCount; bucketNo++) {
			buf.clear();
			fileChannel.read(buf);

			out.print("Bucket " + bucketNo + ": ");

			for (int slotNo = 0; slotNo < bucketSize; slotNo++) {
				int hash = buf.getInt(ITEM_SIZE * slotNo);
				int id = buf.getInt(ITEM_SIZE * slotNo + 4);
				if (slotNo > 0) {
					out.print(" ");
				}
				out.print("[" + toHexString(hash) + "," + id + "]");
			}

			int overflowID = buf.getInt(ITEM_SIZE * bucketSize);
			out.println("---> " + overflowID);
		}

		out.println("---Overflow Buckets---");

		int bucketNo = 0;
		while (fileChannel.position() < fileChannel.size()) {
			buf.clear();
			fileChannel.read(buf);
			bucketNo++;

			out.print("Bucket " + bucketNo + ": ");

			for (int slotNo = 0; slotNo < bucketSize; slotNo++) {
				int hash = buf.getInt(ITEM_SIZE * slotNo);
				int id = buf.getInt(ITEM_SIZE * slotNo + 4);
				if (slotNo > 0) {
					out.print(" ");
				}
				out.print("[" + toHexString(hash) + "," + id + "]");
			}

			int overflowID = buf.getInt(ITEM_SIZE * bucketSize);
			out.println("---> " + overflowID);
		}

		out.println("*** end of hash file contents ***");
		out.println();
	}

	private String toHexString(int decimal) {
		String hex = Integer.toHexString(decimal);

		StringBuilder result = new StringBuilder(8);
		for (int i = hex.length(); i < 8; i++) {
			result.append("0");
		}
		result.append(hex);

		return result.toString();
	}

	/*------------------------*
	 * Inner class IDIterator *
	 *------------------------*/

	public class IDIterator {

		private int _queryHash;

		private ByteBuffer _bucketBuffer;

		private long _bucketOffset;

		private int _slotNo;

		private IDIterator(int hash)
			throws IOException
		{
			_queryHash = hash;

			_bucketBuffer = ByteBuffer.allocate(getRecordSize());

			// Calculate offset for initial bucket
			_bucketOffset = getBucketOffset(hash);

			// Read initial bucket
			getFileChannel().read(_bucketBuffer, _bucketOffset);

			_slotNo = -1;
		}

		/**
		 * Returns the next ID that has been mapped to the specified hash code, or
		 * <tt>-1</tt> if no more IDs were found.
		 */
		public int next()
			throws IOException
		{
			while (_bucketBuffer != null) {
				// Search through current bucket
				_slotNo++;
				while (_slotNo < getBucketSize()) {
					if (_bucketBuffer.getInt(ITEM_SIZE * _slotNo) == _queryHash) {
						return _bucketBuffer.getInt(ITEM_SIZE * _slotNo + 4);
					}
					_slotNo++;
				}

				// No matching hash code in current bucket, check overflow bucket
				int overflowID = _bucketBuffer.getInt(ITEM_SIZE * getBucketSize());
				if (overflowID == 0) {
					// No overflow bucket, end the search
					_bucketBuffer = null;
					_bucketOffset = 0L;
				}
				else {
					// Continue with overflow bucket
					_bucketOffset = getOverflowBucketOffset(overflowID);
					_bucketBuffer.clear();
					getFileChannel().read(_bucketBuffer, _bucketOffset);
					_slotNo = -1;
				}
			}

			return -1;
		}
	} // End inner class IDIterator

	public static void main(String[] args)
		throws Exception
	{
		HashFile hashFile = new HashFile(new File(args[0]));
		hashFile.dumpContents(System.out);
		hashFile.close();
	}

} // End class HashFile
