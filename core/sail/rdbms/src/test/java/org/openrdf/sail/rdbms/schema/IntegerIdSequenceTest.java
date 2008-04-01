package org.openrdf.sail.rdbms.schema;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.TestCase;

import org.openrdf.model.impl.BNodeImpl;
import org.openrdf.model.impl.URIImpl;

public class IntegerIdSequenceTest extends TestCase {

	private IdSequence ids;

	private int STEP;

	public void testStep()
		throws Exception
	{
		assertEquals("0", Integer.toBinaryString(0));
		assertEquals(x28('1'), Integer.toBinaryString(STEP - 1));
		assertEquals("1" + x28('0'), Integer.toBinaryString(STEP));
		assertEquals("10" + x28('0'), Integer.toBinaryString(STEP * 2));
	}

	public void testMinMax()
		throws Exception
	{
		for (ValueType code : ValueType.values()) {
			assertTrue(ids.minId(code).intValue() < ids.maxId(code).intValue());
		}
	}

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

	public void testMax()
		throws Exception
	{
		for (ValueType code : ValueType.values()) {
			String max = Long.toBinaryString(ids.maxId(code).intValue());
			assertEquals(x28('1'), max.substring(max.length() - 28));
		}
	}

	public void testEncode()
		throws Exception
	{
		assertEquals(ValueType.URI, ids.valueOf(ids.nextId(new URIImpl("urn:root"))));
		assertEquals(ValueType.URI, ids.valueOf(ids.nextId(new URIImpl(
				"urn:The quick brown fox jumps over the lazy dog"))));
	}

	public void testBNode()
		throws Exception
	{
		assertEquals(3161856189434237699l, ids.hashOf(new BNodeImpl("node13459o40ix3")));
		assertEquals(2859030200227941027l, ids.hashOf(new BNodeImpl("node13459o4d6x1")));
	}

	public void testAtomicInteger()
		throws Exception
	{
		AtomicInteger seq = new AtomicInteger(47);
		assertEquals(48, seq.incrementAndGet());
	}

	@Override
	protected void setUp()
		throws Exception
	{
		ids = new IntegerIdSequence();
		ids.init();
		STEP = ids.minId(ValueType.values()[1]).intValue();
	}

	@Override
	protected void tearDown()
		throws Exception
	{
	}

	private String x28(char c) {
		char[] a = new char[28];
		Arrays.fill(a, 0, a.length, c);
		return new String(a);
	}
}
