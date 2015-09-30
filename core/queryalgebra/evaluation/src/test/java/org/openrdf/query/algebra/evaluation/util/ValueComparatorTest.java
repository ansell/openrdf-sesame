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
package org.openrdf.query.algebra.evaluation.util;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.IRI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.SimpleValueFactory;
import org.openrdf.model.vocabulary.XMLSchema;

/**
 *
 * @author james
 *
 */
public class ValueComparatorTest {

	private ValueFactory vf = SimpleValueFactory.getInstance();

	private BNode bnode1 = vf.createBNode();

	private BNode bnode2 = vf.createBNode();

	private IRI uri1 = vf.createIRI("http://script.example/Latin");

	private IRI uri2 = vf.createIRI("http://script.example/Кириллица");

	private IRI uri3 = vf.createIRI("http://script.example/日本語");

	private Literal typed1 = vf.createLiteral("http://script.example/Latin", XMLSchema.STRING);

	private ValueComparator cmp = new ValueComparator();

	@Test
	public void testBothNull()
		throws Exception
	{
		assertTrue(cmp.compare(null, null) == 0);
	}

	@Test
	public void testLeftNull()
		throws Exception
	{
		assertTrue(cmp.compare(null, typed1) < 0);
	}

	@Test
	public void testRightNull()
		throws Exception
	{
		assertTrue(cmp.compare(typed1, null) > 0);
	}

	@Test
	public void testBothBnode()
		throws Exception
	{
		assertTrue(cmp.compare(bnode1, bnode1) == 0);
		assertTrue(cmp.compare(bnode2, bnode2) == 0);
		assertTrue(cmp.compare(bnode1, bnode2) != cmp.compare(bnode2, bnode1));
		assertTrue(cmp.compare(bnode1, bnode2) == -1 * cmp.compare(bnode2, bnode1));
	}

	@Test
	public void testLeftBnode()
		throws Exception
	{
		assertTrue(cmp.compare(bnode1, typed1) < 0);
	}

	@Test
	public void testRightBnode()
		throws Exception
	{
		assertTrue(cmp.compare(typed1, bnode1) > 0);
	}

	@Test
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

	@Test
	public void testLeftURI()
		throws Exception
	{
		assertTrue(cmp.compare(uri1, typed1) < 0);
	}

	@Test
	public void testRightURI()
		throws Exception
	{
		assertTrue(cmp.compare(typed1, uri1) > 0);
	}

	/**
	 * Tests whether xsd:int's are properly sorted in a list with mixed value
	 * types.
	 */
	@Test
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
	@Test
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
	@Test
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
