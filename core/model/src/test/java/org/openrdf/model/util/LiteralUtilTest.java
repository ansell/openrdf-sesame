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
	public final void testGetLabelLiteralString()
		throws Exception
	{
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.util.LiteralUtil#getLabel(org.openrdf.model.Value, java.lang.String)}
	 * .
	 */
	@Ignore
	@Test
	public final void testGetLabelValueString()
		throws Exception
	{
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.util.LiteralUtil#getByteValue(org.openrdf.model.Literal, byte)}
	 * .
	 */
	@Ignore
	@Test
	public final void testGetByteValueLiteralByte()
		throws Exception
	{
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.util.LiteralUtil#getByteValue(org.openrdf.model.Value, byte)}
	 * .
	 */
	@Ignore
	@Test
	public final void testGetByteValueValueByte()
		throws Exception
	{
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.util.LiteralUtil#getShortValue(org.openrdf.model.Literal, short)}
	 * .
	 */
	@Ignore
	@Test
	public final void testGetShortValueLiteralShort()
		throws Exception
	{
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.util.LiteralUtil#getShortValue(org.openrdf.model.Value, short)}
	 * .
	 */
	@Ignore
	@Test
	public final void testGetShortValueValueShort()
		throws Exception
	{
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.util.LiteralUtil#getIntValue(org.openrdf.model.Literal, int)}
	 * .
	 */
	@Ignore
	@Test
	public final void testGetIntValueLiteralInt()
		throws Exception
	{
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.util.LiteralUtil#getIntValue(org.openrdf.model.Value, int)}
	 * .
	 */
	@Ignore
	@Test
	public final void testGetIntValueValueInt()
		throws Exception
	{
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.util.LiteralUtil#getLongValue(org.openrdf.model.Literal, long)}
	 * .
	 */
	@Ignore
	@Test
	public final void testGetLongValueLiteralLong()
		throws Exception
	{
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.util.LiteralUtil#getLongValue(org.openrdf.model.Value, long)}
	 * .
	 */
	@Ignore
	@Test
	public final void testGetLongValueValueLong()
		throws Exception
	{
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.util.LiteralUtil#getIntegerValue(org.openrdf.model.Literal, java.math.BigInteger)}
	 * .
	 */
	@Ignore
	@Test
	public final void testGetIntegerValueLiteralBigInteger()
		throws Exception
	{
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.util.LiteralUtil#getIntegerValue(org.openrdf.model.Value, java.math.BigInteger)}
	 * .
	 */
	@Ignore
	@Test
	public final void testGetIntegerValueValueBigInteger()
		throws Exception
	{
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.util.LiteralUtil#getDecimalValue(org.openrdf.model.Literal, java.math.BigDecimal)}
	 * .
	 */
	@Ignore
	@Test
	public final void testGetDecimalValueLiteralBigDecimal()
		throws Exception
	{
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.util.LiteralUtil#getDecimalValue(org.openrdf.model.Value, java.math.BigDecimal)}
	 * .
	 */
	@Ignore
	@Test
	public final void testGetDecimalValueValueBigDecimal()
		throws Exception
	{
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.util.LiteralUtil#getFloatValue(org.openrdf.model.Literal, float)}
	 * .
	 */
	@Ignore
	@Test
	public final void testGetFloatValueLiteralFloat()
		throws Exception
	{
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.util.LiteralUtil#getFloatValue(org.openrdf.model.Value, float)}
	 * .
	 */
	@Ignore
	@Test
	public final void testGetFloatValueValueFloat()
		throws Exception
	{
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.util.LiteralUtil#getDoubleValue(org.openrdf.model.Literal, double)}
	 * .
	 */
	@Ignore
	@Test
	public final void testGetDoubleValueLiteralDouble()
		throws Exception
	{
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.util.LiteralUtil#getDoubleValue(org.openrdf.model.Value, double)}
	 * .
	 */
	@Ignore
	@Test
	public final void testGetDoubleValueValueDouble()
		throws Exception
	{
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.util.LiteralUtil#getBooleanValue(org.openrdf.model.Literal, boolean)}
	 * .
	 */
	@Ignore
	@Test
	public final void testGetBooleanValueLiteralBoolean()
		throws Exception
	{
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.util.LiteralUtil#getBooleanValue(org.openrdf.model.Value, boolean)}
	 * .
	 */
	@Ignore
	@Test
	public final void testGetBooleanValueValueBoolean()
		throws Exception
	{
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.util.LiteralUtil#getCalendarValue(org.openrdf.model.Literal, javax.xml.datatype.XMLGregorianCalendar)}
	 * .
	 */
	@Ignore
	@Test
	public final void testGetCalendarValueLiteralXMLGregorianCalendar()
		throws Exception
	{
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.util.LiteralUtil#getCalendarValue(org.openrdf.model.Value, javax.xml.datatype.XMLGregorianCalendar)}
	 * .
	 */
	@Ignore
	@Test
	public final void testGetCalendarValueValueXMLGregorianCalendar()
		throws Exception
	{
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.util.LiteralUtil#getLocale(org.openrdf.model.Literal, java.util.Locale)}
	 * .
	 */
	@Ignore
	@Test
	public final void testGetLocale()
		throws Exception
	{
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.util.LiteralUtil#createLiteral(org.openrdf.model.ValueFactory, java.lang.Object)}
	 * .
	 */
	@Test
	public void testCreateLiteralObjectNull()
		throws Exception
	{

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
	public void testCreateLiteralObjectBoolean()
		throws Exception
	{

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
	public void testCreateLiteralObjectByte()
		throws Exception
	{

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
	public void testCreateLiteralObjectDouble()
		throws Exception
	{

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
	public void testCreateLiteralObjectFloat()
		throws Exception
	{

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
	public void testCreateLiteralObjectInteger()
		throws Exception
	{

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
	public void testCreateLiteralObjectLong()
		throws Exception
	{

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
	public void testCreateLiteralObjectShort()
		throws Exception
	{

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
	public void testCreateLiteralObjectXMLGregorianCalendar()
		throws Exception
	{

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
	public void testCreateLiteralObjectDate()
		throws Exception
	{

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
	public void testCreateLiteralObjectString()
		throws Exception
	{

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
	public void testCreateLiteralObjectObject()
		throws Exception
	{

		Object obj = new Object();
		Literal l = LiteralUtil.createLiteral(ValueFactoryImpl.getInstance(), obj);
		assertNotNull(l);
		assertEquals(l.getDatatype(), XMLSchema.STRING);

	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.util.LiteralUtil#createLiteralOrFail(org.openrdf.model.ValueFactory, java.lang.Object)}
	 * .
	 */
	@Test
	public void testCreateLiteralOrFailObjectNull()
		throws Exception
	{

		Object obj = null;
		try {
			LiteralUtil.createLiteralOrFail(ValueFactoryImpl.getInstance(), obj);
			fail("Did not find expected exception");
		}
		catch (NullPointerException npe) {
			assertTrue(npe.getMessage().contains("Cannot create a literal from a null"));
		}
	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.util.LiteralUtil#createLiteralOrFail(org.openrdf.model.ValueFactory, java.lang.Object)}
	 * .
	 */
	@Test
	public void testCreateLiteralOrFailObjectBoolean()
		throws Exception
	{

		Object obj = Boolean.TRUE;
		Literal l = LiteralUtil.createLiteralOrFail(ValueFactoryImpl.getInstance(), obj);
		assertNotNull(l);
		assertEquals(l.getDatatype(), XMLSchema.BOOLEAN);
		assertTrue(l.booleanValue());

	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.util.LiteralUtil#createLiteralOrFail(org.openrdf.model.ValueFactory, java.lang.Object)}
	 * .
	 */
	@Test
	public void testCreateLiteralOrFailObjectByte()
		throws Exception
	{

		Object obj = new Integer(42).byteValue();
		Literal l = LiteralUtil.createLiteralOrFail(ValueFactoryImpl.getInstance(), obj);
		assertNotNull(l);
		assertEquals(l.getDatatype(), XMLSchema.BYTE);
		assertEquals(l.getLabel(), "42");

	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.util.LiteralUtil#createLiteralOrFail(org.openrdf.model.ValueFactory, java.lang.Object)}
	 * .
	 */
	@Test
	public void testCreateLiteralOrFailObjectDouble()
		throws Exception
	{

		Object obj = new Double(42);
		Literal l = LiteralUtil.createLiteralOrFail(ValueFactoryImpl.getInstance(), obj);
		assertNotNull(l);
		assertEquals(l.getDatatype(), XMLSchema.DOUBLE);
		assertEquals(l.getLabel(), "42.0");

	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.util.LiteralUtil#createLiteralOrFail(org.openrdf.model.ValueFactory, java.lang.Object)}
	 * .
	 */
	@Test
	public void testCreateLiteralOrFailObjectFloat()
		throws Exception
	{

		Object obj = new Float(42);
		Literal l = LiteralUtil.createLiteralOrFail(ValueFactoryImpl.getInstance(), obj);
		assertNotNull(l);
		assertEquals(l.getDatatype(), XMLSchema.FLOAT);
		assertEquals(l.getLabel(), "42.0");

	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.util.LiteralUtil#createLiteralOrFail(org.openrdf.model.ValueFactory, java.lang.Object)}
	 * .
	 */
	@Test
	public void testCreateLiteralOrFailObjectInteger()
		throws Exception
	{

		Object obj = new Integer(4);
		Literal l = LiteralUtil.createLiteralOrFail(ValueFactoryImpl.getInstance(), obj);
		assertNotNull(l);
		assertEquals(l.getDatatype(), XMLSchema.INT);
		assertEquals(l.getLabel(), "4");

	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.util.LiteralUtil#createLiteralOrFail(org.openrdf.model.ValueFactory, java.lang.Object)}
	 * .
	 */
	@Test
	public void testCreateLiteralOrFailObjectLong()
		throws Exception
	{

		Object obj = new Long(42);
		Literal l = LiteralUtil.createLiteralOrFail(ValueFactoryImpl.getInstance(), obj);
		assertNotNull(l);
		assertEquals(l.getDatatype(), XMLSchema.LONG);
		assertEquals(l.getLabel(), "42");

	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.util.LiteralUtil#createLiteralOrFail(org.openrdf.model.ValueFactory, java.lang.Object)}
	 * .
	 */
	@Test
	public void testCreateLiteralOrFailObjectShort()
		throws Exception
	{

		Object obj = Short.parseShort("42");
		Literal l = LiteralUtil.createLiteralOrFail(ValueFactoryImpl.getInstance(), obj);
		assertNotNull(l);
		assertEquals(l.getDatatype(), XMLSchema.SHORT);
		assertEquals("42", l.getLabel());

	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.util.LiteralUtil#createLiteralOrFail(org.openrdf.model.ValueFactory, java.lang.Object)}
	 * .
	 */
	@Test
	public void testCreateLiteralOrFailObjectXMLGregorianCalendar()
		throws Exception
	{

		GregorianCalendar c = new GregorianCalendar();
		c.setTime(new Date());
		try {
			Object obj = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
			Literal l = LiteralUtil.createLiteralOrFail(ValueFactoryImpl.getInstance(), obj);
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
	 * {@link org.openrdf.model.util.LiteralUtil#createLiteralOrFail(org.openrdf.model.ValueFactory, java.lang.Object)}
	 * .
	 */
	@Test
	public void testCreateLiteralOrFailObjectDate()
		throws Exception
	{

		Object obj = new Date();
		Literal l = LiteralUtil.createLiteralOrFail(ValueFactoryImpl.getInstance(), obj);
		assertNotNull(l);
		assertEquals(l.getDatatype(), XMLSchema.DATETIME);

	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.util.LiteralUtil#createLiteralOrFail(org.openrdf.model.ValueFactory, java.lang.Object)}
	 * .
	 */
	@Test
	public void testCreateLiteralOrFailObjectString()
		throws Exception
	{

		Object obj = "random unique string";
		Literal l = LiteralUtil.createLiteralOrFail(ValueFactoryImpl.getInstance(), obj);
		assertNotNull(l);
		assertEquals(l.getDatatype(), XMLSchema.STRING);
		assertEquals(l.getLabel(), "random unique string");

	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.util.LiteralUtil#createLiteralOrFail(org.openrdf.model.ValueFactory, java.lang.Object)}
	 * .
	 */
	@Test
	public void testCreateLiteralOrFailObjectObject()
		throws Exception
	{

		Object obj = new Object();
		try {
			Literal l = LiteralUtil.createLiteralOrFail(ValueFactoryImpl.getInstance(), obj);
			fail("Did not receive expected exception");
		}
		catch (LiteralUtilException e) {
			assertTrue(e.getMessage().contains("Did not recognise object when creating literal"));
		}
	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.util.LiteralUtil#createLiteral(org.openrdf.model.ValueFactory, java.lang.Object)}
	 * .
	 */
	@Test
	public void testCanCreateLiteralObjectNull()
		throws Exception
	{

		Object obj = null;
		assertFalse(LiteralUtil.canCreateLiteral(obj));
	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.util.LiteralUtil#canCreateLiteral(Object)} .
	 */
	@Test
	public void testCanCreateLiteralObjectBoolean()
		throws Exception
	{

		Object obj = Boolean.TRUE;
		assertTrue(LiteralUtil.canCreateLiteral(obj));

	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.util.LiteralUtil#canCreateLiteral(Object)} .
	 */
	@Test
	public void testCanCreateLiteralObjectByte()
		throws Exception
	{

		Object obj = new Integer(42).byteValue();
		assertTrue(LiteralUtil.canCreateLiteral(obj));

	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.util.LiteralUtil#canCreateLiteral(Object)} .
	 */
	@Test
	public void testCanCreateLiteralObjectDouble()
		throws Exception
	{

		Object obj = new Double(42);
		assertTrue(LiteralUtil.canCreateLiteral(obj));

	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.util.LiteralUtil#canCreateLiteral(Object)} .
	 */
	@Test
	public void testCanCreateLiteralObjectFloat()
		throws Exception
	{

		Object obj = new Float(42);
		assertTrue(LiteralUtil.canCreateLiteral(obj));

	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.util.LiteralUtil#canCreateLiteral(Object)} .
	 */
	@Test
	public void testCanCreateLiteralObjectInteger()
		throws Exception
	{

		Object obj = new Integer(4);
		assertTrue(LiteralUtil.canCreateLiteral(obj));

	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.util.LiteralUtil#canCreateLiteral(Object)} .
	 */
	@Test
	public void testCanCreateLiteralObjectLong()
		throws Exception
	{

		Object obj = new Long(42);
		assertTrue(LiteralUtil.canCreateLiteral(obj));

	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.util.LiteralUtil#canCreateLiteral(Object)} .
	 */
	@Test
	public void testCanCreateLiteralObjectShort()
		throws Exception
	{

		Object obj = Short.parseShort("42");
		assertTrue(LiteralUtil.canCreateLiteral(obj));

	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.util.LiteralUtil#canCreateLiteral(Object)} .
	 */
	@Test
	public void testCanCreateLiteralObjectXMLGregorianCalendar()
		throws Exception
	{

		GregorianCalendar c = new GregorianCalendar();
		c.setTime(new Date());
		Object obj = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
		assertTrue(LiteralUtil.canCreateLiteral(obj));

	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.util.LiteralUtil#canCreateLiteral(Object)} .
	 */
	@Test
	public void testCanCreateLiteralObjectDate()
		throws Exception
	{

		Object obj = new Date();
		assertTrue(LiteralUtil.canCreateLiteral(obj));

	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.util.LiteralUtil#canCreateLiteral(Object)} .
	 */
	@Test
	public void testCanCreateLiteralObjectString()
		throws Exception
	{

		Object obj = "random unique string";
		assertTrue(LiteralUtil.canCreateLiteral(obj));

	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.util.LiteralUtil#canCreateLiteral(Object)} .
	 */
	@Test
	public void testCanCreateLiteralObjectObject()
		throws Exception
	{

		Object obj = new Object();
		assertFalse(LiteralUtil.canCreateLiteral(obj));

	}

}
