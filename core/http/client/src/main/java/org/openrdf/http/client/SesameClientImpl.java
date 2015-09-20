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
package org.openrdf.http.client;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.http.client.HttpClient;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.openrdf.http.client.util.HttpClientBuilders;

/**
 * Uses {@link HttpClient} to manage HTTP connections.
 * 
 * @author James Leigh
 */
public class SesameClientImpl implements SesameClient, HttpClientDependent {

	/** independent life cycle */
	private HttpClient httpClient;

	/** dependent life cycle */
	private CloseableHttpClient dependentClient;

	private ExecutorService executor = null;
	
	/**
	 * Optional {@link HttpClientBuilder} to create the inner
	 * {@link #httpClient} (if not provided externally)
	 */
	private HttpClientBuilder httpClientBuilder;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public SesameClientImpl() {
		initialize();
	}

	public SesameClientImpl(CloseableHttpClient dependentClient, ExecutorService dependentExecutorService) {
		this.httpClient = this.dependentClient = dependentClient;
		this.executor = dependentExecutorService;
	}

	/**
	 * @return Returns the httpClient.
	 */
	public synchronized HttpClient getHttpClient() {
		if (httpClient == null) {
			httpClient = dependentClient = createHttpClient();
		}
		return httpClient;
	}

	/**
	 * @param httpClient The httpClient to use for remote/service calls.
	 */
	public synchronized void setHttpClient(HttpClient httpClient) {
		this.httpClient = httpClient;
	}

	/**
	 * Set an optional {@link HttpClientBuilder} to create the inner
	 * {@link #httpClient} (if the latter is not provided externally
	 * as dependent client).
	 *
	 * @param httpClientBuilder the builder for the managed HttpClient
	 * @see HttpClientBuilders
	 */
	public synchronized void setHttpClientBuilder(HttpClientBuilder httpClientBuilder) {
		this.httpClientBuilder = httpClientBuilder;
	}
	
	private CloseableHttpClient createHttpClient() {
		if (this.httpClientBuilder!=null) {
			return httpClientBuilder.build();
		}
		return HttpClients.createSystem();
	}

	@Override
	public synchronized SparqlSession createSparqlSession(String queryEndpointUrl, String updateEndpointUrl) {
		SparqlSession session = new SparqlSession(getHttpClient(), executor);
		session.setQueryURL(queryEndpointUrl);
		session.setUpdateURL(updateEndpointUrl);
		return session;
	}

	@Override
	public synchronized SesameSession createSesameSession(String serverURL) {
		SesameSession session = new SesameSession(getHttpClient(), executor);
		session.setServerURL(serverURL);
		return session;
	}

	/*-----------------*
	 * Get/set methods *
	 *-----------------*/

	@Override
	public synchronized void shutDown() {
		if (executor != null) {
			executor.shutdown();
			executor = null;
		}
		if (dependentClient != null) {
			HttpClientUtils.closeQuietly(dependentClient);
			dependentClient = null;
		}
	}

	/**
	 * (re)initializes the connection manager and HttpClient (if not already
	 * done), for example after a shutdown has been invoked earlier. Invoking
	 * this method multiple times will have no effect.
	 */
	public synchronized void initialize() {
		if (executor == null) {
			executor = Executors.newCachedThreadPool();
		}
	}

}
