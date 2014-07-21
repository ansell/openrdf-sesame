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
package org.openrdf.model.impl;

import org.openrdf.model.URI;
import org.openrdf.model.util.URIUtil;

/**
 * The default implementation of the {@link URI} interface.
 */
public class URIImpl implements URI {

	/*-----------*
	 * Constants *
	 *-----------*/

	private static final long serialVersionUID = -7330406348751485330L;

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * The URI string.
	 */
	private String uriString;

	/**
	 * An index indicating the first character of the local name in the URI
	 * string, -1 if not yet set.
	 */
	private int localNameIdx;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new, unitialized URI. This URI's string value needs to be
	 * {@link #setURIString(String) set} before the normal methods can be used.
	 */
	protected URIImpl() {
	}

	/**
	 * Creates a new URI from the supplied string.
	 * 
	 * @param uriString
	 *        A String representing a valid, absolute URI.
	 * @throws IllegalArgumentException
	 *         If the supplied URI is not a valid (absolute) URI.
	 */
	public URIImpl(String uriString) {
		setURIString(uriString);
	}

	/*---------*
	 * Methods *
	 *---------*/

	protected void setURIString(String uriString) {
		assert uriString != null;

		if (uriString.indexOf(':') < 0) {
			throw new IllegalArgumentException("Not a valid (absolute) URI: " + uriString);
		}

		this.uriString = uriString;
		this.localNameIdx = -1;
	}

	// Implements URI.toString()
	@Override
	public String toString() {
		return uriString;
	}

	public String stringValue() {
		return uriString;
	}

	public String getNamespace() {
		if (localNameIdx < 0) {
			localNameIdx = URIUtil.getLocalNameIndex(uriString);
		}

		return uriString.substring(0, localNameIdx);
	}

	public String getLocalName() {
		if (localNameIdx < 0) {
			localNameIdx = URIUtil.getLocalNameIndex(uriString);
		}

		return uriString.substring(localNameIdx);
	}

	// Implements URI.equals(Object)
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if (o instanceof URI) {
			return toString().equals(o.toString());
		}

		return false;
	}

	// Implements URI.hashCode()
	@Override
	public int hashCode() {
		return uriString.hashCode();
	}

	@Override
	public String ntriplesString() {
		return "<" + uriString + ">";
	}

	@Override
	public String getIRIString() {
		return uriString;
	}
}
