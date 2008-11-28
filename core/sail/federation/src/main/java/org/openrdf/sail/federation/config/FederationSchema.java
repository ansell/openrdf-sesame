/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.federation.config;

import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

public class FederationSchema {

	/** http://www.openrdf.org/config/sail/federation# */
	public static String NAMESPACE = "http://www.openrdf.org/config/sail/federation#";

	public static URI MEMBER = new URIImpl(NAMESPACE + "member");

	private FederationSchema() {
		// no constructor
	}
}
