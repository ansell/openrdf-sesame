/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2011.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.sparql;

import java.io.File;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.httpclient.params.HttpMethodParams;

import info.aduna.io.MavenUtil;

import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.base.RepositoryBase;

/**
 * A proxy class to access any SPARQL endpoint. 
 * 
 * The instance must be initialized prior to using it.
 * 
 * @author James Leigh
 */
public class SPARQLRepository extends RepositoryBase {


	private static final String APP_NAME = "OpenRDF.org SPARQLConnection";

	private static final String VERSION = MavenUtil.loadVersion("org.openrdf.sesame",
			"sesame-repository-sparql", "devel");
	
	/**
	 * The key under which the (optional) HTTP header are stored in the
	 * HttpClientParams
	 */
	public static String ADDITIONAL_HEADER_NAME = "additionalHTTPHeaders";
	
	private String queryEndpointUrl;

	private String updateEndpointUrl;

	private Map<String, String> additionalHttpHeaders;

	private HttpClient client;
	
	private MultiThreadedHttpConnectionManager manager;
	
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
		if (!isInitialized())
			throw new RepositoryException("SPARQLRepository not initialized.");
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
			// Use MultiThreadedHttpConnectionManager to allow concurrent access on
			// HttpClient
			manager = new MultiThreadedHttpConnectionManager();

			// Allow 20 concurrent connections to the same host (default is 2)
			HttpConnectionManagerParams params = new HttpConnectionManagerParams();
			params.setDefaultMaxConnectionsPerHost(20);
			manager.setParams(params);

			HttpClientParams clientParams = new HttpClientParams();
			clientParams.setParameter(HttpMethodParams.USER_AGENT,
					APP_NAME + "/" + VERSION + " " + clientParams.getParameter(HttpMethodParams.USER_AGENT));
			// set additional HTTP headers, if desired by the user
			if (getAdditionalHttpHeaders() != null)
				clientParams.setParameter(ADDITIONAL_HEADER_NAME, getAdditionalHttpHeaders());
			client = new HttpClient(clientParams, manager);
	}

	public boolean isWritable()
		throws RepositoryException
	{
		return false;
	}

	public void setDataDir(File dataDir) {
		// no-op
	}
	
	public HttpClient getHttpClient() {
		return client;
	}

	@Override
	protected void shutDownInternal()
		throws RepositoryException
	{
		if (manager!=null)
			manager.shutdown();
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
