package org.openrdf.query.algebra.evaluation;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.query.impl.MapBindingSet;

public class QueryBindingSetTest {

	private final MapBindingSet mbs = new MapBindingSet();

	private final QueryBindingSet qbs = new QueryBindingSet();

	private ValueFactory vf = ValueFactoryImpl.getInstance();

	@Before
	public void setup() {
		qbs.addBinding("foo", vf.createURI("urn:foo"));
		mbs.addBinding("foo", vf.createURI("urn:foo"));
	}

	@Test
	public void testEqualsObject() {

		QueryBindingSet bs = new QueryBindingSet();
		assertFalse(bs.equals(qbs));
		assertFalse(bs.equals(mbs));

		bs.addBinding("foo", vf.createURI("urn:foo"));

		assertEquals(bs, qbs);
		assertEquals(bs, mbs);
		assertEquals(qbs, mbs);
	}

	@Test
	public void testHashCode() {
		assertTrue(qbs.equals(mbs));
		assertTrue(mbs.equals(qbs));
		assertEquals("objects that return true on their equals() method must have identical hash codes",
				qbs.hashCode(), mbs.hashCode());
	}

}
