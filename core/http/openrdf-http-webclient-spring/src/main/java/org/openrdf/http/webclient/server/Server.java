/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.webclient.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.manager.RemoteRepositoryManager;
import org.openrdf.repository.manager.RepositoryInfo;

public class Server {

	/** Logger for this class and subclasses */
	final Logger logger = LoggerFactory.getLogger(this.getClass());

	private String location;

	private RemoteRepositoryManager repositoryManager;

	public Server(String location) throws RepositoryException {
		this.location = location;

		if (!this.location.endsWith("/")) {
			this.location += "/";
		}

		repositoryManager = new RemoteRepositoryManager(location);
		repositoryManager.initialize();
	}

	public String getLocation() {
		return location;
	}

	/**
	 * @return Returns the repositoryManager.
	 */
	public RemoteRepositoryManager getRepositoryManager() {
		return repositoryManager;
	}
	
	public List<RepositoryInfo> getRepositoryInfos() throws RepositoryException {
		List<RepositoryInfo> result = new ArrayList<RepositoryInfo>();
		
		result.addAll(repositoryManager.getAllRepositoryInfos(false));
		Collections.sort(result);
		
		return result;
	}
}
