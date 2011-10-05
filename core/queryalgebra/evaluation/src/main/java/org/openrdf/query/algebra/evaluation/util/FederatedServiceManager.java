/*
 * Copyright fluid Operations AG (http://www.fluidops.com/) (c) 2011.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.util;

import java.util.HashMap;
import java.util.Map;

/**
 * The {@link FederatedServiceManager} is used to manage a set of {@link FederatedService}
 * instances, which are used to evaluate SERVICE expressions for particular service Urls.
 * 
 * Lookup can be done via the serviceUrl using the static method 
 * {@link #getService(String)}. If there is no service for the specified
 * url, a {@link SPARQLFederatedService} is created and registered for future use.
 * 
 * Note that this manager can be used to register custom {@link FederatedService}
 * implementations to provide custom behavior for SERVICE evaluation.
 * 
 * @author Andreas Schwarte
 */
public class FederatedServiceManager {

	/*
	 * TODO maybe move to some other package ? 
	 * TODO shutdown method ?
	 */
	
	
	/**
	 * Map service URL to the corresponding initialized {@link FederatedService}
	 */
	private static Map<String, FederatedService> endpointToService = new HashMap<String, FederatedService>();
	
	
	/**
	 * Register the specified service to evaluate SERVICE expressions for the given url.
	 * 
	 * @param serviceUrl
	 * @param service
	 */
	public static void registerService(String serviceUrl, FederatedService service) {
		endpointToService.put(serviceUrl, service);
	}
	
	/**
	 * Unregister a service registered to serviceURl
	 * @param serviceUrl
	 */
	public static void unregisterService(String serviceUrl) {
		if (endpointToService.containsKey(serviceUrl))
			endpointToService.remove(serviceUrl);
	}
	
	
	/**
	 * Retrieve the {@link FederatedService} registered for serviceUrl. If there is no
	 * service registered for serviceUrl, a new {@link SPARQLFederatedService} is created
	 * and registered.
	 * 
	 * @param serviceUrl
	 * @return
	 */
	public static FederatedService getService(String serviceUrl) {
		FederatedService service = endpointToService.get(serviceUrl);
		if (service == null) {
			service = new SPARQLFederatedService(serviceUrl);
			endpointToService.put(serviceUrl, service);
		}
		return service;
	}	
	

}
