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

import java.util.Arrays;

import junit.framework.TestCase;

import org.openrdf.model.impl.BNodeImpl;
import org.openrdf.model.impl.URIImpl;

public class LongIdSequenceTest extends TestCase {

	private IdSequence ids;

	private long STEP;

	public void testStep()
		throws Exception
	{
		assertEquals("0", Long.toBinaryString(0l));
		assertEquals(x60('1'), Long.toBinaryString(STEP - 1));
		assertEquals("1" + x60('0'), Long.toBinaryString(STEP));
		assertEquals("10" + x60('0'), Long.toBinaryString(STEP * 2));
	}

	public void testMinMax()
		throws Exception
	{
		for (ValueType code : ValueType.values()) {
			assertTrue(ids.minId(code).longValue() < ids.maxId(code).longValue());
		}
	}

	public void testDecode()
		throws Exception
	{
		for (ValueType code : ValueType.values()) {
			assertEquals(code, ids.valueOf(ids.minId(code)));
			assertEquals(code, ids.valueOf(ids.minId(code).longValue() + 1));
			assertEquals(code, ids.valueOf(ids.maxId(code)));
			assertEquals(code, ids.valueOf(ids.maxId(code).longValue() - 1));
		}
	}

	public void testMin()
		throws Exception
	{
		for (ValueType code : ValueType.values()) {
			String min = Long.toBinaryString(ids.minId(code).longValue());
			if (ids.minId(code).longValue() == 0) {
				assertEquals("0", min);
			}
			else {
				assertEquals(x60('0'), min.substring(min.length() - 60));
			}
		}
	}

	public void testMax()
		throws Exception
	{
		for (ValueType code : ValueType.values()) {
			String max = Long.toBinaryString(ids.maxId(code).longValue());
			assertEquals(x60('1'), max.substring(max.length() - 60));
		}
	}

	public void testEncode()
		throws Exception
	{
		assertEquals(ValueType.URI, ids.valueOf(ids.hashOf(new URIImpl("urn:root"))));
		assertEquals(ValueType.URI, ids.valueOf(ids.hashOf(new URIImpl(
				"urn:The quick brown fox jumps over the lazy dog"))));
	}

	public void testBNode()
		throws Exception
	{
		assertEquals(3161856189434237699l, ids.hashOf(new BNodeImpl("node13459o40ix3")));
		assertEquals(2859030200227941027l, ids.hashOf(new BNodeImpl("node13459o4d6x1")));
	}

	@Override
	protected void setUp()
		throws Exception
	{
		ids = new LongIdSequence();
		ids.init();
		STEP = ids.minId(ValueType.values()[1]).longValue();
	}

	@Override
	protected void tearDown()
		throws Exception
	{
	}

	private String x60(char c) {
		char[] a = new char[60];
		Arrays.fill(a, 0, a.length, c);
		return new String(a);
	}
}
