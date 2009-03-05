/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.model;

import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

/**
 * Wraps a {@link URIImpl} providing an internal id and version.
 * 
 * @author James Leigh
 */
public class RdbmsURI extends RdbmsResource implements URI {

	private static final long serialVersionUID = 3317398596013196032L;

	private URI uri;

	public RdbmsURI(URI uri) {
		this.uri = uri;
	}

	public RdbmsURI(Number id, Integer version, URI uri) {
		super(id, version);
		this.uri = uri;
	}

	public String getLocalName() {
		return uri.getLocalName();
	}

	public String getNamespace() {
		return uri.getNamespace();
	}

	public String stringValue() {
		return uri.stringValue();
	}

	@Override
	public String toString() {
		return uri.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		return uri.equals(o);
	}

	@Override
	public int hashCode() {
		return uri.hashCode();
	}

}
