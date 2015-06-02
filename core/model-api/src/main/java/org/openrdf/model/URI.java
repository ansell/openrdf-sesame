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
	@Override
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
	@Override
	public boolean equals(Object o);

	/**
	 * The hash code of a URI is defined as the hash code of its
	 * String-representation: <tt>toString().hashCode</tt>.
	 * 
	 * @return A hash code for the URI.
	 */
	@Override
	public int hashCode();
}
