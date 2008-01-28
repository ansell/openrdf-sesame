/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.model;

import org.openrdf.model.util.URIUtil;

/**
 * A URI. A URI consists of a namespace and a local name, which are derived from
 * a URI string by splitting it in two using the following algorithm:
 * <ul>
 * <li>Split after the first occurrence of the '#' character,
 * <li>If this fails, split after the last occurrence of the '/' character,
 * <li>If this fails, split after the last occurrence of the ':' character.
 * </ul>
 * The last step should never fail as every legal (full) URI contains at least
 * one ':' character to seperate the scheme from the rest of the URI. The
 * implementation should check this upon object creation.
 * 
 * @see URIUtil#getLocalNameIndex(String)
 */
public interface URI extends Resource {

	/**
	 * Returns the String-representation of this URI.
	 * 
	 * @return The String-representation of this URI.
	 */
	public String toString();

	/**
	 * Gets the namespace of this URI. The namespace is defined as per the
	 * algorithm described in the class documentation.
	 * 
	 * @return The URI's namespace.
	 */
	public String getNamespace();

	/**
	 * Gets the local name of this URI. The local name is defined as per the
	 * algorithm described in the class documentation.
	 * 
	 * @return The URI's local name.
	 */
	public String getLocalName();

	/**
	 * Compares a URI object to another object.
	 * 
	 * @param o
	 *        The object to compare this URI to.
	 * @return <tt>true</tt> if the other object is an instance of {@link URI}
	 *         and their String-representations are equal, <tt>false</tt>
	 *         otherwise.
	 */
	public boolean equals(Object o);

	/**
	 * The hash code of a URI is defined as the hash code of its
	 * String-representation: <tt>toString().hashCode</tt>.
	 * 
	 * @return A hash code for the URI.
	 */
	public int hashCode();
}
