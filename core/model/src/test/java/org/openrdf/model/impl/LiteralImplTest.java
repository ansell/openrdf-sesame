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
package org.openrdf.model.impl;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.XMLSchema;

/**
 * Unit tests for {@link LiteralImpl}.
 *
 * @author Peter Ansell
 */
public class LiteralImplTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp()
		throws Exception
	{
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown()
		throws Exception
	{
	}

	/**
	 * Test method for {@link org.openrdf.model.impl.LiteralImpl#hashCode()}.
	 */
	@Ignore("TODO: Implement me!")
	@Test
	public final void testHashCode()
		throws Exception
	{
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.impl.LiteralImpl#LiteralImpl(java.lang.String)}.
	 */
	@Test
	public final void testLiteralImplStringNull()
		throws Exception
	{
		thrown.expect(IllegalArgumentException.class);
		new LiteralImpl(null);
	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.impl.LiteralImpl#LiteralImpl(java.lang.String)}.
	 */
	@Test
	public final void testLiteralImplStringEmpty()
		throws Exception
	{
		Literal test = new LiteralImpl("");
		assertEquals("", test.getLabel());
		assertNull(test.getLanguage());
		assertEquals(XMLSchema.STRING, test.getDatatype());
	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.impl.LiteralImpl#LiteralImpl(java.lang.String)}.
	 */
	@Test
	public final void testLiteralImplStringLong()
		throws Exception
	{
		StringBuilder testBuilder = new StringBuilder(1000000);
		for (int i = 0; i < 1000000; i++) {
			testBuilder.append(Integer.toHexString(i % 16));
		}

		Literal test = new LiteralImpl(testBuilder.toString());
		assertEquals(testBuilder.toString(), test.getLabel());
		assertNull(test.getLanguage());
		assertEquals(XMLSchema.STRING, test.getDatatype());
	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.impl.LiteralImpl#LiteralImpl(java.lang.String, java.lang.String)}
	 * .
	 */
	@Test
	public final void testLiteralImplStringStringNullNull()
		throws Exception
	{
		String label = null;
		String language = null;
		
		thrown.expect(IllegalArgumentException.class);
		new LiteralImpl(label, language);
	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.impl.LiteralImpl#LiteralImpl(java.lang.String, java.lang.String)}
	 * .
	 */
	@Test
	public final void testLiteralImplStringStringEmptyNull()
		throws Exception
	{
		String label = "";
		String language = null;
		
		thrown.expect(NullPointerException.class);
		new LiteralImpl(label, language);
	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.impl.LiteralImpl#LiteralImpl(java.lang.String, java.lang.String)}
	 * .
	 */
	@Test
	public final void testLiteralImplStringStringNullEmpty()
		throws Exception
	{
		String label = null;
		String language = "";
		
		thrown.expect(IllegalArgumentException.class);
		new LiteralImpl(label, language);
	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.impl.LiteralImpl#LiteralImpl(java.lang.String, org.openrdf.model.URI)}
	 * .
	 */
	@Test
	public final void testLiteralImplStringURINullNull()
		throws Exception
	{
		String label = null;
		URI datatype = null;
		
		thrown.expect(IllegalArgumentException.class);
		new LiteralImpl(label, datatype);
	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.impl.LiteralImpl#LiteralImpl(java.lang.String, org.openrdf.model.URI)}
	 * .
	 */
	@Test
	public final void testLiteralImplStringURINullString()
		throws Exception
	{
		String label = null;
		URI datatype = XMLSchema.STRING;
		
		thrown.expect(IllegalArgumentException.class);
		new LiteralImpl(label, datatype);
	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.impl.LiteralImpl#LiteralImpl(java.lang.String, org.openrdf.model.URI)}
	 * .
	 */
	@Test
	public final void testLiteralImplStringURINullLangString()
		throws Exception
	{
		String label = null;
		URI datatype = RDF.LANGSTRING;
		
		thrown.expect(IllegalArgumentException.class);
		new LiteralImpl(label, datatype);
	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.impl.LiteralImpl#LiteralImpl(java.lang.String, org.openrdf.model.URI)}
	 * .
	 */
	@Test
	public final void testLiteralImplStringURIEmptyNull()
		throws Exception
	{
		String label = "";
		URI datatype = null;
		
		Literal test = new LiteralImpl(label, datatype);
		assertEquals("", test.getLabel());
		assertNull(test.getLanguage());
		assertEquals(XMLSchema.STRING, test.getDatatype());
	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.impl.LiteralImpl#LiteralImpl(java.lang.String, org.openrdf.model.URI)}
	 * .
	 */
	@Test
	public final void testLiteralImplStringURIEmptyLangString()
		throws Exception
	{
		String label = "";
		URI datatype = RDF.LANGSTRING;
		
		thrown.expect(NullPointerException.class);
		new LiteralImpl(label, datatype);
	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.impl.LiteralImpl#setLabel(java.lang.String)}.
	 */
	@Ignore("TODO: Implement me!")
	@Test
	public final void testSetLabel()
		throws Exception
	{
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link org.openrdf.model.impl.LiteralImpl#getLabel()}.
	 */
	@Ignore("TODO: Implement me!")
	@Test
	public final void testGetLabel()
		throws Exception
	{
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.impl.LiteralImpl#setLanguage(java.lang.String)}.
	 */
	@Ignore("TODO: Implement me!")
	@Test
	public final void testSetLanguage()
		throws Exception
	{
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link org.openrdf.model.impl.LiteralImpl#getLanguage()}.
	 */
	@Ignore("TODO: Implement me!")
	@Test
	public final void testGetLanguage()
		throws Exception
	{
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.impl.LiteralImpl#setDatatype(org.openrdf.model.URI)}
	 * .
	 */
	@Ignore("TODO: Implement me!")
	@Test
	public final void testSetDatatype()
		throws Exception
	{
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link org.openrdf.model.impl.LiteralImpl#getDatatype()}.
	 */
	@Ignore("TODO: Implement me!")
	@Test
	public final void testGetDatatype()
		throws Exception
	{
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.impl.LiteralImpl#equals(java.lang.Object)}.
	 */
	@Ignore("TODO: Implement me!")
	@Test
	public final void testEqualsObject()
		throws Exception
	{
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link org.openrdf.model.impl.LiteralImpl#toString()}.
	 */
	@Ignore("TODO: Implement me!")
	@Test
	public final void testToString()
		throws Exception
	{
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link org.openrdf.model.impl.LiteralImpl#stringValue()}.
	 */
	@Ignore("TODO: Implement me!")
	@Test
	public final void testStringValue()
		throws Exception
	{
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link org.openrdf.model.impl.LiteralImpl#booleanValue()}.
	 */
	@Ignore("TODO: Implement me!")
	@Test
	public final void testBooleanValue()
		throws Exception
	{
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link org.openrdf.model.impl.LiteralImpl#byteValue()}.
	 */
	@Ignore("TODO: Implement me!")
	@Test
	public final void testByteValue()
		throws Exception
	{
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link org.openrdf.model.impl.LiteralImpl#shortValue()}.
	 */
	@Ignore("TODO: Implement me!")
	@Test
	public final void testShortValue()
		throws Exception
	{
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link org.openrdf.model.impl.LiteralImpl#intValue()}.
	 */
	@Ignore("TODO: Implement me!")
	@Test
	public final void testIntValue()
		throws Exception
	{
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link org.openrdf.model.impl.LiteralImpl#longValue()}.
	 */
	@Ignore("TODO: Implement me!")
	@Test
	public final void testLongValue()
		throws Exception
	{
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link org.openrdf.model.impl.LiteralImpl#floatValue()}.
	 */
	@Ignore("TODO: Implement me!")
	@Test
	public final void testFloatValue()
		throws Exception
	{
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link org.openrdf.model.impl.LiteralImpl#doubleValue()}.
	 */
	@Ignore("TODO: Implement me!")
	@Test
	public final void testDoubleValue()
		throws Exception
	{
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link org.openrdf.model.impl.LiteralImpl#integerValue()}.
	 */
	@Ignore("TODO: Implement me!")
	@Test
	public final void testIntegerValue()
		throws Exception
	{
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link org.openrdf.model.impl.LiteralImpl#decimalValue()}.
	 */
	@Ignore("TODO: Implement me!")
	@Test
	public final void testDecimalValue()
		throws Exception
	{
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link org.openrdf.model.impl.LiteralImpl#calendarValue()}
	 * .
	 */
	@Ignore("TODO: Implement me!")
	@Test
	public final void testCalendarValue()
		throws Exception
	{
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link java.lang.Object#equals(java.lang.Object)}.
	 */
	@Ignore("TODO: Implement me!")
	@Test
	public final void testEqualsObject1()
		throws Exception
	{
		fail("Not yet implemented"); // TODO
	}

}
