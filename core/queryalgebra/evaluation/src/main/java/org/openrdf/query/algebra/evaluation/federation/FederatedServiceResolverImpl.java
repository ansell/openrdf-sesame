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
package org.openrdf.query.algebra.evaluation.federation;

import org.apache.http.client.HttpClient;

import org.openrdf.http.client.HttpClientDependent;
import org.openrdf.http.client.SesameClient;
import org.openrdf.http.client.SesameClientDependent;
import org.openrdf.http.client.SesameClientImpl;
import org.openrdf.query.QueryEvaluationException;

/**
 * The {@link FederatedServiceResolverImpl} is used to manage a set of
 * {@link FederatedService} instances, which are used to evaluate SERVICE
 * expressions for particular service Urls.
 * <p>
 * Lookup can be done via the serviceUrl using the method
 * {@link #getService(String)}. If there is no service for the specified url, a
 * {@link SPARQLFederatedService} is created and registered for future use.
 * 
 * @author Andreas Schwarte
 * @author James Leigh
 */
public class FederatedServiceResolverImpl extends FederatedServiceResolverBase implements FederatedServiceResolver, HttpClientDependent, SesameClientDependent {

	public FederatedServiceResolverImpl() {
		super();
	}

	/** independent life cycle */
	private SesameClient client;

	/** dependent life cycle */
	private SesameClientImpl dependentClient;

	public synchronized SesameClient getSesameClient() {
		if (client == null) {
			client = dependentClient = new SesameClientImpl();
		}
		return client;
	}

	public synchronized void setSesameClient(SesameClient client) {
		this.client = client;
	}

	public HttpClient getHttpClient() {
		return getSesameClient().getHttpClient();
	}

	public void setHttpClient(HttpClient httpClient) {
		if (dependentClient == null) {
			client = dependentClient = new SesameClientImpl();
		}
		dependentClient.setHttpClient(httpClient);
	}
	
	@Override
	protected FederatedService createService(String serviceUrl)
			throws QueryEvaluationException {
		return new SPARQLFederatedService(serviceUrl, getSesameClient());
	}

	@Override
	public void shutDown() {
		super.shutDown();
		if (dependentClient != null) {
			dependentClient.shutDown();
			dependentClient = null;
		}
	}
}
