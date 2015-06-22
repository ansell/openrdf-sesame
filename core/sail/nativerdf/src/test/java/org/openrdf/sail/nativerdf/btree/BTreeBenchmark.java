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

import java.io.File;
import java.io.IOException;
import java.util.Random;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import info.aduna.io.ByteArrayUtil;
import info.aduna.io.FileUtil;

/**
 * @author Arjohn Kampman
 */
public class BTreeBenchmark {

	private static final int VALUE_COUNT = 100 * 1000;

	/*-----------*
	 * Variables *
	 *-----------*/

	private File dir;

	private BTree btree;

	/*---------*
	 * Methods *
	 *---------*/

	@Before
	public void setUp()
		throws Exception
	{
		dir = FileUtil.createTempDir("btree");
		btree = new BTree(dir, "test", 4096, 8);
	}

	@After
	public void tearDown()
		throws Exception
	{
		btree.delete();
		FileUtil.deleteDir(dir);
	}


	@Test
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

	@Test
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

	@Test
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

	@Test
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

	@Test
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

	@Test
	public void testRangeScan4()
		throws Exception
	{
		testRangeScan(4L);
	}

	@Test
	public void testRangeScan20()
		throws Exception
	{
		testRangeScan(20L);
	}

	@Test
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
