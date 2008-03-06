/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.webclient.server;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.aduna.collections.LRUMap;

import org.openrdf.http.client.HTTPClient;
import org.openrdf.http.client.RepositoryInfo;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.http.HTTPRepository;

public class Server {

	/** Logger for this class and subclasses */
	final Logger logger = LoggerFactory.getLogger(this.getClass());

	private String location;

	private HTTPClient httpClient;

	private Map<String, HTTPRepository> repositories;

	public Server(String location) {
		this.location = location;

		if (!this.location.endsWith("/")) {
			this.location += "/";
		}

		httpClient = new HTTPClient();
		httpClient.setServerURL(location);
		
		repositories = new LRUMap<String, HTTPRepository>(16);
	}

	public String getLocation() {
		return location;
	}

	public Map<String, RepositoryInfo> getRepositories() throws RepositoryException {
		return RepositoryInfo.getAll(httpClient, true);
	}
	
	public HTTPRepository getRepository(String id) {
		HTTPRepository result = repositories.get(id);
		
		if(result == null) {
			result = new HTTPRepository(location, id);
			repositories.put(id, result);
		}
		
		return result;
	}

}
