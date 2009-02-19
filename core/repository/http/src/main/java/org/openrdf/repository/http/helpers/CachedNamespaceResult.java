/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.http.helpers;

import java.util.Map;


/**
 *
 * @author James Leigh
 */
public class CachedNamespaceResult extends Cache {
	private Map<String, String> namespaces;

	public CachedNamespaceResult(Map<String, String> namespaces, String eTag) {
		super(eTag);
		this.namespaces = namespaces;
	}

	public Map<String, String> getNamespaces() {
		return namespaces;
	}

}
