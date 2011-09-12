/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2011.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.sparql;

import java.io.File;

import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

/**
 * Implement the {@link Repository} interface to any SPARQl endpoint.
 * 
 * @author James Leigh
 * 
 */
public class SPARQLRepository implements Repository {
	
	private String queryEndpointUrl;
	private String updateEndpointUrl;

	public SPARQLRepository(String queryEndpointUrl) {
		this.queryEndpointUrl = queryEndpointUrl;
	}

	public SPARQLRepository(String queryEndpointUrl, String updateEndpointUrl) {
		this.queryEndpointUrl = queryEndpointUrl;
		this.updateEndpointUrl = updateEndpointUrl;
	}

	public RepositoryConnection getConnection() throws RepositoryException {
		return new SPARQLConnection(this, queryEndpointUrl, updateEndpointUrl);
	}

	public File getDataDir() {
		return null;
	}

	public ValueFactory getValueFactory() {
		return ValueFactoryImpl.getInstance();
	}

	public void initialize() throws RepositoryException {
		// no-op
	}

	public boolean isWritable() throws RepositoryException {
		return false;
	}

	public void setDataDir(File dataDir) {
		// no-op
	}

	public void shutDown() throws RepositoryException {
		// no-op
	}

	@Override
	public String toString() {
		return queryEndpointUrl;
	}

}
