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
package org.openrdf.model.util;

import static org.junit.Assert.*;

import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

import org.junit.Ignore;
import org.junit.Test;

import org.openrdf.model.Literal;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.XMLSchema;

/**
 * Tests for {@link LiteralUtil}.
 * 
 * @author Peter Ansell
 */
public class LiteralUtilTest {

	/**
	 * Test method for
	 * {@link org.openrdf.model.util.LiteralUtil#getLabel(org.openrdf.model.Literal, java.lang.String)}
	 * .
	 */
	@Ignore
	@Test
	public final void testGetLabelLiteralString() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.util.LiteralUtil#getLabel(org.openrdf.model.Value, java.lang.String)}
	 * .
	 */
	@Ignore
	@Test
	public final void testGetLabelValueString() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.util.LiteralUtil#getByteValue(org.openrdf.model.Literal, byte)}
	 * .
	 */
	@Ignore
	@Test
	public final void testGetByteValueLiteralByte() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.util.LiteralUtil#getByteValue(org.openrdf.model.Value, byte)}
	 * .
	 */
	@Ignore
	@Test
	public final void testGetByteValueValueByte() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.util.LiteralUtil#getShortValue(org.openrdf.model.Literal, short)}
	 * .
	 */
	@Ignore
	@Test
	public final void testGetShortValueLiteralShort() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.util.LiteralUtil#getShortValue(org.openrdf.model.Value, short)}
	 * .
	 */
	@Ignore
	@Test
	public final void testGetShortValueValueShort() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.util.LiteralUtil#getIntValue(org.openrdf.model.Literal, int)}
	 * .
	 */
	@Ignore
	@Test
	public final void testGetIntValueLiteralInt() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.util.LiteralUtil#getIntValue(org.openrdf.model.Value, int)}
	 * .
	 */
	@Ignore
	@Test
	public final void testGetIntValueValueInt() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.util.LiteralUtil#getLongValue(org.openrdf.model.Literal, long)}
	 * .
	 */
	@Ignore
	@Test
	public final void testGetLongValueLiteralLong() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.util.LiteralUtil#getLongValue(org.openrdf.model.Value, long)}
	 * .
	 */
	@Ignore
	@Test
	public final void testGetLongValueValueLong() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.util.LiteralUtil#getIntegerValue(org.openrdf.model.Literal, java.math.BigInteger)}
	 * .
	 */
	@Ignore
	@Test
	public final void testGetIntegerValueLiteralBigInteger() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.util.LiteralUtil#getIntegerValue(org.openrdf.model.Value, java.math.BigInteger)}
	 * .
	 */
	@Ignore
	@Test
	public final void testGetIntegerValueValueBigInteger() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.util.LiteralUtil#getDecimalValue(org.openrdf.model.Literal, java.math.BigDecimal)}
	 * .
	 */
	@Ignore
	@Test
	public final void testGetDecimalValueLiteralBigDecimal() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.util.LiteralUtil#getDecimalValue(org.openrdf.model.Value, java.math.BigDecimal)}
	 * .
	 */
	@Ignore
	@Test
	public final void testGetDecimalValueValueBigDecimal() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.util.LiteralUtil#getFloatValue(org.openrdf.model.Literal, float)}
	 * .
	 */
	@Ignore
	@Test
	public final void testGetFloatValueLiteralFloat() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.util.LiteralUtil#getFloatValue(org.openrdf.model.Value, float)}
	 * .
	 */
	@Ignore
	@Test
	public final void testGetFloatValueValueFloat() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.util.LiteralUtil#getDoubleValue(org.openrdf.model.Literal, double)}
	 * .
	 */
	@Ignore
	@Test
	public final void testGetDoubleValueLiteralDouble() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.util.LiteralUtil#getDoubleValue(org.openrdf.model.Value, double)}
	 * .
	 */
	@Ignore
	@Test
	public final void testGetDoubleValueValueDouble() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.util.LiteralUtil#getBooleanValue(org.openrdf.model.Literal, boolean)}
	 * .
	 */
	@Ignore
	@Test
	public final void testGetBooleanValueLiteralBoolean() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.util.LiteralUtil#getBooleanValue(org.openrdf.model.Value, boolean)}
	 * .
	 */
	@Ignore
	@Test
	public final void testGetBooleanValueValueBoolean() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.util.LiteralUtil#getCalendarValue(org.openrdf.model.Literal, javax.xml.datatype.XMLGregorianCalendar)}
	 * .
	 */
	@Ignore
	@Test
	public final void testGetCalendarValueLiteralXMLGregorianCalendar() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.util.LiteralUtil#getCalendarValue(org.openrdf.model.Value, javax.xml.datatype.XMLGregorianCalendar)}
	 * .
	 */
	@Ignore
	@Test
	public final void testGetCalendarValueValueXMLGregorianCalendar() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.util.LiteralUtil#getLocale(org.openrdf.model.Literal, java.util.Locale)}
	 * .
	 */
	@Ignore
	@Test
	public final void testGetLocale() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.util.LiteralUtil#createLiteral(org.openrdf.model.ValueFactory, java.lang.Object)}
	 * .
	 */
	@Test
	public void testCreateLiteralObjectNull() {

		Object obj = null;

		try {
			LiteralUtil.createLiteral(ValueFactoryImpl.getInstance(), obj);
			fail("Did not find expected exception");
		}
		catch (NullPointerException npe) {
			assertTrue(npe.getMessage().contains("Cannot create a literal from a null"));
		}
	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.util.LiteralUtil#createLiteral(org.openrdf.model.ValueFactory, java.lang.Object)}
	 * .
	 */
	@Test
	public void testCreateLiteralObjectBoolean() {

		Object obj = Boolean.TRUE;

		Literal l = LiteralUtil.createLiteral(ValueFactoryImpl.getInstance(), obj);
		assertNotNull(l);
		assertEquals(l.getDatatype(), XMLSchema.BOOLEAN);
		assertTrue(l.booleanValue());

	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.util.LiteralUtil#createLiteral(org.openrdf.model.ValueFactory, java.lang.Object)}
	 * .
	 */
	@Test
	public void testCreateLiteralObjectByte() {

		Object obj = new Integer(42).byteValue();

		Literal l = LiteralUtil.createLiteral(ValueFactoryImpl.getInstance(), obj);
		assertNotNull(l);
		assertEquals(l.getDatatype(), XMLSchema.BYTE);
		assertEquals(l.getLabel(), "42");

	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.util.LiteralUtil#createLiteral(org.openrdf.model.ValueFactory, java.lang.Object)}
	 * .
	 */
	@Test
	public void testCreateLiteralObjectDouble() {

		Object obj = new Double(42);

		Literal l = LiteralUtil.createLiteral(ValueFactoryImpl.getInstance(), obj);
		assertNotNull(l);
		assertEquals(l.getDatatype(), XMLSchema.DOUBLE);
		assertEquals(l.getLabel(), "42.0");

	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.util.LiteralUtil#createLiteral(org.openrdf.model.ValueFactory, java.lang.Object)}
	 * .
	 */
	@Test
	public void testCreateLiteralObjectFloat() {

		Object obj = new Float(42);

		Literal l = LiteralUtil.createLiteral(ValueFactoryImpl.getInstance(), obj);
		assertNotNull(l);
		assertEquals(l.getDatatype(), XMLSchema.FLOAT);
		assertEquals(l.getLabel(), "42.0");

	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.util.LiteralUtil#createLiteral(org.openrdf.model.ValueFactory, java.lang.Object)}
	 * .
	 */
	@Test
	public void testCreateLiteralObjectInteger() {

		Object obj = new Integer(4);

		Literal l = LiteralUtil.createLiteral(ValueFactoryImpl.getInstance(), obj);
		assertNotNull(l);
		assertEquals(l.getDatatype(), XMLSchema.INT);
		assertEquals(l.getLabel(), "4");

	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.util.LiteralUtil#createLiteral(org.openrdf.model.ValueFactory, java.lang.Object)}
	 * .
	 */
	@Test
	public void testCreateLiteralObjectLong() {

		Object obj = new Long(42);

		Literal l = LiteralUtil.createLiteral(ValueFactoryImpl.getInstance(), obj);
		assertNotNull(l);
		assertEquals(l.getDatatype(), XMLSchema.LONG);
		assertEquals(l.getLabel(), "42");

	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.util.LiteralUtil#createLiteral(org.openrdf.model.ValueFactory, java.lang.Object)}
	 * .
	 */
	@Test
	public void testCreateLiteralObjectShort() {

		Object obj = Short.parseShort("42");

		Literal l = LiteralUtil.createLiteral(ValueFactoryImpl.getInstance(), obj);
		assertNotNull(l);
		assertEquals(l.getDatatype(), XMLSchema.SHORT);
		assertEquals("42", l.getLabel());

	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.util.LiteralUtil#createLiteral(org.openrdf.model.ValueFactory, java.lang.Object)}
	 * .
	 */
	@Test
	public void testCreateLiteralObjectXMLGregorianCalendar() {

		GregorianCalendar c = new GregorianCalendar();
		c.setTime(new Date());
		try {
			Object obj = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
			Literal l = LiteralUtil.createLiteral(ValueFactoryImpl.getInstance(), obj);
			assertNotNull(l);
			assertEquals(l.getDatatype(), XMLSchema.DATETIME);
			// TODO check lexical value?
		}
		catch (DatatypeConfigurationException e) {
			e.printStackTrace();
			fail("Could not instantiate javax.xml.datatype.DatatypeFactory");
		}

	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.util.LiteralUtil#createLiteral(org.openrdf.model.ValueFactory, java.lang.Object)}
	 * .
	 */
	@Test
	public void testCreateLiteralObjectDate() {

		Object obj = new Date();

		Literal l = LiteralUtil.createLiteral(ValueFactoryImpl.getInstance(), obj);
		assertNotNull(l);
		assertEquals(l.getDatatype(), XMLSchema.DATETIME);

	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.util.LiteralUtil#createLiteral(org.openrdf.model.ValueFactory, java.lang.Object)}
	 * .
	 */
	@Test
	public void testCreateLiteralObjectString() {

		Object obj = "random unique string";

		Literal l = LiteralUtil.createLiteral(ValueFactoryImpl.getInstance(), obj);
		assertNotNull(l);
		assertEquals(l.getDatatype(), XMLSchema.STRING);
		assertEquals(l.getLabel(), "random unique string");

	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.util.LiteralUtil#createLiteral(org.openrdf.model.ValueFactory, java.lang.Object)}
	 * .
	 */
	@Test
	public void testCreateLiteralObjectObject() {

		Object obj = new Object();

		Literal l = LiteralUtil.createLiteral(ValueFactoryImpl.getInstance(), obj);
		assertNotNull(l);
		assertEquals(l.getDatatype(), XMLSchema.STRING);

	}

}
