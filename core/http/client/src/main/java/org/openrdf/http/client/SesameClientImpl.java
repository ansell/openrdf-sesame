/*******************************************************************************
 * Copyright (c) 2015, Eclipse Foundation, Inc. and its licensors.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * - Neither the name of the Eclipse Foundation, Inc. nor the names of its
 *   contributors may be used to endorse or promote products derived from this
 *   software without specific prior written permission. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
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
