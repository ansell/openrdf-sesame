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
 * An Internationalized Resource Identifier (IRI). IRIs are an extension of the
 * existing {@link URI}: while URIs are limited to a subset of the ASCII
 * character set, IRIs may contain characters from the Universal Character Set
 * (Unicode/ISO 10646), including Chinese or Japanese kanji, Korean, Cyrillic
 * characters, and so forth. It is defined by RFC 3987.
 *  <p>
 *  An IRI can be split into
 * a namespace part and a local name part, which are derived from an IRI string
 * by splitting it in two using the following algorithm:
 * <ul>
 * <li>Split after the first occurrence of the '#' character,
 * <li>If this fails, split after the last occurrence of the '/' character,
 * <li>If this fails, split after the last occurrence of the ':' character.
 * </ul>
 * The last step should never fail as every legal (full) IRI contains at least
 * one ':' character to seperate the scheme from the rest of the IRI. The
 * implementation should check this upon object creation.
 * 
 * @see <a href="http://tools.ietf.org/html/rfc3987">RFC 3987</a>
 */
public interface IRI extends Resource {

	/**
	 * Returns the String-representation of this IRI.
	 * 
	 * @return The String-representation of this IRI.
	 */
	public String toString();

	/**
	 * Gets the namespace of this IRI. The namespace is defined as per the
	 * algorithm described in the class documentation.
	 * 
	 * @return The IRI's namespace.
	 */
	public String getNamespace();

	/**
	 * Gets the local name of this IRI. The local name is defined as per the
	 * algorithm described in the class documentation.
	 * 
	 * @return The IRI's local name.
	 */
	public String getLocalName();

	/**
	 * Compares a IRI object to another object.
	 * 
	 * @param o
	 *        The object to compare this IRI to.
	 * @return <tt>true</tt> if the other object is an instance of {@link IRI}
	 *         and their String-representations are equal, <tt>false</tt>
	 *         otherwise.
	 */
	public boolean equals(Object o);

	/**
	 * The hash code of a IRI is defined as the hash code of its
	 * String-representation: <tt>toString().hashCode</tt>.
	 * 
	 * @return A hash code for the IRI.
	 */
	public int hashCode();
}
