/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.model.impl;

import java.math.BigDecimal;
import java.math.BigInteger;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;

import org.openrdf.model.Literal;
import org.openrdf.model.LiteralFactory;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.datatypes.XMLDatatypeUtil;
import org.openrdf.model.vocabulary.XMLSchema;

/**
 * implements {@link LiteralFactory} creating literals for basic types by
 * calling the generic {@link ValueFactory#createLiteral(String, URI)} with the
 * appropriate value and datatype.
 * 
 * @author Arjohn Kampman
 * @author James Leigh
 */
public class LiteralFactoryImpl implements LiteralFactory {

	/*---------*
	 * Methods *
	 *---------*/

	public Literal createLiteral(String label) {
		return new LiteralImpl(label);
	}

	public Literal createLiteral(String label, String language) {
		return new LiteralImpl(label, language);
	}

	public Literal createLiteral(String label, URI datatype) {
		return new LiteralImpl(label, datatype);
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
	 * Calls {@link #createIntegerLiteral} with the supplied value and
	 * {@link XMLSchema#BYTE} as parameters.
	 */
	public Literal createLiteral(byte value) {
		return createIntegerLiteral(value, XMLSchema.BYTE);
	}

	/**
	 * Calls {@link #createIntegerLiteral} with the supplied value and
	 * {@link XMLSchema#SHORT} as parameters.
	 */
	public Literal createLiteral(short value) {
		return createIntegerLiteral(value, XMLSchema.SHORT);
	}

	/**
	 * Calls {@link #createIntegerLiteral} with the supplied value and
	 * {@link XMLSchema#INT} as parameters.
	 */
	public Literal createLiteral(int value) {
		return createIntegerLiteral(value, XMLSchema.INT);
	}

	/**
	 * Calls {@link #createIntegerLiteral} with the supplied value and
	 * {@link XMLSchema#LONG} as parameters.
	 */
	public Literal createLiteral(long value) {
		return createIntegerLiteral(value, XMLSchema.LONG);
	}

	/**
	 * Calls {@link #createIntegerLiteral} with the supplied value and
	 * {@link XMLSchema#INTEGER} as parameters.
	 */
	public Literal createLiteral(BigInteger value) {
		return createIntegerLiteral(value, XMLSchema.INTEGER);
	}

	/**
	 * Calls {@link #createNumericLiteral} with the supplied value
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
	 * Calls {@link #createFPLiteral(Number, URI)} with the supplied value and
	 * {@link XMLSchema#DECIMAL} as parameters.
	 */
	public Literal createLiteral(BigDecimal value) {
		return createFPLiteral(value, XMLSchema.DECIMAL);
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
	 * String-value of the supplied duration and the appropriate datatype as
	 * parameters. If only one or both of the year and month field are set, then
	 * return a {@link XMLSchema#DURATION_YEARMONTH} literal. If year and month
	 * are not set, then return a {@link XMLSchema#DURATION_DAYTIME} literal.
	 * Otherwise return a {@link XMLSchema#DURATION} literal.
	 * 
	 * @see Duration#toString()
	 * @see Duration#isSet(DatatypeConstants.Field)
	 */
	public Literal createLiteral(Duration duration) {
		boolean yearSet = duration.isSet(DatatypeConstants.YEARS);
		boolean monthSet = duration.isSet(DatatypeConstants.MONTHS);
		if (!yearSet && !monthSet) {
			return createLiteral(duration.toString(), XMLSchema.DURATION_DAYTIME);
		}

		boolean daySet = duration.isSet(DatatypeConstants.DAYS);
		boolean hourSet = duration.isSet(DatatypeConstants.HOURS);
		boolean minuteSet = duration.isSet(DatatypeConstants.MINUTES);
		boolean secondSet = duration.isSet(DatatypeConstants.SECONDS);
		if (!daySet && !hourSet && !minuteSet && !secondSet) {
			return createLiteral(duration.toString(), XMLSchema.DURATION_YEARMONTH);
		}

		return createLiteral(duration.toString(), XMLSchema.DURATION);
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
}
