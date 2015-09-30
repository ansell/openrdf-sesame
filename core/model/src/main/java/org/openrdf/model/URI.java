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
	@Override
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
	@Override
	public boolean equals(Object o);

	/**
	 * The hash code of an URI is defined as the hash code of its
	 * String-representation: <tt>toString().hashCode</tt>.
	 * 
	 * @return A hash code for the URI.
	 */
	@Override
	public int hashCode();

}
