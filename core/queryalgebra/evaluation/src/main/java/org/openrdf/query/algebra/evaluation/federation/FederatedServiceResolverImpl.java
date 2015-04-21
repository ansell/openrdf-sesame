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

import java.util.HashMap;
import java.util.Map;

import org.openrdf.http.client.SesameClient;
import org.openrdf.http.client.SesameClientImpl;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;

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
public class FederatedServiceResolverImpl implements FederatedServiceResolver {

	public FederatedServiceResolverImpl() {
		super();
	}

	/**
	 * Map service URL to the corresponding initialized {@link FederatedService}
	 */
	private Map<String, FederatedService> endpointToService = new HashMap<String, FederatedService>();

	private SesameClient client;

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

	/**
	 * Register the specified service to evaluate SERVICE expressions for the
	 * given url.
	 * 
	 * @param serviceUrl
	 * @param service
	 */
	public synchronized void registerService(String serviceUrl, FederatedService service) {
		endpointToService.put(serviceUrl, service);
	}

	/**
	 * Unregister a service registered to serviceURl
	 * 
	 * @param serviceUrl
	 */
	public void unregisterService(String serviceUrl) {
		FederatedService service;
		synchronized (endpointToService) {
			service = endpointToService.remove(serviceUrl);
		}
		if (service != null && service.isInitialized()) {
			try {
				service.shutdown();
			}
			catch (QueryEvaluationException e) {
				// TODO issue a warning, otherwise ignore
			}
		}
	}

	/**
	 * Retrieve the {@link FederatedService} registered for serviceUrl. If there
	 * is no service registered for serviceUrl, a new
	 * {@link SPARQLFederatedService} is created and registered.
	 * 
	 * @param serviceUrl
	 *        locator for the federation service
	 * @return the {@link FederatedService}, created fresh if necessary
	 * @throws RepositoryException
	 */
	public FederatedService getService(String serviceUrl)
		throws QueryEvaluationException
	{
		FederatedService service;
		synchronized (endpointToService) {
			service = endpointToService.get(serviceUrl);
			if (service == null) {
				service = new SPARQLFederatedService(serviceUrl, getSesameClient());
				endpointToService.put(serviceUrl, service);
			}
		}
		if (!service.isInitialized()) {
			service.initialize();
		}
		return service;
	}

	public void unregisterAll() {
		synchronized (endpointToService) {
			for (FederatedService service : endpointToService.values()) {
				try {
					service.shutdown();
				}
				catch (QueryEvaluationException e) {
					// TODO issue a warning, otherwise ignore
				}
			}
			endpointToService.clear();
		}
	}

	public void shutDown() {
		unregisterAll();
		if (dependentClient != null) {
			dependentClient.shutDown();
			dependentClient = null;
		}
	}

}
