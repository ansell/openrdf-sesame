/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.manager.templates;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.openrdf.http.client.SesameClient;
import org.openrdf.model.Model;
import org.openrdf.store.StoreConfigException;

/**
 * @author James Leigh
 */
public class RemoteTemplateManager implements ConfigTemplateManager {

	private SesameClient client;

	public RemoteTemplateManager(String serverURL) {
		this(new SesameClient(serverURL));
	}

	public RemoteTemplateManager(SesameClient client) {
		this.client = client;
	}

	public void setUsernameAndPassword(String username, String password) {
		client.setUsernameAndPassword(username, password);
	}

	public URL getLocation()
		throws MalformedURLException
	{
		return new URL(client.templates().getURL());
	}

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
	public void addTemplate(String id, Model model)
		throws StoreConfigException
	{
		client.templates().put(id, model);
	}

	/**
	 * Removes a service from the registry.
	 * 
	 * @param service
	 *        The service be removed from the registry.
	 * @throws StoreConfigException
	 */
	public void removeTemplate(String id)
		throws StoreConfigException
	{
		client.templates().delete(id);
	}

	/**
	 * Gets the service for the specified key, if any.
	 * 
	 * @param key
	 *        The key identifying which service to get.
	 * @return The service for the specified key, or <tt>null</tt> if no such
	 *         service is avaiable.
	 * @throws StoreConfigException 
	 */
	public ConfigTemplate getTemplate(String key) throws StoreConfigException {
		return new ConfigTemplate(client.templates().get(key), client.schemas().get());
	}

	/**
	 * Gets the set of registered keys.
	 * 
	 * @return An unmodifiable set containing all registered keys.
	 * @throws StoreConfigException 
	 */
	public Set<String> getIDs() throws StoreConfigException {
		return new HashSet<String>(client.templates().list());
	}

	public Model getSchemas()
		throws StoreConfigException
	{
		return client.schemas().get();
	}
}
