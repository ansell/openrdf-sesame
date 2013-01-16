/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.model.impl;

import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.datatypes.XMLDatatypeUtil;
import org.openrdf.model.vocabulary.XMLSchema;

/**
 * Abstract base class for {@link ValueFactory} implementations that implements
 * the utility methods for creating literals for basic types by calling the
 * generic {@link ValueFactory#createLiteral(String, URI)} with the appropriate
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
	 * Calls {@link ValueFactory#createLiteral(String, URI)} with the
	 * String-value of the supplied value and {@link XMLSchema#BOOLEAN} as
	 * parameters.
	 */
	public Literal createLiteral(boolean b) {
		return createLiteral(Boolean.toString(b), XMLSchema.BOOLEAN);
	}

	/**
	 * Calls {@link #createIntegerLiteral(Number, URI)} with the supplied value
	 * and {@link XMLSchema#BYTE} as parameters.
	 */
	public Literal createLiteral(byte value) {
		return createIntegerLiteral(value, XMLSchema.BYTE);
	}

	/**
	 * Calls {@link #createIntegerLiteral(Number, URI)} with the supplied value
	 * and {@link XMLSchema#SHORT} as parameters.
	 */
	public Literal createLiteral(short value) {
		return createIntegerLiteral(value, XMLSchema.SHORT);
	}

	/**
	 * Calls {@link #createIntegerLiteral(Number, URI)} with the supplied value
	 * and {@link XMLSchema#INT} as parameters.
	 */
	public Literal createLiteral(int value) {
		return createIntegerLiteral(value, XMLSchema.INT);
	}

	/**
	 * Calls {@link #createIntegerLiteral(Number, URI)} with the supplied value
	 * and {@link XMLSchema#LONG} as parameters.
	 */
	public Literal createLiteral(long value) {
		return createIntegerLiteral(value, XMLSchema.LONG);
	}

	/**
	 * Calls {@link #createNumericLiteral(Number, URI)} with the supplied value
	 * and datatype as parameters.
	 */
	protected Literal createIntegerLiteral(Number value, URI datatype) {
		return createNumericLiteral(value, datatype);
	}

	/**
	 * Calls {@link #createFPLiteral(Number, URI)} with the supplied value and
	 * {@link XMLSchema#FLOAT} as parameters.
	 */
	public Literal createLiteral(float value) {
		return createFPLiteral(value, XMLSchema.FLOAT);
	}

	/**
	 * Calls {@link #createFPLiteral(Number, URI)} with the supplied value and
	 * {@link XMLSchema#DOUBLE} as parameters.
	 */
	public Literal createLiteral(double value) {
		return createFPLiteral(value, XMLSchema.DOUBLE);
	}

	/**
	 * Calls {@link #createNumericLiteral(Number, URI)} with the supplied value
	 * and datatype as parameters.
	 */
	protected Literal createFPLiteral(Number value, URI datatype) {
		return createNumericLiteral(value, datatype);
	}

	/**
	 * Calls {@link ValueFactory#createLiteral(String, URI)} with the
	 * String-value of the supplied number and the supplied datatype as
	 * parameters.
	 */
	protected Literal createNumericLiteral(Number number, URI datatype) {
		return createLiteral(number.toString(), datatype);
	}

	/**
	 * Calls {@link ValueFactory#createLiteral(String, URI)} with the
	 * String-value of the supplied calendar and the appropriate datatype as
	 * parameters.
	 * 
	 * @see XMLGregorianCalendar#toXMLFormat()
	 * @see XMLGregorianCalendar#getXMLSchemaType()
	 * @see XMLDatatypeUtil#qnameToURI(javax.xml.namespace.QName)
	 */
	public Literal createLiteral(XMLGregorianCalendar calendar) {
		return createLiteral(calendar.toXMLFormat(), XMLDatatypeUtil.qnameToURI(calendar.getXMLSchemaType()));
	}

	/**
	 * Converts the supplied {@link Date} to a {@link XMLGregorianCalendar}, then
	 * calls {@link ValueFactory#createLiteral(XMLGregorianCalendar)}.
	 * 
	 * @since 2.7.0
	 */
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

	/**
	 * Creates a typed {@link Literal} out of the supplied object, mapping the runtime
	 * type of the object to the appropriate XML Schema type. If no mapping
	 * is available, the method returns a literal with the string representation
	 * of the supplied object as the value, and {@link XMLSchema#STRING} as the
	 * datatype. Recognized types are {@link Boolean}, {@link Byte},
	 * {@link Double}, {@link Float}, {@link Integer}, {@link Long},
	 * {@link Short}, {@link XMLGregorianCalendar }, and {@link Date}.
	 * 
	 * @since 2.7.0
	 * @param object
	 *        an object to be converted to a typed literal.
	 * @return a typed literal representation of the supplied object.
	 */
	public Literal createLiteral(Object object) {
		if (object instanceof Boolean) {
			return createLiteral(((Boolean)object).booleanValue());
		}
		else if (object instanceof Byte) {
			return createLiteral(((Byte)object).byteValue());
		}
		else if (object instanceof Double) {
			return createLiteral(((Double)object).doubleValue());
		}
		else if (object instanceof Float) {
			return createLiteral(((Float)object).floatValue());
		}
		else if (object instanceof Integer) {
			return createLiteral(((Integer)object).intValue());
		}
		else if (object instanceof Long) {
			return createLiteral(((Long)object).longValue());
		}
		else if (object instanceof Short) {
			return createLiteral(((Short)object).shortValue());
		}
		else if (object instanceof XMLGregorianCalendar) {
			return createLiteral((XMLGregorianCalendar)object);
		}
		else if (object instanceof Date) {
			return createLiteral((Date)object);
		}
		else {
			return createLiteral(object.toString(), XMLSchema.STRING);
		}
	}
}
