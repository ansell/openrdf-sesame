/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.model;

import javax.xml.datatype.XMLGregorianCalendar;

/**
 * A factory for creating  literals.
 * 
 * @author Arjohn Kampman
 */
public interface LiteralFactory {

	/**
	 * Creates a new literal with the supplied label.
	 * 
	 * @param label
	 *        The literal's label.
	 */
	public Literal createLiteral(String label);

	/**
	 * Creates a new literal with the supplied label and language attribute.
	 * 
	 * @param label
	 *        The literal's label.
	 * @param language
	 *        The literal's language attribute, or <tt>null</tt> if the literal
	 *        doesn't have a language.
	 */
	public Literal createLiteral(String label, String language);

	/**
	 * Creates a new literal with the supplied label and datatype.
	 * 
	 * @param label
	 *        The literal's label.
	 * @param datatype
	 *        The literal's datatype, or <tt>null</tt> if the literal doesn't
	 *        have a datatype.
	 */
	public Literal createLiteral(String label, URI datatype);

	/**
	 * Creates a new <tt>xsd:boolean</tt>-typed literal representing the
	 * specified value.
	 * 
	 * @param value
	 *        The value for the literal.
	 * @return An <tt>xsd:boolean</tt>-typed literal for the specified value.
	 */
	public Literal createLiteral(boolean value);

	/**
	 * Creates a new <tt>xsd:byte</tt>-typed literal representing the
	 * specified value.
	 * 
	 * @param value
	 *        The value for the literal.
	 * @return An <tt>xsd:byte</tt>-typed literal for the specified value.
	 */
	public Literal createLiteral(byte value);

	/**
	 * Creates a new <tt>xsd:short</tt>-typed literal representing the
	 * specified value.
	 * 
	 * @param value
	 *        The value for the literal.
	 * @return An <tt>xsd:short</tt>-typed literal for the specified value.
	 */
	public Literal createLiteral(short value);

	/**
	 * Creates a new <tt>xsd:int</tt>-typed literal representing the specified
	 * value.
	 * 
	 * @param value
	 *        The value for the literal.
	 * @return An <tt>xsd:int</tt>-typed literal for the specified value.
	 */
	public Literal createLiteral(int value);

	/**
	 * Creates a new <tt>xsd:long</tt>-typed literal representing the
	 * specified value.
	 * 
	 * @param value
	 *        The value for the literal.
	 * @return An <tt>xsd:long</tt>-typed literal for the specified value.
	 */
	public Literal createLiteral(long value);

	/**
	 * Creates a new <tt>xsd:float</tt>-typed literal representing the
	 * specified value.
	 * 
	 * @param value
	 *        The value for the literal.
	 * @return An <tt>xsd:float</tt>-typed literal for the specified value.
	 */
	public Literal createLiteral(float value);

	/**
	 * Creates a new <tt>xsd:double</tt>-typed literal representing the
	 * specified value.
	 * 
	 * @param value
	 *        The value for the literal.
	 * @return An <tt>xsd:double</tt>-typed literal for the specified value.
	 */
	public Literal createLiteral(double value);

	/**
	 * Creates a new literal representing the specified calendar that is typed
	 * using the appropriate XML Schema date/time datatype.
	 * 
	 * @param calendar
	 *        The value for the literal.
	 * @return An typed literal for the specified calendar.
	 */
	public Literal createLiteral(XMLGregorianCalendar calendar);
}
