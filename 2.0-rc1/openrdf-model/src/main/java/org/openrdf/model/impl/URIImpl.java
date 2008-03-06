/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
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

	/**
	 * The URI string.
	 */
	private final String uriString;

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * An index indicating the first character of the local name in the URI
	 * string, -1 if not yet set.
	 */
	private int localNameIdx;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new URI from the supplied string.
	 * 
	 * @param uriString
	 *        A String representing a valid, absolute URI.
	 * @throws IllegalArgumentException
	 *         If the supplied URI is not a valid (absolute) URI.
	 */
	public URIImpl(String uriString) {
		assert uriString != null;

		if (uriString.indexOf(':') < 0) {
			throw new IllegalArgumentException("Not a valid (absolute) URI: " + uriString);
		}

		this.uriString = uriString;
		this.localNameIdx = -1;
	}

	/*---------*
	 * Methods *
	 *---------*/

	// Implements URI.toString()
	@Override
	public String toString()
	{
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
	public boolean equals(Object o)
	{
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
	public int hashCode()
	{
		return uriString.hashCode();
	}
}
