/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.model;

/**
 * An RDF literal consisting of a label (the value) and optionally a
 * language tag or a datatype (but not both).
 */
public interface Literal extends Value {

	/**
	 * Gets the label of this literal.
	 *
	 * @return The literal's label.
	 */
	public String getLabel();

	/**
	 * Gets the language tag for this literal, normalized to lower case.
	 *
	 * @return The language tag for this literal, or <tt>null</tt>
	 * if it doesn't have one.
	 */
	public String getLanguage();

	/**
	 * Gets the datatype for this literal.
	 *
	 * @return The datatype for this literal, or <tt>null</tt>
	 * if it doesn't have one.
	 */
	public URI getDatatype();
	
	/**
	 * Compares a literal object to another object.
	 * 
	 * @param other The object to compare this literal to.
	 * @return <tt>true</tt> if the other object is an instance of
	 * {@link Literal} and if their labels, language tags and datatypes are
	 * equal.
	 */
	public boolean equals(Object other);
	
	/**
	 * The hash code of a literal is defined as the hash code of its label:
	 * <tt>label.hashCode()</tt>.
	 * 
	 * @return A hash code for the literal.
	 */
	public int hashCode();
}
