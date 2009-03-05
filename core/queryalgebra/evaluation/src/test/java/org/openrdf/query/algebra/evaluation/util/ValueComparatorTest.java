/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.XMLSchema;

/**
 * @author James Leigh
 */
public class ValueComparatorTest extends TestCase {

	private ValueFactory vf = ValueFactoryImpl.getInstance();

	private BNode bnode1 = vf.createBNode();

	private BNode bnode2 = vf.createBNode();

	private URI uri1 = vf.createURI("http://script.example/Latin");

	private URI uri2 = vf.createURI("http://script.example/Кириллица");

	private URI uri3 = vf.createURI("http://script.example/日本語");

	private Literal typed1 = vf.createLiteral("http://script.example/Latin", XMLSchema.STRING);

	private ValueComparator cmp = new ValueComparator();

	public void testBothNull()
		throws Exception
	{
		assertTrue(cmp.compare(null, null) == 0);
	}

	public void testLeftNull()
		throws Exception
	{
		assertTrue(cmp.compare(null, typed1) < 0);
	}

	public void testRightNull()
		throws Exception
	{
		assertTrue(cmp.compare(typed1, null) > 0);
	}

	public void testBothBnode()
		throws Exception
	{
		assertTrue(cmp.compare(bnode1, bnode2) == 0);
	}

	public void testLeftBnode()
		throws Exception
	{
		assertTrue(cmp.compare(bnode1, typed1) < 0);
	}

	public void testRightBnode()
		throws Exception
	{
		assertTrue(cmp.compare(typed1, bnode1) > 0);
	}

	public void testBothURI()
		throws Exception
	{
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

	public void testLeftURI()
		throws Exception
	{
		assertTrue(cmp.compare(uri1, typed1) < 0);
	}

	public void testRightURI()
		throws Exception
	{
		assertTrue(cmp.compare(typed1, uri1) > 0);
	}

	/**
	 * Tests whether xsd:int's are properly sorted in a list with mixed value
	 * types.
	 */
	public void testOrder1()
		throws Exception
	{
		Literal en4 = vf.createLiteral("4", "en");
		Literal int10 = vf.createLiteral(10);
		Literal int9 = vf.createLiteral(9);

		List<Literal> valueList = Arrays.asList(en4, int10, int9);
		Collections.sort(valueList, cmp);

		assertTrue(valueList.indexOf(int9) < valueList.indexOf(int10));
	}

	/**
	 * Tests whether various numerics are properly sorted in a list with mixed
	 * value types.
	 */
	public void testOrder2()
		throws Exception
	{
		Literal en4 = vf.createLiteral("4", "en");
		Literal int10 = vf.createLiteral(10);
		Literal int9 = vf.createLiteral(9);
		Literal plain9 = vf.createLiteral("9");
		Literal integer5 = vf.createLiteral("5", XMLSchema.INTEGER);
		Literal float9 = vf.createLiteral(9f);
		Literal plain4 = vf.createLiteral("4");
		Literal plain10 = vf.createLiteral("10");

		List<Literal> valueList = Arrays.asList(en4, int10, int9, plain9, integer5, float9, plain4, plain10);
		Collections.sort(valueList, cmp);

		assertTrue(valueList.indexOf(integer5) < valueList.indexOf(float9));
		assertTrue(valueList.indexOf(integer5) < valueList.indexOf(int9));
		assertTrue(valueList.indexOf(integer5) < valueList.indexOf(int10));
		assertTrue(valueList.indexOf(float9) < valueList.indexOf(int10));
		assertTrue(valueList.indexOf(int9) < valueList.indexOf(int10));
		assertTrue(valueList.indexOf(int9) < valueList.indexOf(int10));
	}

	/**
	 * Tests whether numerics of different types are properly sorted. The list
	 * also contains a datatype that would be sorted between the numerics if the
	 * datatypes were to be sorted alphabetically.
	 */
	public void testOrder3()
		throws Exception
	{
		Literal year1234 = vf.createLiteral("1234", XMLSchema.GYEAR);
		Literal float2000 = vf.createLiteral(2000f);
		Literal int1000 = vf.createLiteral(1000);

		List<Literal> valueList = Arrays.asList(year1234, float2000, int1000);
		Collections.sort(valueList, cmp);
		assertTrue(valueList.indexOf(int1000) < valueList.indexOf(float2000));
	}
}
