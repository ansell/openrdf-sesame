/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008-2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.manager.templates;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;

import org.openrdf.model.Model;
import org.openrdf.store.StoreConfigException;

/**
 * @author james
 */
public interface ConfigTemplateManager {

	/**
	 * Gets the URL of the server or directory.
	 * 
	 * @throws MalformedURLException
	 *         If the location cannot be represented as a URL.
	 */
	URL getLocation()
		throws MalformedURLException;

	/**
	 * Adds a template to the manager. Any template that is currently registered
	 * with the same id will be replaced with the new template.
	 * 
	 * @param id
	 *        The template's ID.
	 * @param template
	 *        The template that should be associated with the specified ID.
	 */
	void addTemplate(String id, Model template)
		throws StoreConfigException;

	/**
	 * Removes a template from the manager.
	 * 
	 * @param id
	 *        The template's ID.
	 */
	void removeTemplate(String id)
		throws StoreConfigException;

	/**
	 * Gets the template for the specified key, if any.
	 * 
	 * @param id
	 *        The template's ID.
	 * @return The template for the specified ID, or <tt>null</tt> if no such
	 *         template is available.
	 */
	ConfigTemplate getTemplate(String id)
		throws StoreConfigException;

	/**
	 * Gets the set of registered IDs.
	 * 
	 * @return An unmodifiable set containing all registered IDs.
	 */
	Set<String> getIDs()
		throws StoreConfigException;

	Model getSchemas()
		throws StoreConfigException;
}