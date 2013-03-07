/* 
 * Licensed to Aduna under one or more contributor license agreements.  
 * See the NOTICE.txt file distributed with this work for additional 
 * information regarding copyright ownership. 
 *
 * Aduna licenses this file to you under the terms of the Aduna BSD 
 * License (the "License"); you may not use this file except in compliance 
 * with the License. See the LICENSE.txt file distributed with this work 
 * for the full License.
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package org.openrdf.repository.sparql;

import java.io.File;
import java.util.Map;

import info.aduna.io.MavenUtil;

import org.openrdf.http.client.HTTPClient;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.query.resultio.TupleQueryResultFormat;
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
	 * The HTTP client that takes care of the client-server communication.
	 */
	private final HTTPClient httpClient;
	
	// TODO remove from here and from SPARQLConnection entirely?
	private String queryEndpointUrl;

	private String updateEndpointUrl;

	public SPARQLRepository(String queryEndpointUrl) {
		this(queryEndpointUrl, null);
	}

	public SPARQLRepository(String queryEndpointUrl, String updateEndpointUrl) {
		// initialize HTTP client
		httpClient = new HTTPClient();
		httpClient.setValueFactory(new ValueFactoryImpl());
		httpClient.setPreferredTupleQueryResultFormat(TupleQueryResultFormat.SPARQL);
		httpClient.setQueryURL(queryEndpointUrl);
		httpClient.setUpdateURL(updateEndpointUrl);
		
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
	
	public HTTPClient getNewHttpClient() {
		return httpClient;
	}

	@Override
	protected void shutDownInternal()
		throws RepositoryException
	{
		httpClient.shutDown();
	}

	@Override
	public String toString() {
		return queryEndpointUrl;
	}

	/**
	 * @return Returns the additionalHttpHeaders.
	 */
	public Map<String, String> getAdditionalHttpHeaders() {
		return httpClient.getAdditionalHttpHeaders();
	}

	/**
	 * @param additionalHttpHeaders
	 *        The additionalHttpHeaders to set as key value pairs.
	 */
	public void setAdditionalHttpHeaders(Map<String, String> additionalHttpHeaders) {
		httpClient.setAdditionalHttpHeaders(additionalHttpHeaders);
	}
}
