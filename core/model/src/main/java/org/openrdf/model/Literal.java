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
package org.openrdf.model;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Optional;

import javax.xml.datatype.XMLGregorianCalendar;

/**
 * An RDF literal consisting of a label (the lexical value), a datatype, and optionally a language
 * tag.
 * 
 * @author Arjohn Kampman
 */
public interface Literal extends Value {

	/**
	 * Gets the label (the lexical value) of this literal.
	 * 
	 * @return The literal's label.
	 */
	public String getLabel();

	/**
	 * Gets the language tag for this literal, normalized to lower case.
	 * 
	 * @return The language tag for this literal, or {@link Optional#empty()} if
	 *         it doesn't have one.
	 */
	public Optional<String> getLanguage();

	/**
	 * Gets the datatype for this literal.
	 * 
	 * @return The datatype for this literal. If {@link #getLanguage()} returns a
	 *         non-empty value than this must return {@link RDF#LANGSTRING}.
	 */
	public IRI getDatatype();

	/**
	 * Compares a literal object to another object.
	 * 
	 * @param other
	 *        The object to compare this literal to.
	 * @return <tt>true</tt> if the other object is an instance of
	 *         {@link Literal} and if their labels, language tags and datatypes
	 *         are equal.
	 */
	@Override
	public boolean equals(Object other);

	/**
	 * Returns the literal's hash code. The hash code of a literal is defined as
	 * the hash code of its label: <tt>label.hashCode()</tt>.
	 * 
	 * @return A hash code for the literal.
	 */
	@Override
	public int hashCode();

	/**
	 * Returns the <tt>byte</tt> value of this literal.
	 * 
	 * @return The <tt>byte value of the literal.
	 * @throws NumberFormatException
	 *         If the literal cannot be represented by a <tt>byte</tt>.
	 */
	public byte byteValue();

	/**
	 * Returns the <tt>short</tt> value of this literal.
	 * 
	 * @return The <tt>short</tt> value of the literal.
	 * @throws NumberFormatException
	 *         If the literal's label cannot be represented by a <tt>short</tt>.
	 */
	public short shortValue();

	/**
	 * Returns the <tt>int</tt> value of this literal.
	 * 
	 * @return The <tt>int</tt> value of the literal.
	 * @throws NumberFormatException
	 *         If the literal's label cannot be represented by a <tt>int</tt>.
	 */
	public int intValue();

	/**
	 * Returns the <tt>long</tt> value of this literal.
	 * 
	 * @return The <tt>long</tt> value of the literal.
	 * @throws NumberFormatException
	 *         If the literal's label cannot be represented by to a <tt>long</tt>
	 *         .
	 */
	public long longValue();

	/**
	 * Returns the integer value of this literal.
	 * 
	 * @return The integer value of the literal.
	 * @throws NumberFormatException
	 *         If the literal's label is not a valid integer.
	 */
	public BigInteger integerValue();

	/**
	 * Returns the decimal value of this literal.
	 * 
	 * @return The decimal value of the literal.
	 * @throws NumberFormatException
	 *         If the literal's label is not a valid decimal.
	 */
	public BigDecimal decimalValue();

	/**
	 * Returns the <tt>float</tt> value of this literal.
	 * 
	 * @return The <tt>float</tt> value of the literal.
	 * @throws NumberFormatException
	 *         If the literal's label cannot be represented by a <tt>float</tt>.
	 */
	public float floatValue();

	/**
	 * Returns the <tt>double</tt> value of this literal.
	 * 
	 * @return The <tt>double</tt> value of the literal.
	 * @throws NumberFormatException
	 *         If the literal's label cannot be represented by a <tt>double</tt>.
	 */
	public double doubleValue();

	/**
	 * Returns the <tt>boolean</tt> value of this literal.
	 * 
	 * @return The <tt>long</tt> value of the literal.
	 * @throws IllegalArgumentException
	 *         If the literal's label cannot be represented by a <tt>boolean</tt>
	 *         .
	 */
	public boolean booleanValue();

	/**
	 * Returns the {@link XMLGregorianCalendar} value of this literal. A calendar
	 * representation can be given for literals whose label conforms to the
	 * syntax of the following <a href="http://www.w3.org/TR/xmlschema-2/">XML
	 * Schema datatypes</a>: <tt>dateTime</tt>, <tt>time</tt>, <tt>date</tt>,
	 * <tt>gYearMonth</tt>, <tt>gMonthDay</tt>, <tt>gYear</tt>, <tt>gMonth</tt>
	 * or <tt>gDay</tt>.
	 * 
	 * @return The calendar value of the literal.
	 * @throws IllegalArgumentException
	 *         If the literal cannot be represented by a
	 *         {@link XMLGregorianCalendar}.
	 */
	public XMLGregorianCalendar calendarValue();
}
