/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.nativerdf.btree;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import junit.framework.TestCase;

import info.aduna.io.ByteArrayUtil;
import info.aduna.io.FileUtil;

/**
 * @author Arjohn Kampman
 */
public class BTreeBenchmark extends TestCase {

	private static final int VALUE_COUNT = 100 * 1000;

	/*-----------*
	 * Variables *
	 *-----------*/

	private File dir;

	private BTree btree;

	/*---------*
	 * Methods *
	 *---------*/

	@Override
	protected void setUp()
		throws Exception
	{
		super.setUp();
		dir = FileUtil.createTempDir("btree");
		btree = new BTree(dir, "test", 4096, 8);
	}

	@Override
	protected void tearDown()
		throws Exception
	{
		try {
			btree.delete();
			FileUtil.deleteDir(dir);
		}
		finally {
			super.tearDown();
		}
	}

	public void testAddAscending()
		throws Exception
	{
		Thread.sleep(500L);
		long startTime = System.currentTimeMillis();

		addAscending(0L, 1L, VALUE_COUNT);
		btree.sync();

		long endTime = System.currentTimeMillis();
		printTime(startTime, endTime, "testAddAscending");
	}

	public void testAddRandom()
		throws Exception
	{
		Thread.sleep(500L);
		long startTime = System.currentTimeMillis();

		addRandom(VALUE_COUNT);
		btree.sync();

		long endTime = System.currentTimeMillis();
		printTime(startTime, endTime, "testAddRandom");
	}

	public void testUpdate()
		throws Exception
	{
		addAscending(0L, 2L, VALUE_COUNT);
		btree.sync();

		Thread.sleep(500L);
		long startTime = System.currentTimeMillis();

		update(0L, 8L, VALUE_COUNT / 4, 1L);
		btree.sync();

		long endTime = System.currentTimeMillis();
		printTime(startTime, endTime, "testUpdate");
	}

	public void testRemove()
		throws Exception
	{
		addAscending(0L, 1L, VALUE_COUNT);
		btree.sync();

		Thread.sleep(500L);
		long startTime = System.currentTimeMillis();

		remove(0L, 4L, VALUE_COUNT / 4);
		btree.sync();

		long endTime = System.currentTimeMillis();
		printTime(startTime, endTime, "testRemove");
	}

	public void testFullScan()
		throws Exception
	{
		addAscending(0L, 1L, VALUE_COUNT);
		btree.sync();

		Thread.sleep(500L);
		long startTime = System.currentTimeMillis();

		RecordIterator iter = btree.iterateAll();
		try {
			while (iter.next() != null) {
			}
		}
		finally {
			iter.close();
		}

		long endTime = System.currentTimeMillis();
		printTime(startTime, endTime, "testFullScan");
	}

	public void testRangeScan4()
		throws Exception
	{
		testRangeScan(4L);
	}

	public void testRangeScan20()
		throws Exception
	{
		testRangeScan(20L);
	}

	public void testRangeScan1000()
		throws Exception
	{
		testRangeScan(1000L);
	}

	private void testRangeScan(long rangeSize)
		throws Exception
	{
		addAscending(0L, 1L, VALUE_COUNT);
		btree.sync();

		byte[] minData = new byte[8];
		byte[] maxData = new byte[8];

		Thread.sleep(500L);
		long startTime = System.currentTimeMillis();

		for (long minValue = 0L; minValue < VALUE_COUNT; minValue += rangeSize) {
			ByteArrayUtil.putLong(minValue, minData, 0);
			ByteArrayUtil.putLong(minValue + rangeSize, maxData, 0);

			RecordIterator iter = btree.iterateRange(minData, maxData);
			try {
				while (iter.next() != null) {
				}
			}
			finally {
				iter.close();
			}
		}

		long endTime = System.currentTimeMillis();
		printTime(startTime, endTime, "testRangeScan" + rangeSize);
	}

	private void addAscending(long startValue, long increment, int valueCount)
		throws IOException
	{
		long value = startValue;

		byte[] data = new byte[8];
		for (int i = 0; i < valueCount; i++) {
			ByteArrayUtil.putLong(value, data, 0);
			btree.insert(data);
			value += increment;
		}
	}

	private void addRandom(int valueCount)
		throws IOException
	{
		Random random = new Random(0L);

		byte[] data = new byte[8];
		for (int i = 0; i < valueCount; i++) {
			ByteArrayUtil.putLong(random.nextLong(), data, 0);
			btree.insert(data);
		}
	}

	private void update(long startValue, long increment, int valueCount, long updateDelta)
		throws IOException
	{
		long oldValue = startValue;
		long newValue;

		byte[] oldData = new byte[8];
		byte[] newData = new byte[8];

		for (int i = 0; i < valueCount; i++) {
			newValue = oldValue += updateDelta;

			ByteArrayUtil.putLong(oldValue, oldData, 0);
			ByteArrayUtil.putLong(newValue, newData, 0);

			btree.insert(newData);
			btree.remove(oldData);

			oldValue += increment;
		}
	}

	private void remove(long startValue, long increment, int valueCount)
		throws IOException
	{
		long value = startValue;
		byte[] data = new byte[8];

		for (int i = 0; i < valueCount; i++) {
			ByteArrayUtil.putLong(value, data, 0);
			btree.remove(data);
			value += increment;
		}
	}

	private void printTime(long startTime, long endTime, String methodName) {
		System.out.println((endTime - startTime) + " ms for " + methodName + "()");
	}
}
