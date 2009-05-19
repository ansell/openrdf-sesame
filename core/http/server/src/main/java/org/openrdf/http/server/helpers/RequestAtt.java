/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.helpers;

import org.restlet.data.Request;

import org.openrdf.query.Query;
import org.openrdf.repository.Repository;

/**
 * Sesame-specific request attributes.
 * 
 * @author Arjohn Kampman
 */
public class RequestAtt {

	private static final String REPOSITORY_KEY = "org.openrdf.sesame.repository";

	private static final String CONNECTION_KEY = "org.openrdf.sesame.connection";

	private static final String QUERY_KEY = "org.openrdf.sesame.query";

	/**
	 * Stores a repository in a request's attributes.
	 */
	public static void setRepository(Request request, ServerRepository repository) {
		request.getAttributes().put(REPOSITORY_KEY, repository);
	}

	/**
	 * Retrieves a repository from a request's attributes that has previously
	 * been stored in there using {@link #setRepository(Request, Repository)}.
	 */
	public static ServerRepository getRepository(Request request) {
		return (ServerRepository)request.getAttributes().get(REPOSITORY_KEY);
	}

	/**
	 * Stores a repository connection in a request's attributes.
	 */
	public static void setConnection(Request request, ServerConnection connection) {
		request.getAttributes().put(CONNECTION_KEY, connection);
	}

	/**
	 * Retrieves a repository connection from a request's attributes that has
	 * previously been stored in there using
	 * {@link #setConnection(Request, ServerConnection)}.
	 */
	public static ServerConnection getConnection(Request request) {
		return (ServerConnection)request.getAttributes().get(CONNECTION_KEY);
	}

	/**
	 * Stores a query in a request's attributes.
	 */
	public static void setQuery(Request request, Query query) {
		request.getAttributes().put(QUERY_KEY, query);
	}

	/**
	 * Retrieves a query from a request's attributes that has previously been
	 * stored in there using {@link #setQuery(Request, Query)}.
	 */
	public static Query getQuery(Request request) {
		return (Query)request.getAttributes().get(QUERY_KEY);
	}
}
