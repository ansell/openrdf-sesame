/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.webclient.repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.aduna.iteration.CloseableIteration;
import info.aduna.text.ToStringComparator;

import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.http.HTTPRepository;

public class RepositoryInfo {

	final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	public static final String REPOSITORY_KEY = "openrdf-repository";

	private String _id;

	private String _location;

	private String _description;

	private boolean _readable;

	private boolean _writable;

	private String _serverURL;

	private HTTPRepository _httpRepository;

	public String getId() {
		return _id;
	}

	public void setId(String id) {
		_id = id;
	}

	public void setLocation(String location) {
		_location = location;
	}

	public String getLocation() {
		return _location;
	}

	public void setDescription(String description) {
		_description = description;
	}

	public String getDescription() {
		return _description;
	}

	public boolean isReadable() {
		return _readable;
	}

	public void setReadable(boolean readable) {
		_readable = readable;
	}

	public boolean isWritable() {
		return _writable;
	}

	public void setWritable(boolean writable) {
		_writable = writable;
	}

	public void setServerURL(String serverURL) {
		_serverURL = serverURL;
		_httpRepository = new HTTPRepository(_serverURL, getId());
	}
	
	public HTTPRepository getRepository() {
		return _httpRepository;
	}

	public List<Namespace> getNamespaces() {
		List<Namespace> result = null;

		RepositoryConnection conn = null;
		try {
			conn = _httpRepository.getConnection();
			CloseableIteration<? extends Namespace, RepositoryException> namespaces = conn.getNamespaces();
			result = new ArrayList<Namespace>();
			while (namespaces.hasNext()) {
				result.add(namespaces.next());
			}
			Collections.sort(result, ToStringComparator.getInstance());
		}
		catch (RepositoryException e) {
			logger.warn("Unable to retrieve namespaces", e);
		}
		finally {
			if (conn != null) {
				try {
					conn.close();
				}
				catch (RepositoryException e) {
					logger.debug("Unable to close connection...", e);
				}
			}
		}

		return result;
	}

	public List<Resource> getContexts() {
		List<Resource> result = null;

		RepositoryConnection conn = null;
		try {
			conn = _httpRepository.getConnection();
			CloseableIteration<? extends Resource, RepositoryException> contexts = conn.getContextIDs();
			result = new ArrayList<Resource>();
			while (contexts.hasNext()) {
				result.add(contexts.next());
			}
			Collections.sort(result, ToStringComparator.getInstance());
		}
		catch (RepositoryException e) {
			logger.warn("Unable to retrieve contexts", e);
		}
		finally {
			if (conn != null) {
				try {
					conn.close();
				}
				catch (RepositoryException e) {
					logger.debug("Unable to close connection...", e);
				}
			}
		}

		return result;
	}

}
