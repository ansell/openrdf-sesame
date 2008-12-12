/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.helpers;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.openrdf.query.Query;
import org.openrdf.repository.RepositoryConnection;


/**
 *
 * @author James Leigh
 */
public class ActiveConnection {

	private RepositoryConnection connection;

	private Map<String, Query> queries = new ConcurrentHashMap<String, Query>();

	public ActiveConnection(RepositoryConnection connection) {
		this.connection = connection;
	}

	public RepositoryConnection getConnection() {
		return connection;
	}

	public void putQuery(String id, Query query) {
		queries.put(id, query);
	}

	public Query getQuery(String id) {
		return queries.get(id);
	}

	public void removeQuery(String id) {
		queries.remove(id);
	}

}
