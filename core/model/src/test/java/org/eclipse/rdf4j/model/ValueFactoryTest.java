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
package org.eclipse.rdf4j.model;

import static org.junit.Assert.*;

import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.junit.Before;
import org.junit.Test;


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
		f = SimpleValueFactory.getInstance();
	}

	/**
	 * Test method for {@link org.eclipse.rdf4j.model.impl.AbstractValueFactory#createBNode()}.
	 */
	@Test
	public void testCreateBNode() {
		BNode b = f.createBNode();
		assertNotNull(b);
		assertNotNull(b.getID());
	}

	/**
	 * Test method for {@link org.eclipse.rdf4j.model.impl.AbstractValueFactory#createLiteral(boolean)}.
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
	 * Test method for {@link org.eclipse.rdf4j.model.impl.AbstractValueFactory#createLiteral(byte)}.
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
	 * Test method for {@link org.eclipse.rdf4j.model.impl.AbstractValueFactory#createLiteral(short)}.
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
	 * Test method for {@link org.eclipse.rdf4j.model.impl.AbstractValueFactory#createLiteral(int)}.
	 */
	@Test
	public void testCreateLiteralInt() {
		Literal l = f.createLiteral(42);
		assertNotNull(l);
		assertEquals("42", l.getLabel());
		assertEquals(l.getDatatype(), XMLSchema.INT);
	}

	/**
	 * Test method for {@link org.eclipse.rdf4j.model.impl.AbstractValueFactory#createLiteral(long)}.
	 */
	@Test
	public void testCreateLiteralLong() {
		Literal l = f.createLiteral(42L);
		assertNotNull(l);
		assertEquals("42", l.getLabel());
		assertEquals(l.getDatatype(), XMLSchema.LONG);
	}

	/**
	 * Test method for {@link org.eclipse.rdf4j.model.impl.AbstractValueFactory#createLiteral(float)}.
	 */
	@Test
	public void testCreateLiteralFloat() {
		
		Literal l = f.createLiteral(42.0f);
		assertNotNull(l);
		assertEquals("42.0", l.getLabel());
		assertEquals(l.getDatatype(), XMLSchema.FLOAT);

	}

	/**
	 * Test method for {@link org.eclipse.rdf4j.model.impl.AbstractValueFactory#createLiteral(double)}.
	 */
	@Test
	public void testCreateLiteralDouble() {
		Literal l = f.createLiteral(42.0d);
		assertNotNull(l);
		assertEquals("42.0", l.getLabel());
		assertEquals(l.getDatatype(), XMLSchema.DOUBLE);
	}

	/**
	 * Test method for {@link org.eclipse.rdf4j.model.impl.AbstractValueFactory#createLiteral(javax.xml.datatype.XMLGregorianCalendar)}.
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
	 * Test method for {@link org.eclipse.rdf4j.model.impl.AbstractValueFactory#createLiteral(java.util.Date)}.
	 */
	@Test
	public void testCreateLiteralDate() {
		Literal l = f.createLiteral(new Date());
		assertNotNull(l);
		assertEquals(l.getDatatype(), XMLSchema.DATETIME);
		
	}

}
