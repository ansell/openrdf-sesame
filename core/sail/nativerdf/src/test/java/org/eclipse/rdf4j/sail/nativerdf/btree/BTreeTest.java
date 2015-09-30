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
package org.eclipse.rdf4j.sail.nativerdf.btree;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.rdf4j.common.io.FileUtil;
import org.eclipse.rdf4j.sail.nativerdf.btree.BTree;
import org.eclipse.rdf4j.sail.nativerdf.btree.RecordIterator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Arjohn Kampman
 */
public class BTreeTest {

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
		btree = new BTree(dir, "test", 85, 1);
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
		for (byte[] value : TEST_VALUES) {
			btree.insert(value);
		}
	}

	@Test
	public void testAddDescending()
		throws Exception
	{
		for (int i = TEST_VALUES.size() - 1; i >= 0; i--) {
			btree.insert(TEST_VALUES.get(i));
		}
	}

	@Test
	public void testAddRandom()
		throws Exception
	{
		for (byte[] value : RANDOMIZED_TEST_VALUES) {
			btree.insert(value);
		}
	}

	@Test
	public void testRemoveAscending()
		throws Exception
	{
		testAddRandom();

		for (byte[] value : TEST_VALUES) {
			btree.remove(value);
		}
	}

	@Test
	public void testRemoveDescending()
		throws Exception
	{
		testAddRandom();

		for (int i = TEST_VALUES.size() - 1; i >= 0; i--) {
			btree.remove(TEST_VALUES.get(i));
		}
	}

	@Test
	public void testRemoveRandom()
		throws Exception
	{
		testAddAscending();

		for (byte[] value : RANDOMIZED_TEST_VALUES) {
			btree.remove(value);
		}
	}

	@Test
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

	@Test
	public void testNewAndClear()
		throws Exception
	{
		btree.clear();
	}

	/* Test for SES-527
		public void testRootNodeSplit()
			throws Exception
		{
			// Fill the root node
			for (int i = 0; i < 15; i++) {
				btree.insert(TEST_VALUES.get(i));
			}

			// Fire up an iterator
			RecordIterator iter = btree.iterateAll();
			iter.next();

			// Force the root node to split
			btree.insert(TEST_VALUES.get(15));

			// Verify that the iterator returns all 15 elements
			int count = 0;
			while (iter.next() != null) {
				count++;
			}

			iter.close();
			
			assertEquals(15, count);
		}
	*/
}
