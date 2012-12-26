/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008-2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.federation.config;

import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

/**
 * RDF Schema used by the federation configuration.
 * 
 * @author James Leigh
 */
public final class FederationSchema {

	/** http://www.openrdf.org/config/sail/federation# */
	public static final String NAMESPACE = "http://www.openrdf.org/config/sail/federation#";

	public static final URI MEMBER = new URIImpl(NAMESPACE + "member");

	/**
	 * For all triples with a predicate in this space, the container RDF store
	 * contains all triples with that subject and any predicate in this space.
	 */
	public static final URI LOCALPROPERTYSPACE = new URIImpl(NAMESPACE + "localPropertySpace");

	/**
	 * If no two members contain the same statement.
	 */
	public static final URI DISTINCT = new URIImpl(NAMESPACE + "distinct");

	/**
	 * If the federation should not try and add statements to its members.
	 */
	public static final URI READ_ONLY = new URIImpl(NAMESPACE + "readOnly");

	private FederationSchema() {
		// no constructor
	}
}
