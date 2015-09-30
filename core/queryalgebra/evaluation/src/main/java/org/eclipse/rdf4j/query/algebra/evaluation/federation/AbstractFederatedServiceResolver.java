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
package org.eclipse.rdf4j.query.algebra.evaluation.federation;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.algebra.evaluation.federation.FederatedService;
import org.eclipse.rdf4j.query.algebra.evaluation.federation.FederatedServiceResolver;
import org.eclipse.rdf4j.repository.RepositoryException;

/**
 * Base class for {@link FederatedServiceResolver} which takes care for lifecycle
 * management of produced {@link FederatedService}s.<p>
 * 
 * Specific implementation can implement {@link #createService(String)}.
 * 
 * @author Andreas Schwarte
 *
 */
public abstract class AbstractFederatedServiceResolver implements FederatedServiceResolver {

	
	/**
	 * Map service URL to the corresponding initialized {@link FederatedService}
	 */
	protected Map<String, FederatedService> endpointToService = new HashMap<String, FederatedService>();

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
	 * {@link FederatedService} is created and registered.
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
				service = createService(serviceUrl);
				endpointToService.put(serviceUrl, service);
			}
		}
		if (!service.isInitialized()) {
			service.initialize();
		}
		return service;
	}
	
	/**
	 * Create a new {@link FederatedService} for the given serviceUrl. This method
	 * is invoked, if no {@link FederatedService} has been created yet for the
	 * serviceUrl. 
	 * 
	 * @param serviceUrl the service IRI
	 * @return a non-null {@link FederatedService}
	 * @throws QueryEvaluationException
	 */
	protected abstract FederatedService createService(String serviceUrl) throws QueryEvaluationException;

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
	}
}
