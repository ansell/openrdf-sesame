/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2012.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.model;

import static org.junit.Assert.*;

import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.junit.Before;
import org.junit.Test;

import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.XMLSchema;


/**
 *
 * @author jeen
 */
public class ValueFactoryTest {

	private ValueFactory f;
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp()
		throws Exception
	{
		f = ValueFactoryImpl.getInstance();
	}

	/**
	 * Test method for {@link org.openrdf.model.impl.ValueFactoryBase#createBNode()}.
	 */
	@Test
	public void testCreateBNode() {
		BNode b = f.createBNode();
		assertNotNull(b);
		assertNotNull(b.getID());
	}

	/**
	 * Test method for {@link org.openrdf.model.impl.ValueFactoryBase#createLiteral(boolean)}.
	 */
	@Test
	public void testCreateLiteralBoolean() {
		Literal l = f.createLiteral(true);
		assertNotNull(l);
		assertEquals("true", l.getLabel());
		assertEquals(l.getDatatype(), XMLSchema.BOOLEAN);
		
		l = f.createLiteral(false);
		assertNotNull(l);
		assertEquals("false", l.getLabel());
		assertEquals(l.getDatatype(), XMLSchema.BOOLEAN);
	}

	/**
	 * Test method for {@link org.openrdf.model.impl.ValueFactoryBase#createLiteral(byte)}.
	 */
	@Test
	public void testCreateLiteralByte() {
		byte b = new Integer(42).byteValue();
		
		Literal l = f.createLiteral(b);
		assertNotNull(l);
		assertEquals("42", l.getLabel());
		assertEquals(l.getDatatype(), XMLSchema.BYTE);
	}

	/**
	 * Test method for {@link org.openrdf.model.impl.ValueFactoryBase#createLiteral(short)}.
	 */
	@Test
	public void testCreateLiteralShort() {
		
		short s = Short.parseShort("42");

		Literal l = f.createLiteral(s);
		assertNotNull(l);
		assertEquals("42", l.getLabel());
		assertEquals(l.getDatatype(), XMLSchema.SHORT);
	}

	/**
	 * Test method for {@link org.openrdf.model.impl.ValueFactoryBase#createLiteral(int)}.
	 */
	@Test
	public void testCreateLiteralInt() {
		Literal l = f.createLiteral(42);
		assertNotNull(l);
		assertEquals("42", l.getLabel());
		assertEquals(l.getDatatype(), XMLSchema.INT);
	}

	/**
	 * Test method for {@link org.openrdf.model.impl.ValueFactoryBase#createLiteral(long)}.
	 */
	@Test
	public void testCreateLiteralLong() {
		Literal l = f.createLiteral(42L);
		assertNotNull(l);
		assertEquals("42", l.getLabel());
		assertEquals(l.getDatatype(), XMLSchema.LONG);
	}

	/**
	 * Test method for {@link org.openrdf.model.impl.ValueFactoryBase#createLiteral(float)}.
	 */
	@Test
	public void testCreateLiteralFloat() {
		
		Literal l = f.createLiteral(42.0f);
		assertNotNull(l);
		assertEquals("42.0", l.getLabel());
		assertEquals(l.getDatatype(), XMLSchema.FLOAT);

	}

	/**
	 * Test method for {@link org.openrdf.model.impl.ValueFactoryBase#createLiteral(double)}.
	 */
	@Test
	public void testCreateLiteralDouble() {
		Literal l = f.createLiteral(42.0d);
		assertNotNull(l);
		assertEquals("42.0", l.getLabel());
		assertEquals(l.getDatatype(), XMLSchema.DOUBLE);
	}

	/**
	 * Test method for {@link org.openrdf.model.impl.ValueFactoryBase#createLiteral(javax.xml.datatype.XMLGregorianCalendar)}.
	 */
	@Test
	public void testCreateLiteralXMLGregorianCalendar() {
		GregorianCalendar c = new GregorianCalendar();
		c.setTime(new Date());
		try {
			XMLGregorianCalendar xmlGregCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
			Literal l = f.createLiteral(xmlGregCalendar);
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
	 * Test method for {@link org.openrdf.model.impl.ValueFactoryBase#createLiteral(java.util.Date)}.
	 */
	@Test
	public void testCreateLiteralDate() {
		Literal l = f.createLiteral(new Date());
		assertNotNull(l);
		assertEquals(l.getDatatype(), XMLSchema.DATETIME);
		
	}

	/**
	 * Test method for {@link org.openrdf.model.impl.ValueFactoryBase#createLiteral(java.lang.Object)}.
	 */
	@Test
	public void testCreateLiteralObject() {
		
		Object obj = new Integer(4);
		
		Literal l = f.createLiteral(obj);
		assertNotNull(l);
		assertEquals(l.getDatatype(), XMLSchema.INT);
		
		obj = "some string";
		l = f.createLiteral(obj);
		assertNotNull(l);
		assertEquals(l.getDatatype(), XMLSchema.STRING);
		
	}

}
