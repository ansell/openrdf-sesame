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
 * A Uniform Resource Identifier (URI).
 * 
 * @deprecated Since 4.0. Use {@link IRI} instead.
 * @author Jeen Broekstra
 * @author Arjohn Kampman
 * @see <a href="http://tools.ietf.org/html/rfc3986">RFC 3986</a>
 */
@Deprecated
public interface URI extends Resource {

	/**
	 * Returns the String-representation of this URI.
	 * 
	 * @return The String-representation of this URI.
	 */
	public String toString();

	/**
	 * Gets the namespace part of this URI. The namespace is defined as per the
	 * algorithm described in the class documentation.
	 * 
	 * @return The URI's namespace.
	 */
	public String getNamespace();

	/**
	 * Gets the local name part of this URI. The local name is defined as per the
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
	 * The hash code of an URI is defined as the hash code of its
	 * String-representation: <tt>toString().hashCode</tt>.
	 * 
	 * @return A hash code for the URI.
	 */
	public int hashCode();

}
