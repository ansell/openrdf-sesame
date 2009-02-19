/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.model;


/**
 * A factory for creating URIs.
 * 
 * @author Arjohn Kampman
 */
public interface URIFactory {

	/**
	 * Creates a new URI from the supplied string-representation.
	 * 
	 * @param uri
	 *        A string-representation of a URI.
	 * @return An object representing the URI.
	 * @throws IlllegalArgumentException
	 *         If the supplied string does not resolve to a legal (absolute) URI.
	 */
	public URI createURI(String uri);

	/**
	 * Creates a new URI from the supplied namespace and local name. Calling this
	 * method is funtionally equivalent to calling
	 * {@link #createURI(String) createURI(namespace+localName)}, but allows the
	 * ValueFactory to reuse supplied namespace and local name strings whenever
	 * possible. Note that the values returned by {@link URI#getNamespace()} and
	 * {@link URI#getLocalName()} are not necessarily the same as the values that
	 * are supplied to this method.
	 * 
	 * @param namespace
	 *        The URI's namespace.
	 * @param localName
	 *        The URI's local name.
	 * @throws IllegalArgumentException
	 *         If the supplied namespace and localname do not resolve to a legal
	 *         (absolute) URI.
	 */
	public URI createURI(String namespace, String localName);
}
