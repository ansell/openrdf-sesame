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
package org.eclipse.rdf4j.model.impl;

import java.util.Objects;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.util.URIUtil;

/**
 * The default implementation of the {@link IRI} interface.
 */
public class SimpleIRI implements IRI {

	/*-----------*
	 * Constants *
	 *-----------*/

	private static final long serialVersionUID = -7330406348751485330L;

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * The IRI string.
	 */
	private String iriString;

	/**
	 * An index indicating the first character of the local name in the IRI
	 * string, -1 if not yet set.
	 */
	private int localNameIdx;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new, un-initialized IRI. This IRI's string value needs to be
	 * {@link #setIRIString(String) set} before the normal methods can be used.
	 */
	protected SimpleIRI() {
	}

	/**
	 * Creates a new IRI from the supplied string.
	 * <p>
	 * Note that creating SimpleIRI objects directly via this constructor is not
	 * the recommended approach. Instead, use a
	 * {@link org.eclipse.rdf4j.model.ValueFactory ValueFactory} (obtained from your
	 * repository or by using {@link SimpleValueFactory#getInstance()}) to create
	 * new IRI objects.
	 * 
	 * @param iriString
	 *        A String representing a valid, absolute IRI. May not be
	 *        <code>null</code>.
	 * @throws IllegalArgumentException
	 *         If the supplied IRI is not a valid (absolute) IRI.
	 * @see {@link SimpleValueFactory#createIRI(String)}
	 */
	protected SimpleIRI(String iriString) {
		setIRIString(iriString);
	}

	/*---------*
	 * Methods *
	 *---------*/

	protected void setIRIString(String iriString) {
		Objects.requireNonNull(iriString, "iriString must not be null");

		if (iriString.indexOf(':') < 0) {
			throw new IllegalArgumentException("Not a valid (absolute) IRI: " + iriString);
		}

		this.iriString = iriString;
		this.localNameIdx = -1;
	}

	// Implements IRI.toString()
	@Override
	public String toString() {
		return iriString;
	}

	public String stringValue() {
		return iriString;
	}

	public String getNamespace() {
		if (localNameIdx < 0) {
			localNameIdx = URIUtil.getLocalNameIndex(iriString);
		}

		return iriString.substring(0, localNameIdx);
	}

	public String getLocalName() {
		if (localNameIdx < 0) {
			localNameIdx = URIUtil.getLocalNameIndex(iriString);
		}

		return iriString.substring(localNameIdx);
	}

	// Implements IRI.equals(Object)
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if (o instanceof IRI) {
			return toString().equals(o.toString());
		}

		return false;
	}

	// Implements IRI.hashCode()
	@Override
	public int hashCode() {
		return iriString.hashCode();
	}
}
