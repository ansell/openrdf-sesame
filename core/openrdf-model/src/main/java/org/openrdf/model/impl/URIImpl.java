/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.model.impl;

import org.openrdf.model.URI;

/**
 * The default implementation of the {@link URI} interface.
 */
public class URIImpl implements URI {

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * The URI string.
	 */
	private final String _uriString;

	/**
	 * An index indicating the first character of the local name in the URI
	 * string, -1 if not yet set.
	 */
	private int _localNameIdx;

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

		_uriString = uriString;
		_localNameIdx = -1;
	}

	/**
	 * Creates a new URI from the supplied namespace and local name.
	 * 
	 * @param namespace
	 *        A URI's namespace.
	 * @param localName
	 *        A URI's local name.
	 * @throws IllegalArgumentException
	 *         If the supplied namespace and localname do not resolve to a legal
	 *         (absolute) URI.
	 */
	public URIImpl(String namespace, String localName) {
		this(namespace + localName);

		assert namespace != null;
		assert localName != null;
	}

	/*---------*
	 * Methods *
	 *---------*/

	// Overrides Object.toString(), implements URI.toString()
	public String toString() {
		return _uriString;
	}

	/**
	 * Initializes the <tt>_localNameIdx</tt> variable.
	 */
	private void _setSplitIdx() {
		int splitIdx = _uriString.indexOf('#');

		if (splitIdx < 0) {
			splitIdx = _uriString.lastIndexOf('/');
		}

		if (splitIdx < 0) {
			splitIdx = _uriString.lastIndexOf(':');
		}

		if (splitIdx < 0) {
			throw new RuntimeException("Not a legal (absolute) URI: " + _uriString);
		}

		_localNameIdx = splitIdx + 1;
	}

	// Implements URI.getNamespace()
	public String getNamespace() {
		if (_localNameIdx < 0) {
			_setSplitIdx();
		}

		return _uriString.substring(0, _localNameIdx);
	}

	// Implements URI.getLocalName()
	public String getLocalName() {
		if (_localNameIdx < 0) {
			_setSplitIdx();
		}

		return _uriString.substring(_localNameIdx);
	}

	// Overrides Object.equals(Object), implements URI.equals(Object)
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if (o instanceof URI) {
			return toString().equals(o.toString());
		}

		return false;
	}

	// Overrides Object.hashCode(), implements URI.hashCode()
	public int hashCode() {
		return _uriString.hashCode();
	}
}
