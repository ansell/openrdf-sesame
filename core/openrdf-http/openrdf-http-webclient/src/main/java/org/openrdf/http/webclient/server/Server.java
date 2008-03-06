/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.webclient.server;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openrdf.http.client.HTTPClient;
import org.openrdf.http.webclient.repository.RepositoryInfo;
import org.openrdf.model.Literal;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResult;

public class Server {

	/** Logger for this class and subclasses */
	final Logger logger = LoggerFactory.getLogger(this.getClass());

	private String _location;

	private HTTPClient _httpClient;

	public static final String SERVER_KEY = "openrdf-server";

	public Server(String location) {
		_location = location;

		if (!_location.endsWith("/")) {
			_location += "/";
		}

		_httpClient = new HTTPClient();
		_httpClient.setServerURL(_location);
	}

	public String getLocation() {
		return _location;
	}

	public Map<String, RepositoryInfo> getRepositories() {
		Map<String, RepositoryInfo> result = new TreeMap<String, RepositoryInfo>();

		try {
			TupleQueryResult responseFromServer = _httpClient.getRepositoryList();
			while(responseFromServer.hasNext()) {
				BindingSet bindingSet = responseFromServer.next();
				RepositoryInfo rep = new RepositoryInfo();

				String uri = bindingSet.getValue("uri").toString();
				String id = bindingSet.getValue("id").toString();
				String description = bindingSet.getValue("title").toString();
				boolean readable = Boolean.parseBoolean(((Literal)bindingSet.getValue("readable")).getLabel());
				boolean writable = Boolean.parseBoolean(((Literal)bindingSet.getValue("writable")).getLabel());

				rep.setLocation(uri);
				rep.setId(id);
				rep.setDescription(description);
				rep.setReadable(readable);
				rep.setWritable(writable);

				rep.setServerURL(_location);

				result.put(rep.getId(), rep);
			}
		}
		catch (IOException ioe) {
			logger.warn("Unable to retrieve list of repositories", ioe);
			result = null;
		}
		catch (QueryEvaluationException e) {
			logger.warn("Unable to retrieve list of repositories", e);
			result = null;
		}

		return result;
	}

}
