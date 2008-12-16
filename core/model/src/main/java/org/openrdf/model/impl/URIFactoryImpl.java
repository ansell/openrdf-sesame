/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.model.impl;

import org.openrdf.model.URI;
import org.openrdf.model.URIFactory;

/**
 * @author James Leigh
 */
public class URIFactoryImpl implements URIFactory {

	public URI createURI(String uri) {
		return new URIImpl(uri);
	}

	public URI createURI(String namespace, String localName) {
		return createURI(namespace + localName);
	}

}
