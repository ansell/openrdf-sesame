/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.helpers;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;

import org.openrdf.query.Query;
import org.openrdf.repository.RepositoryConnection;


/**
 *
 * @author James Leigh
 */
public class ActiveConnection {

	private RepositoryConnection connection;

	private Map<String, Query> queries = new ConcurrentHashMap<String, Query>();

	private Set<HttpServletRequest> activeRequests = new HashSet<HttpServletRequest>();

	private long lastAccessed;

	public ActiveConnection(RepositoryConnection connection) {
		this.connection = connection;
	}

	public RepositoryConnection getConnection() {
		return connection;
	}

	public void accessed(long now) {
		lastAccessed = now;
	}

	public long getLastAccessed() {
		return lastAccessed;
	}

	public void open(HttpServletRequest request) {
		synchronized (activeRequests) {
			activeRequests.add(request);
		}
	}

	public void close(HttpServletRequest request) {
		synchronized (activeRequests) {
			activeRequests.remove(request);
		}
	}

	public boolean isActive() {
		synchronized (activeRequests) {
			return !activeRequests.isEmpty();
		}
	}

	public Set<HttpServletRequest> getActiveRequests() {
		synchronized (activeRequests) {
			return new HashSet<HttpServletRequest>(activeRequests);
		}
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
