/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007--2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.http;

import org.openrdf.http.client.QueryClient;
import org.openrdf.query.impl.AbstractQuery;

/**
 * @author Arjohn Kampman
 * @author James Leigh
 */
public abstract class HTTPQuery extends AbstractQuery {

	private String queryString;

	public HTTPQuery(String queryString) {
		this.queryString = queryString;
	}

	protected void prepareClient(QueryClient client) {
		client.setIncludeInferred(includeInferred);
		client.setDataset(dataset);
		client.setBindingSet(bindings);
	}

	@Override
	public String toString() {
		return queryString;
	}
}
