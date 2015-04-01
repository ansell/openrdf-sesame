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

import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.IRI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.datatypes.XMLDatatypeUtil;
import org.openrdf.model.vocabulary.XMLSchema;

/**
 * Abstract base class for {@link ValueFactory} implementations that implements
 * the utility methods for creating literals for basic types by calling the
 * generic {@link ValueFactory#createLiteral(String, IRI)} with the appropriate
 * value and datatype.
 * 
 * @author Arjohn Kampman
 */
public abstract class ValueFactoryBase implements ValueFactory {

	/**
	 * "universal" ID for bnode prefixes to prevent blank node clashes (unique
	 * per classloaded instance of this class)
	 */
	private static long lastBNodePrefixUID = 0;

	private static synchronized long getNextBNodePrefixUid() {
		return lastBNodePrefixUID = Math.max(System.currentTimeMillis(), lastBNodePrefixUID + 1);
	}

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * The ID for the next bnode that is created.
	 */
	private int nextBNodeID;

	/**
	 * The prefix for any new bnode IDs.
	 */
	private String bnodePrefix;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public ValueFactoryBase() {
		initBNodeParams();
	}

	/*---------*
	 * Methods *
	 *---------*/

	/**
	 * Generates a new bnode prefix and resets <tt>nextBNodeID</tt> to <tt>1</tt>
	 * .
	 */
	protected void initBNodeParams() {
		// BNode prefix is based on currentTimeMillis(). Combined with a
		// sequential number per session, this gives a unique identifier.
		bnodePrefix = "node" + Long.toString(getNextBNodePrefixUid(), 32) + "x";
		nextBNodeID = 1;
	}

	@Override
	public synchronized BNode createBNode() {
		int id = nextBNodeID++;

		BNode result = createBNode(bnodePrefix + id);

		if (id == Integer.MAX_VALUE) {
			// Start with a new bnode prefix
			initBNodeParams();
		}

		return result;
	}

	/**
	 * Calls {@link ValueFactory#createLiteral(String, IRI)} with the
	 * String-value of the supplied value and {@link XMLSchema#BOOLEAN} as
	 * parameters.
	 */
	@Override
	public Literal createLiteral(boolean b) {
		return createLiteral(Boolean.toString(b), XMLSchema.BOOLEAN);
	}

	/**
	 * Calls {@link #createIntegerLiteral(Number, IRI)} with the supplied value
	 * and {@link XMLSchema#BYTE} as parameters.
	 */
	@Override
	public Literal createLiteral(byte value) {
		return createIntegerLiteral(value, XMLSchema.BYTE);
	}

	/**
	 * Calls {@link #createIntegerLiteral(Number, IRI)} with the supplied value
	 * and {@link XMLSchema#SHORT} as parameters.
	 */
	@Override
	public Literal createLiteral(short value) {
		return createIntegerLiteral(value, XMLSchema.SHORT);
	}

	/**
	 * Calls {@link #createIntegerLiteral(Number, IRI)} with the supplied value
	 * and {@link XMLSchema#INT} as parameters.
	 */
	@Override
	public Literal createLiteral(int value) {
		return createIntegerLiteral(value, XMLSchema.INT);
	}

	/**
	 * Calls {@link #createIntegerLiteral(Number, IRI)} with the supplied value
	 * and {@link XMLSchema#LONG} as parameters.
	 */
	@Override
	public Literal createLiteral(long value) {
		return createIntegerLiteral(value, XMLSchema.LONG);
	}

	/**
	 * Calls {@link #createNumericLiteral(Number, IRI)} with the supplied value
	 * and datatype as parameters.
	 */
	protected Literal createIntegerLiteral(Number value, IRI datatype) {
		return createNumericLiteral(value, datatype);
	}

	/**
	 * Calls {@link #createFPLiteral(Number, IRI)} with the supplied value and
	 * {@link XMLSchema#FLOAT} as parameters.
	 */
	@Override
	public Literal createLiteral(float value) {
		return createFPLiteral(value, XMLSchema.FLOAT);
	}

	/**
	 * Calls {@link #createFPLiteral(Number, IRI)} with the supplied value and
	 * {@link XMLSchema#DOUBLE} as parameters.
	 */
	@Override
	public Literal createLiteral(double value) {
		return createFPLiteral(value, XMLSchema.DOUBLE);
	}

	/**
	 * Calls {@link #createNumericLiteral(Number, IRI)} with the supplied value
	 * and datatype as parameters.
	 */
	protected Literal createFPLiteral(Number value, IRI datatype) {
		return createNumericLiteral(value, datatype);
	}

	/**
	 * Calls {@link ValueFactory#createLiteral(String, IRI)} with the
	 * String-value of the supplied number and the supplied datatype as
	 * parameters.
	 */
	protected Literal createNumericLiteral(Number number, IRI datatype) {
		return createLiteral(number.toString(), datatype);
	}

	/**
	 * Calls {@link ValueFactory#createLiteral(String, IRI)} with the
	 * String-value of the supplied calendar and the appropriate datatype as
	 * parameters.
	 * 
	 * @see XMLGregorianCalendar#toXMLFormat()
	 * @see XMLGregorianCalendar#getXMLSchemaType()
	 * @see XMLDatatypeUtil#qnameToURI(javax.xml.namespace.QName)
	 */
	@Override
	public Literal createLiteral(XMLGregorianCalendar calendar) {
		return createLiteral(calendar.toXMLFormat(), XMLDatatypeUtil.qnameToURI(calendar.getXMLSchemaType()));
	}

	/**
	 * Converts the supplied {@link Date} to a {@link XMLGregorianCalendar}, then
	 * calls {@link ValueFactory#createLiteral(XMLGregorianCalendar)}.
	 * 
	 * @since 2.7.0
	 */
	@Override
	public Literal createLiteral(Date date) {
		GregorianCalendar c = new GregorianCalendar();
		c.setTime(date);
		try {
			XMLGregorianCalendar xmlGregCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
			return createLiteral(xmlGregCalendar);
		}
		catch (DatatypeConfigurationException e) {
			throw new RuntimeException("Could not instantiate javax.xml.datatype.DatatypeFactory", e);
		}
	}
}
