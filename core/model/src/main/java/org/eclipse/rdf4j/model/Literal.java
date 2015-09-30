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
