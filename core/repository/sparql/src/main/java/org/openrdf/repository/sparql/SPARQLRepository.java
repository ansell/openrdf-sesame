/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2011.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.sparql;

import java.io.File;
import java.util.Map;

import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.base.RepositoryBase;

/**
 * A proxy class to access any SPARQL endpoint.
 * 
 * @author James Leigh
 */
public class SPARQLRepository extends RepositoryBase {

	private String queryEndpointUrl;

	private String updateEndpointUrl;

	private Map<String, String> additionalHttpHeaders;

	public SPARQLRepository(String queryEndpointUrl) {
		this.queryEndpointUrl = queryEndpointUrl;
	}

	public SPARQLRepository(String queryEndpointUrl, String updateEndpointUrl) {
		this.queryEndpointUrl = queryEndpointUrl;
		this.updateEndpointUrl = updateEndpointUrl;
	}

	public RepositoryConnection getConnection()
		throws RepositoryException
	{
		return new SPARQLConnection(this, queryEndpointUrl, updateEndpointUrl);
	}

	public File getDataDir() {
		return null;
	}

	public ValueFactory getValueFactory() {
		return ValueFactoryImpl.getInstance();
	}

	@Override
	protected void initializeInternal()
		throws RepositoryException
	{
		// no-op
	}

	public boolean isWritable()
		throws RepositoryException
	{
		return false;
	}

	public void setDataDir(File dataDir) {
		// no-op
	}

	@Override
	protected void shutDownInternal()
		throws RepositoryException
	{
		// no-op
	}

	@Override
	public String toString() {
		return queryEndpointUrl;
	}

	/**
	 * @return Returns the additionalHttpHeaders.
	 */
	public Map<String, String> getAdditionalHttpHeaders() {
		return additionalHttpHeaders;
	}

	/**
	 * @param additionalHttpHeaders
	 *        The additionalHttpHeaders to set as key value pairs.
	 */
	public void setAdditionalHttpHeaders(Map<String, String> additionalHttpHeaders) {
		this.additionalHttpHeaders = additionalHttpHeaders;
	}
}
