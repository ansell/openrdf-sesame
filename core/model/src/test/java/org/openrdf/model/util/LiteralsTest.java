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
package org.openrdf.model.util;

import static org.junit.Assert.*;

import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

import org.junit.Ignore;
import org.junit.Test;

import org.openrdf.model.Literal;
import org.openrdf.model.impl.SimpleValueFactory;
import org.openrdf.model.vocabulary.XMLSchema;

/**
 * Tests for {@link Literals}.
 * 
 * @author Peter Ansell
 */
public class LiteralsTest {

	/**
	 * Test method for
	 * {@link org.openrdf.model.util.Literals#getLabel(org.openrdf.model.Literal, java.lang.String)}
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
	 * {@link org.openrdf.model.util.Literals#getLabel(org.openrdf.model.Value, java.lang.String)}
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
	 * {@link org.openrdf.model.util.Literals#getByteValue(org.openrdf.model.Literal, byte)}
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
	 * {@link org.openrdf.model.util.Literals#getByteValue(org.openrdf.model.Value, byte)}
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
	 * {@link org.openrdf.model.util.Literals#getShortValue(org.openrdf.model.Literal, short)}
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
	 * {@link org.openrdf.model.util.Literals#getShortValue(org.openrdf.model.Value, short)}
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
	 * {@link org.openrdf.model.util.Literals#getIntValue(org.openrdf.model.Literal, int)}
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
	 * {@link org.openrdf.model.util.Literals#getIntValue(org.openrdf.model.Value, int)}
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
	 * {@link org.openrdf.model.util.Literals#getLongValue(org.openrdf.model.Literal, long)}
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
	 * {@link org.openrdf.model.util.Literals#getLongValue(org.openrdf.model.Value, long)}
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
	 * {@link org.openrdf.model.util.Literals#getIntegerValue(org.openrdf.model.Literal, java.math.BigInteger)}
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
	 * {@link org.openrdf.model.util.Literals#getIntegerValue(org.openrdf.model.Value, java.math.BigInteger)}
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
	 * {@link org.openrdf.model.util.Literals#getDecimalValue(org.openrdf.model.Literal, java.math.BigDecimal)}
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
	 * {@link org.openrdf.model.util.Literals#getDecimalValue(org.openrdf.model.Value, java.math.BigDecimal)}
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
	 * {@link org.openrdf.model.util.Literals#getFloatValue(org.openrdf.model.Literal, float)}
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
	 * {@link org.openrdf.model.util.Literals#getFloatValue(org.openrdf.model.Value, float)}
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
	 * {@link org.openrdf.model.util.Literals#getDoubleValue(org.openrdf.model.Literal, double)}
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
	 * {@link org.openrdf.model.util.Literals#getDoubleValue(org.openrdf.model.Value, double)}
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
	 * {@link org.openrdf.model.util.Literals#getBooleanValue(org.openrdf.model.Literal, boolean)}
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
	 * {@link org.openrdf.model.util.Literals#getBooleanValue(org.openrdf.model.Value, boolean)}
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
	 * {@link org.openrdf.model.util.Literals#getCalendarValue(org.openrdf.model.Literal, javax.xml.datatype.XMLGregorianCalendar)}
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
	 * {@link org.openrdf.model.util.Literals#getCalendarValue(org.openrdf.model.Value, javax.xml.datatype.XMLGregorianCalendar)}
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
	 * {@link org.openrdf.model.util.Literals#getLocale(org.openrdf.model.Literal, java.util.Locale)}
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
	 * {@link org.openrdf.model.util.Literals#createLiteral(org.openrdf.model.ValueFactory, java.lang.Object)}
	 * .
	 */
	@Test
	public void testCreateLiteralObjectNull()
		throws Exception
	{

		Object obj = null;
		try {
			Literals.createLiteral(SimpleValueFactory.getInstance(), obj);
			fail("Did not find expected exception");
		}
		catch (NullPointerException npe) {
			assertTrue(npe.getMessage().contains("Cannot create a literal from a null"));
		}
	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.util.Literals#createLiteral(org.openrdf.model.ValueFactory, java.lang.Object)}
	 * .
	 */
	@Test
	public void testCreateLiteralObjectBoolean()
		throws Exception
	{

		Object obj = Boolean.TRUE;
		Literal l = Literals.createLiteral(SimpleValueFactory.getInstance(), obj);
		assertNotNull(l);
		assertEquals(l.getDatatype(), XMLSchema.BOOLEAN);
		assertTrue(l.booleanValue());

	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.util.Literals#createLiteral(org.openrdf.model.ValueFactory, java.lang.Object)}
	 * .
	 */
	@Test
	public void testCreateLiteralObjectByte()
		throws Exception
	{

		Object obj = new Integer(42).byteValue();
		Literal l = Literals.createLiteral(SimpleValueFactory.getInstance(), obj);
		assertNotNull(l);
		assertEquals(l.getDatatype(), XMLSchema.BYTE);
		assertEquals(l.getLabel(), "42");

	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.util.Literals#createLiteral(org.openrdf.model.ValueFactory, java.lang.Object)}
	 * .
	 */
	@Test
	public void testCreateLiteralObjectDouble()
		throws Exception
	{

		Object obj = new Double(42);
		Literal l = Literals.createLiteral(SimpleValueFactory.getInstance(), obj);
		assertNotNull(l);
		assertEquals(l.getDatatype(), XMLSchema.DOUBLE);
		assertEquals(l.getLabel(), "42.0");

	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.util.Literals#createLiteral(org.openrdf.model.ValueFactory, java.lang.Object)}
	 * .
	 */
	@Test
	public void testCreateLiteralObjectFloat()
		throws Exception
	{

		Object obj = new Float(42);
		Literal l = Literals.createLiteral(SimpleValueFactory.getInstance(), obj);
		assertNotNull(l);
		assertEquals(l.getDatatype(), XMLSchema.FLOAT);
		assertEquals(l.getLabel(), "42.0");

	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.util.Literals#createLiteral(org.openrdf.model.ValueFactory, java.lang.Object)}
	 * .
	 */
	@Test
	public void testCreateLiteralObjectInteger()
		throws Exception
	{

		Object obj = new Integer(4);
		Literal l = Literals.createLiteral(SimpleValueFactory.getInstance(), obj);
		assertNotNull(l);
		assertEquals(l.getDatatype(), XMLSchema.INT);
		assertEquals(l.getLabel(), "4");

	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.util.Literals#createLiteral(org.openrdf.model.ValueFactory, java.lang.Object)}
	 * .
	 */
	@Test
	public void testCreateLiteralObjectLong()
		throws Exception
	{

		Object obj = new Long(42);
		Literal l = Literals.createLiteral(SimpleValueFactory.getInstance(), obj);
		assertNotNull(l);
		assertEquals(l.getDatatype(), XMLSchema.LONG);
		assertEquals(l.getLabel(), "42");

	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.util.Literals#createLiteral(org.openrdf.model.ValueFactory, java.lang.Object)}
	 * .
	 */
	@Test
	public void testCreateLiteralObjectShort()
		throws Exception
	{

		Object obj = Short.parseShort("42");
		Literal l = Literals.createLiteral(SimpleValueFactory.getInstance(), obj);
		assertNotNull(l);
		assertEquals(l.getDatatype(), XMLSchema.SHORT);
		assertEquals("42", l.getLabel());

	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.util.Literals#createLiteral(org.openrdf.model.ValueFactory, java.lang.Object)}
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
			Literal l = Literals.createLiteral(SimpleValueFactory.getInstance(), obj);
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
	 * {@link org.openrdf.model.util.Literals#createLiteral(org.openrdf.model.ValueFactory, java.lang.Object)}
	 * .
	 */
	@Test
	public void testCreateLiteralObjectDate()
		throws Exception
	{

		Object obj = new Date();
		Literal l = Literals.createLiteral(SimpleValueFactory.getInstance(), obj);
		assertNotNull(l);
		assertEquals(l.getDatatype(), XMLSchema.DATETIME);

	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.util.Literals#createLiteral(org.openrdf.model.ValueFactory, java.lang.Object)}
	 * .
	 */
	@Test
	public void testCreateLiteralObjectString()
		throws Exception
	{

		Object obj = "random unique string";
		Literal l = Literals.createLiteral(SimpleValueFactory.getInstance(), obj);
		assertNotNull(l);
		assertEquals(l.getDatatype(), XMLSchema.STRING);
		assertEquals(l.getLabel(), "random unique string");

	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.util.Literals#createLiteral(org.openrdf.model.ValueFactory, java.lang.Object)}
	 * .
	 */
	@Test
	public void testCreateLiteralObjectObject()
		throws Exception
	{

		Object obj = new Object();
		Literal l = Literals.createLiteral(SimpleValueFactory.getInstance(), obj);
		assertNotNull(l);
		assertEquals(l.getDatatype(), XMLSchema.STRING);

	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.util.Literals#createLiteralOrFail(org.openrdf.model.ValueFactory, java.lang.Object)}
	 * .
	 */
	@Test
	public void testCreateLiteralOrFailObjectNull()
		throws Exception
	{

		Object obj = null;
		try {
			Literals.createLiteralOrFail(SimpleValueFactory.getInstance(), obj);
			fail("Did not find expected exception");
		}
		catch (NullPointerException npe) {
			assertTrue(npe.getMessage().contains("Cannot create a literal from a null"));
		}
	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.util.Literals#createLiteralOrFail(org.openrdf.model.ValueFactory, java.lang.Object)}
	 * .
	 */
	@Test
	public void testCreateLiteralOrFailObjectBoolean()
		throws Exception
	{

		Object obj = Boolean.TRUE;
		Literal l = Literals.createLiteralOrFail(SimpleValueFactory.getInstance(), obj);
		assertNotNull(l);
		assertEquals(l.getDatatype(), XMLSchema.BOOLEAN);
		assertTrue(l.booleanValue());

	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.util.Literals#createLiteralOrFail(org.openrdf.model.ValueFactory, java.lang.Object)}
	 * .
	 */
	@Test
	public void testCreateLiteralOrFailObjectByte()
		throws Exception
	{

		Object obj = new Integer(42).byteValue();
		Literal l = Literals.createLiteralOrFail(SimpleValueFactory.getInstance(), obj);
		assertNotNull(l);
		assertEquals(l.getDatatype(), XMLSchema.BYTE);
		assertEquals(l.getLabel(), "42");

	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.util.Literals#createLiteralOrFail(org.openrdf.model.ValueFactory, java.lang.Object)}
	 * .
	 */
	@Test
	public void testCreateLiteralOrFailObjectDouble()
		throws Exception
	{

		Object obj = new Double(42);
		Literal l = Literals.createLiteralOrFail(SimpleValueFactory.getInstance(), obj);
		assertNotNull(l);
		assertEquals(l.getDatatype(), XMLSchema.DOUBLE);
		assertEquals(l.getLabel(), "42.0");

	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.util.Literals#createLiteralOrFail(org.openrdf.model.ValueFactory, java.lang.Object)}
	 * .
	 */
	@Test
	public void testCreateLiteralOrFailObjectFloat()
		throws Exception
	{

		Object obj = new Float(42);
		Literal l = Literals.createLiteralOrFail(SimpleValueFactory.getInstance(), obj);
		assertNotNull(l);
		assertEquals(l.getDatatype(), XMLSchema.FLOAT);
		assertEquals(l.getLabel(), "42.0");

	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.util.Literals#createLiteralOrFail(org.openrdf.model.ValueFactory, java.lang.Object)}
	 * .
	 */
	@Test
	public void testCreateLiteralOrFailObjectInteger()
		throws Exception
	{

		Object obj = new Integer(4);
		Literal l = Literals.createLiteralOrFail(SimpleValueFactory.getInstance(), obj);
		assertNotNull(l);
		assertEquals(l.getDatatype(), XMLSchema.INT);
		assertEquals(l.getLabel(), "4");

	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.util.Literals#createLiteralOrFail(org.openrdf.model.ValueFactory, java.lang.Object)}
	 * .
	 */
	@Test
	public void testCreateLiteralOrFailObjectLong()
		throws Exception
	{

		Object obj = new Long(42);
		Literal l = Literals.createLiteralOrFail(SimpleValueFactory.getInstance(), obj);
		assertNotNull(l);
		assertEquals(l.getDatatype(), XMLSchema.LONG);
		assertEquals(l.getLabel(), "42");

	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.util.Literals#createLiteralOrFail(org.openrdf.model.ValueFactory, java.lang.Object)}
	 * .
	 */
	@Test
	public void testCreateLiteralOrFailObjectShort()
		throws Exception
	{

		Object obj = Short.parseShort("42");
		Literal l = Literals.createLiteralOrFail(SimpleValueFactory.getInstance(), obj);
		assertNotNull(l);
		assertEquals(l.getDatatype(), XMLSchema.SHORT);
		assertEquals("42", l.getLabel());

	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.util.Literals#createLiteralOrFail(org.openrdf.model.ValueFactory, java.lang.Object)}
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
			Literal l = Literals.createLiteralOrFail(SimpleValueFactory.getInstance(), obj);
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
	 * {@link org.openrdf.model.util.Literals#createLiteralOrFail(org.openrdf.model.ValueFactory, java.lang.Object)}
	 * .
	 */
	@Test
	public void testCreateLiteralOrFailObjectDate()
		throws Exception
	{

		Object obj = new Date();
		Literal l = Literals.createLiteralOrFail(SimpleValueFactory.getInstance(), obj);
		assertNotNull(l);
		assertEquals(l.getDatatype(), XMLSchema.DATETIME);

	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.util.Literals#createLiteralOrFail(org.openrdf.model.ValueFactory, java.lang.Object)}
	 * .
	 */
	@Test
	public void testCreateLiteralOrFailObjectString()
		throws Exception
	{

		Object obj = "random unique string";
		Literal l = Literals.createLiteralOrFail(SimpleValueFactory.getInstance(), obj);
		assertNotNull(l);
		assertEquals(l.getDatatype(), XMLSchema.STRING);
		assertEquals(l.getLabel(), "random unique string");

	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.util.Literals#createLiteralOrFail(org.openrdf.model.ValueFactory, java.lang.Object)}
	 * .
	 */
	@Test
	public void testCreateLiteralOrFailObjectObject()
		throws Exception
	{

		Object obj = new Object();
		try {
			Literal l = Literals.createLiteralOrFail(SimpleValueFactory.getInstance(), obj);
			fail("Did not receive expected exception");
		}
		catch (LiteralUtilException e) {
			assertTrue(e.getMessage().contains("Did not recognise object when creating literal"));
		}
	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.util.Literals#createLiteral(org.openrdf.model.ValueFactory, java.lang.Object)}
	 * .
	 */
	@Test
	public void testCanCreateLiteralObjectNull()
		throws Exception
	{

		Object obj = null;
		assertFalse(Literals.canCreateLiteral(obj));
	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.util.Literals#canCreateLiteral(Object)} .
	 */
	@Test
	public void testCanCreateLiteralObjectBoolean()
		throws Exception
	{

		Object obj = Boolean.TRUE;
		assertTrue(Literals.canCreateLiteral(obj));

	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.util.Literals#canCreateLiteral(Object)} .
	 */
	@Test
	public void testCanCreateLiteralObjectByte()
		throws Exception
	{

		Object obj = new Integer(42).byteValue();
		assertTrue(Literals.canCreateLiteral(obj));

	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.util.Literals#canCreateLiteral(Object)} .
	 */
	@Test
	public void testCanCreateLiteralObjectDouble()
		throws Exception
	{

		Object obj = new Double(42);
		assertTrue(Literals.canCreateLiteral(obj));

	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.util.Literals#canCreateLiteral(Object)} .
	 */
	@Test
	public void testCanCreateLiteralObjectFloat()
		throws Exception
	{

		Object obj = new Float(42);
		assertTrue(Literals.canCreateLiteral(obj));

	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.util.Literals#canCreateLiteral(Object)} .
	 */
	@Test
	public void testCanCreateLiteralObjectInteger()
		throws Exception
	{

		Object obj = new Integer(4);
		assertTrue(Literals.canCreateLiteral(obj));

	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.util.Literals#canCreateLiteral(Object)} .
	 */
	@Test
	public void testCanCreateLiteralObjectLong()
		throws Exception
	{

		Object obj = new Long(42);
		assertTrue(Literals.canCreateLiteral(obj));

	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.util.Literals#canCreateLiteral(Object)} .
	 */
	@Test
	public void testCanCreateLiteralObjectShort()
		throws Exception
	{

		Object obj = Short.parseShort("42");
		assertTrue(Literals.canCreateLiteral(obj));

	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.util.Literals#canCreateLiteral(Object)} .
	 */
	@Test
	public void testCanCreateLiteralObjectXMLGregorianCalendar()
		throws Exception
	{

		GregorianCalendar c = new GregorianCalendar();
		c.setTime(new Date());
		Object obj = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
		assertTrue(Literals.canCreateLiteral(obj));

	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.util.Literals#canCreateLiteral(Object)} .
	 */
	@Test
	public void testCanCreateLiteralObjectDate()
		throws Exception
	{

		Object obj = new Date();
		assertTrue(Literals.canCreateLiteral(obj));

	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.util.Literals#canCreateLiteral(Object)} .
	 */
	@Test
	public void testCanCreateLiteralObjectString()
		throws Exception
	{

		Object obj = "random unique string";
		assertTrue(Literals.canCreateLiteral(obj));

	}

	/**
	 * Test method for
	 * {@link org.openrdf.model.util.Literals#canCreateLiteral(Object)} .
	 */
	@Test
	public void testCanCreateLiteralObjectObject()
		throws Exception
	{

		Object obj = new Object();
		assertFalse(Literals.canCreateLiteral(obj));

	}

}
