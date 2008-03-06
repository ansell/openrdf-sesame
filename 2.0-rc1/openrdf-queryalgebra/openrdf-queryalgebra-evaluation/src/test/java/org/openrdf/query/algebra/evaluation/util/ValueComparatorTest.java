/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.util;

import junit.framework.TestCase;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.XMLSchema;

/**
 * 
 * @author james
 * 
 */
public class ValueComparatorTest extends TestCase {
	private ValueFactory vf = new ValueFactoryImpl();

	private BNode bnode1 = vf.createBNode();

	private BNode bnode2 = vf.createBNode();

	private URI uri1 = vf.createURI("http://script.example/Latin");

	private URI uri2 = vf.createURI("http://script.example/Кириллица");

	private URI uri3 = vf.createURI("http://script.example/日本語");

	private Literal typed1 = vf.createLiteral("http://script.example/Latin",
			XMLSchema.STRING);

	private ValueComparator cmp = new ValueComparator();

	public void testBothNull() throws Exception {
		assertTrue(cmp.compare(null, null) == 0);
	}

	public void testLeftNull() throws Exception {
		assertTrue(cmp.compare(null, typed1) < 0);
	}

	public void testRightNull() throws Exception {
		assertTrue(cmp.compare(typed1, null) > 0);
	}

	public void testBothBnode() throws Exception {
		assertTrue(cmp.compare(bnode1, bnode2) == 0);
	}

	public void testLeftBnode() throws Exception {
		assertTrue(cmp.compare(bnode1, typed1) < 0);
	}

	public void testRightBnode() throws Exception {
		assertTrue(cmp.compare(typed1, bnode1) > 0);
	}

	public void testBothURI() throws Exception {
		assertTrue(cmp.compare(uri1, uri1) == 0);
		assertTrue(cmp.compare(uri1, uri2) < 0);
		assertTrue(cmp.compare(uri1, uri3) < 0);
		assertTrue(cmp.compare(uri2, uri1) > 0);
		assertTrue(cmp.compare(uri2, uri2) == 0);
		assertTrue(cmp.compare(uri2, uri3) < 0);
		assertTrue(cmp.compare(uri3, uri1) > 0);
		assertTrue(cmp.compare(uri3, uri2) > 0);
		assertTrue(cmp.compare(uri3, uri3) == 0);
	}

	public void testLeftURI() throws Exception {
		assertTrue(cmp.compare(uri1, typed1) < 0);
	}

	public void testRightURI() throws Exception {
		assertTrue(cmp.compare(typed1, uri1) > 0);
	}

}
