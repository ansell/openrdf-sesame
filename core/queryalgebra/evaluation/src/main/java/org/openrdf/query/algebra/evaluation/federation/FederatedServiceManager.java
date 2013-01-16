/*
 * Copyright fluid Operations AG (http://www.fluidops.com/) (c) 2011.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.federation;

import java.util.concurrent.ConcurrentHashMap;

import org.openrdf.repository.RepositoryException;

/**
 * The {@link FederatedServiceManager} is used to manage a set of {@link FederatedService}
 * instances, which are used to evaluate SERVICE expressions for particular service Urls.<p>
 * 
 * Lookup can be done via the serviceUrl using the method {@link #getService(String)}.
 * If there is no service for the specified url, a {@link SPARQLFederatedService} is 
 * created and registered for future use.<p>
 * 
 * Note that this manager can be used to register custom {@link FederatedService}
 * implementations to provide custom behavior for SERVICE evaluation.<p>
 * 
 * The default behavior can be changed by extending from this class and setting
 * the implementation class via {@link #setImplementationClass(Class)}. The 
 * new class must provide the default constructor.
 * 
 * @author Andreas Schwarte
 */
public class FederatedServiceManager {

	/*
	 * TODO maybe move to some other package ? 
	 * TODO shutdown method ?
	 */
	
	
		

	private static Class<? extends FederatedServiceManager> implementationClass = FederatedServiceManager.class;
	private static volatile FederatedServiceManager instance = null;
	
	public static FederatedServiceManager getInstance() {
		if (instance==null) {
			try {
				instance = implementationClass.newInstance();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return instance;
	}
	
	public static synchronized void setImplementationClass(
			Class<? extends FederatedServiceManager> implementationClass) {
		FederatedServiceManager.implementationClass = implementationClass;
		try {
			instance = implementationClass.newInstance();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	
	
	public FederatedServiceManager() {
		;
	}
	
	
	/**
	 * Map service URL to the corresponding initialized {@link FederatedService}
	 */
	private ConcurrentHashMap<String, FederatedService> endpointToService = new ConcurrentHashMap<String, FederatedService>();
	
	
	/**
	 * Register the specified service to evaluate SERVICE expressions for the given url.
	 * 
	 * @param serviceUrl
	 * @param service
	 */
	public void registerService(String serviceUrl, FederatedService service) {
		endpointToService.put(serviceUrl, service);
	}
	

	/**
	 * Unregister a service registered to serviceURl
	 * @param serviceUrl
	 */
	public void unregisterService(String serviceUrl) {
		FederatedService service = endpointToService.remove(serviceUrl);
		if (service!=null) {
			try {
				service.shutdown();
			} catch (RepositoryException e) {
				// TODO issue a warning, otherwise ignore
			}
		}
	}
	
	
	/**
	 * Retrieve the {@link FederatedService} registered for serviceUrl. If there is no
	 * service registered for serviceUrl, a new {@link SPARQLFederatedService} is created
	 * and registered.
	 * 
	 * @param serviceUrl
	 * @return
	 * @throws RepositoryException 
	 */
	public FederatedService getService(String serviceUrl) throws RepositoryException {
		FederatedService service = endpointToService.get(serviceUrl);
		if (service == null) {
			service = new SPARQLFederatedService(serviceUrl);
			service.initialize();
			endpointToService.put(serviceUrl, service);
		}
		return service;
	}	
	
	
	public void unregisterAll() {
		synchronized (endpointToService) {
			for (FederatedService service : endpointToService.values()) {
				try {
					service.shutdown();
				} catch (RepositoryException e) {
					// TODO issue a warning, otherwise ignore
				}
			}
			endpointToService.clear();
		}
	}

}
