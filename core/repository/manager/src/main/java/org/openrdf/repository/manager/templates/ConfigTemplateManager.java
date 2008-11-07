/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.manager.templates;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;

import org.openrdf.store.StoreConfigException;

/**
 *
 * @author james
 */
public interface ConfigTemplateManager {

	/**
	 * Gets the URL of the server or directory.
	 * 
	 * @throws MalformedURLException If the location cannot be represented as a URL.
	 */
	URL getLocation()
		throws MalformedURLException;

	/**
	 * Adds a service to the registry. Any service that is currently registered
	 * for the same key (as specified by {@link #getKey(Object)}) will be
	 * replaced with the new service.
	 * 
	 * @param service
	 *        The service that should be added to the registry.
	 * @return The previous service that was registered for the same key, or
	 *         <tt>null</tt> if there was no such service.
	 * @throws StoreConfigException 
	 */
	void addTemplate(ConfigTemplate template) throws StoreConfigException;

	/**
	 * Removes a service from the registry.
	 * 
	 * @param service
	 *        The service be removed from the registry.
	 */
	void removeTemplate(String id) throws StoreConfigException;

	/**
	 * Gets the service for the specified key, if any.
	 * 
	 * @param key
	 *        The key identifying which service to get.
	 * @return The service for the specified key, or <tt>null</tt> if no such
	 *         service is avaiable.
	 */
	ConfigTemplate getTemplate(String id) throws StoreConfigException;

	/**
	 * Gets the set of registered keys.
	 * 
	 * @return An unmodifiable set containing all registered keys.
	 */
	Set<String> getIDs() throws StoreConfigException;

}