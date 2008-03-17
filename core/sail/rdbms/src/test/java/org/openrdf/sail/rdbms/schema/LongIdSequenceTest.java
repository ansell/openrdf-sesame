package org.openrdf.sail.rdbms.schema;

import java.util.Arrays;

import org.openrdf.model.impl.URIImpl;

import junit.framework.TestCase;

public class LongIdSequenceTest extends TestCase {
	private IdSequence ids;
	private long STEP;

	public void testStep() throws Exception {
		assertEquals("0", Long.toBinaryString(0l));
		assertEquals(x60('1'), Long.toBinaryString(STEP - 1));
		assertEquals("1" + x60('0'), Long.toBinaryString(STEP));
		assertEquals("10" + x60('0'), Long.toBinaryString(STEP * 2));
	}

	public void testMinMax() throws Exception {
		for (ValueType code : ValueType.values()) {
			assertTrue(ids.minId(code).longValue() < ids.maxId(code).longValue());
		}
	}

	public void testDecode() throws Exception {
		for (ValueType code : ValueType.values()) {
			assertEquals(code, ids.valueOf(ids.minId(code)));
			assertEquals(code, ids.valueOf(ids.minId(code).longValue() + 1));
			assertEquals(code, ids.valueOf(ids.maxId(code)));
			assertEquals(code, ids.valueOf(ids.maxId(code).longValue() - 1));
		}
	}

	public void testMin() throws Exception {
		for (ValueType code : ValueType.values()) {
			String min = Long.toBinaryString(ids.minId(code).longValue());
			if (ids.minId(code).longValue() == 0) {
				assertEquals("0", min);
			} else {
				assertEquals(x60('0'), min.substring(min.length() - 60));
			}
		}
	}

	public void testMax() throws Exception {
		for (ValueType code : ValueType.values()) {
			String max = Long.toBinaryString(ids.maxId(code).longValue());
			assertEquals(x60('1'), max.substring(max.length() - 60));
		}
	}

	public void testEncode() throws Exception {
		assertEquals(ValueType.URI, ids.valueOf(ids.hashOf(new URIImpl("urn:root"))));
		assertEquals(ValueType.URI, ids.valueOf(ids.hashOf(new URIImpl("urn:The quick brown fox jumps over the lazy dog"))));
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
