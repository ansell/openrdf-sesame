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
package org.openrdf.sail.rdbms.schema;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Before;
import org.junit.Test;

import org.openrdf.model.impl.SimpleBNode;
import org.openrdf.model.impl.SimpleIRI;

public class IntegerIdSequenceTest {

	private IdSequence ids;

	private int STEP;

	@Test
	public void testStep()
		throws Exception
	{
		assertEquals("0", Integer.toBinaryString(0));
		assertEquals(x28('1'), Integer.toBinaryString(STEP - 1));
		assertEquals("1" + x28('0'), Integer.toBinaryString(STEP));
		assertEquals("10" + x28('0'), Integer.toBinaryString(STEP * 2));
	}

	@Test
	public void testMinMax()
		throws Exception
	{
		for (ValueType code : ValueType.values()) {
			assertTrue(ids.minId(code).intValue() < ids.maxId(code).intValue());
		}
	}

	@Test
	public void testDecode()
		throws Exception
	{
		for (ValueType code : ValueType.values()) {
			assertEquals(code, ids.valueOf(ids.minId(code)));
			assertEquals(code, ids.valueOf(ids.minId(code).intValue() + 1));
			assertEquals(code, ids.valueOf(ids.maxId(code)));
			assertEquals(code, ids.valueOf(ids.maxId(code).intValue() - 1));
		}
	}

	@Test
	public void testMin()
		throws Exception
	{
		for (ValueType code : ValueType.values()) {
			String min = Long.toBinaryString(ids.minId(code).intValue());
			if (ids.minId(code).intValue() == 0) {
				assertEquals("0", min);
			}
			else {
				assertEquals(x28('0'), min.substring(min.length() - 28));
			}
		}
	}

	@Test
	public void testMax()
		throws Exception
	{
		for (ValueType code : ValueType.values()) {
			String max = Long.toBinaryString(ids.maxId(code).intValue());
			assertEquals(x28('1'), max.substring(max.length() - 28));
		}
	}

	@Test
	public void testEncode()
		throws Exception
	{
		assertEquals(ValueType.URI, ids.valueOf(ids.nextId(new SimpleIRI("urn:root"))));
		assertEquals(ValueType.URI, ids.valueOf(ids.nextId(new SimpleIRI(
				"urn:The quick brown fox jumps over the lazy dog"))));
	}

	@Test
	public void testBNode()
		throws Exception
	{
		assertEquals(3161856189434237699l, ids.hashOf(new SimpleBNode("node13459o40ix3")));
		assertEquals(2859030200227941027l, ids.hashOf(new SimpleBNode("node13459o4d6x1")));
	}

	@Test
	public void testAtomicInteger()
		throws Exception
	{
		AtomicInteger seq = new AtomicInteger(47);
		assertEquals(48, seq.incrementAndGet());
	}

	@Before
	public void setUp()
		throws Exception
	{
		ids = new IntegerIdSequence();
		ids.init();
		STEP = ids.minId(ValueType.values()[1]).intValue();
	}

	private String x28(char c) {
		char[] a = new char[28];
		Arrays.fill(a, 0, a.length, c);
		return new String(a);
	}
}
