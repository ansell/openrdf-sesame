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

/**
 * An Internationalized Resource Identifier (IRI). IRIs are an extension of the
 * existing {@link URI}: while URIs are limited to a subset of the ASCII
 * character set, IRIs may contain characters from the Universal Character Set
 * (Unicode/ISO 10646), including Chinese or Japanese kanji, Korean, Cyrillic
 * characters, and so forth. It is defined by RFC 3987.
 * <p>
 * An IRI can be split into a namespace part and a local name part, which are
 * derived from an IRI string by splitting it in two using the following
 * algorithm:
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
 * @since 4.0.0
 * @author Jeen Broekstra
 */
@SuppressWarnings("deprecation")
public interface IRI extends URI, Resource {

	/**
	 * Returns the String-representation of this IRI.
	 * 
	 * @return The String-representation of this IRI.
	 */
	public String toString();

	/**
	 * Gets the namespace part of this IRI. The namespace is defined as per the
	 * algorithm described in the class documentation.
	 * 
	 * @return The IRI's namespace.
	 */
	public String getNamespace();

	/**
	 * Gets the local name part of this IRI. The local name is defined as per the
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
	 * The hash code of an IRI is defined as the hash code of its
	 * String-representation: <tt>toString().hashCode</tt>.
	 * 
	 * @return A hash code for the IRI.
	 */
	public int hashCode();
}
