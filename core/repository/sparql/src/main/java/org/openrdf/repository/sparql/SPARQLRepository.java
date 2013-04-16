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

import java.nio.file.Path;
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
 * A proxy class to access any SPARQL endpoint. The instance must be initialized
 * prior to using it.
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

	/**
	 * Create a new SPARQLRepository using the supplied endpoint URL for queries
	 * and updates.
	 * 
	 * @param endpointUrl
	 *        a SPARQL endpoint URL. May not be null.
	 */
	public SPARQLRepository(String endpointUrl) {
		this(endpointUrl, endpointUrl);
	}

	/**
	 * Create a new SPARQLREpository using the supplied query endpoint URL for
	 * queries, and the supplied update endpoint URL for updates.
	 * 
	 * @param queryEndpointUrl
	 *        a SPARQL endpoint URL for queries. May not be null.
	 * @param updateEndpointUrl
	 *        a SPARQL endpoint URL for updates. May not be null.
	 * @throws IllegalArgumentException
	 *         if one of the supplied endpoint URLs is null.
	 */
	public SPARQLRepository(String queryEndpointUrl, String updateEndpointUrl) {
		if (queryEndpointUrl == null || updateEndpointUrl == null) {
			throw new IllegalArgumentException("endpoint URL may not be null.");
		}

		// initialize HTTP client
		httpClient = createHTTPClient();
		httpClient.setValueFactory(ValueFactoryImpl.getInstance());
		httpClient.setPreferredTupleQueryResultFormat(TupleQueryResultFormat.SPARQL);
		httpClient.setQueryURL(queryEndpointUrl);
		httpClient.setUpdateURL(updateEndpointUrl);
	}

	/**
	 * Creates a new HTTPClient object. Subclasses may override to return a more
	 * specific HTTPClient subtype.
	 * 
	 * @return a HTTPClient object.
	 */
	protected HTTPClient createHTTPClient() {
		return new HTTPClient();
	}

	@Override
	public RepositoryConnection getConnection()
		throws RepositoryException
	{
		if (!isInitialized())
			throw new RepositoryException("SPARQLRepository not initialized.");
		return new SPARQLConnection(this);
	}

	@Override
	public Path getDataDir() {
		return null;
	}

	@Override
	public ValueFactory getValueFactory() {
		return ValueFactoryImpl.getInstance();
	}

	@Override
	protected void initializeInternal()
		throws RepositoryException
	{
		httpClient.initialize();
	}

	@Override
	public boolean isWritable()
		throws RepositoryException
	{
		return false;
	}

	@Override
	public void setDataDir(Path dataDir) {
		// no-op
	}

	protected HTTPClient getHTTPClient() {
		return httpClient;
	}
	
	/**
	 * Set the username and password to use for authenticating with the remote
	 * repository.
	 * 
	 * @param username
	 *        the username. Setting this to null will disable authentication.
	 * @param password
	 *        the password. Setting this to null will disable authentication.
	 */
	public void setUsernameAndPassword(final String username, final String password) {
		httpClient.setUsernameAndPassword(username, password);
	}

	@Override
	protected void shutDownInternal()
		throws RepositoryException
	{
		httpClient.shutDown();
	}
	
	@Override
	public String toString() {
		return getHTTPClient().getQueryURL();
	}

	/**
	 * @return Returns the additionalHttpHeaders.
	 */
	public Map<String, String> getAdditionalHttpHeaders() {
		return getHTTPClient().getAdditionalHttpHeaders();
	}

	/**
	 * @param additionalHttpHeaders
	 *        The additionalHttpHeaders to set as key value pairs.
	 */
	public void setAdditionalHttpHeaders(Map<String, String> additionalHttpHeaders) {
		getHTTPClient().setAdditionalHttpHeaders(additionalHttpHeaders);
	}
}
