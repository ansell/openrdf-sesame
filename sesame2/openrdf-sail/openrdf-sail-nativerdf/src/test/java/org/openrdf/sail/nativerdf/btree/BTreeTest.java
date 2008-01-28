/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.nativerdf.btree;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;

/**
 * @author Arjohn Kampman
 */
public class BTreeTest extends TestCase {

	/*-----------*
	 * Constants *
	 *-----------*/

	private static List<byte[]> TEST_VALUES = new ArrayList<byte[]>(256);

	private static List<byte[]> RANDOMIZED_TEST_VALUES = new ArrayList<byte[]>(256);

	static {
		for (int i = 0; i < 256; i++) {
			byte[] value = new byte[1];
			value[0] = (byte)i;
			TEST_VALUES.add(value);
		}

		RANDOMIZED_TEST_VALUES.addAll(TEST_VALUES);
		Collections.shuffle(RANDOMIZED_TEST_VALUES);
	}

	/*-----------*
	 * Variables *
	 *-----------*/

	private File dataFile;

	private BTree btree;

	/*---------*
	 * Methods *
	 *---------*/

	@Override
	protected void setUp()
		throws Exception
	{
		super.setUp();
		dataFile = File.createTempFile("btree", null);
		btree = new BTree(dataFile, 85, 1);
	}

	@Override
	protected void tearDown()
		throws Exception
	{
		try {
			btree.close();
		}
		finally {
			try {
				dataFile.delete();
			}
			finally {
				super.tearDown();
			}
		}
	}

	public void testAddAscending()
		throws Exception
	{
		for (byte[] value : TEST_VALUES) {
			btree.insert(value);
		}
	}

	public void testAddDescending()
		throws Exception
	{
		for (int i = TEST_VALUES.size() - 1; i >= 0; i--) {
			btree.insert(TEST_VALUES.get(i));
		}
	}

	public void testAddRandom()
		throws Exception
	{
		for (byte[] value : RANDOMIZED_TEST_VALUES) {
			btree.insert(value);
		}
	}

	public void testRemoveAscending()
		throws Exception
	{
		testAddRandom();

		for (byte[] value : TEST_VALUES) {
			btree.remove(value);
		}
	}

	public void testRemoveDescending()
		throws Exception
	{
		testAddRandom();

		for (int i = TEST_VALUES.size() - 1; i >= 0; i--) {
			btree.remove(TEST_VALUES.get(i));
		}
	}

	public void testRemoveRandom()
		throws Exception
	{
		testAddAscending();

		for (byte[] value : RANDOMIZED_TEST_VALUES) {
			btree.remove(value);
		}
	}

	public void testConcurrentAccess()
		throws Exception
	{
		int meanIdx = TEST_VALUES.size() / 2;
		btree.insert(TEST_VALUES.get(meanIdx - 1));
		btree.insert(TEST_VALUES.get(meanIdx));
		btree.insert(TEST_VALUES.get(meanIdx + 1));

		RecordIterator iter1 = btree.iterateAll();
		iter1.next();

		RecordIterator iter2 = btree.iterateAll();
		iter2.next();
		iter2.next();
		iter2.next();

		for (byte[] value : TEST_VALUES) {
			btree.insert(value);
		}
		
		iter2.close();
		iter1.close();
	}
}
