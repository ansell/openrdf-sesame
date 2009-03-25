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
 * A manager for repository configuration templates.
 * 
 * @author james
 * @author Arjohn Kampman
 */
public interface ConfigTemplateManager {

	/**
	 * Gets the URL of the server or directory.
	 * 
	 * @throws MalformedURLException
	 *         If the location cannot be represented as a URL.
	 */
	public URL getLocation()
		throws MalformedURLException;

	/**
	 * Gets the identifiers of the available repository configuration templates.
	 * 
	 * @return The repository configuration template identifiers.
	 * @throws StoreConfigException
	 *         If there was a problem getting the set of identifiers.
	 */
	public Set<String> getIDs()
		throws StoreConfigException;

	/**
	 * Gets the template for the specified key, if any.
	 * 
	 * @param templateID
	 *        The template's ID.
	 * @return The template for the specified ID, or <tt>null</tt> if no such
	 *         template is available.
	 * @throws StoreConfigException
	 *         If there was a problem getting the repository configuration
	 *         template.
	 */
	public ConfigTemplate getTemplate(String templateID)
		throws StoreConfigException;

	/**
	 * Adds a template to the manager. Any template that is currently registered
	 * with the same id will be replaced with the new template.
	 * 
	 * @param id
	 *        The template's ID.
	 * @param template
	 *        The template that should be added.
	 * @throws StoreConfigException
	 *         If there was a problem adding the template.
	 */
	public void addTemplate(String id, Model template)
		throws StoreConfigException;

	/**
	 * Removes a template from the manager.
	 * 
	 * @param templateID
	 *        The template's ID.
	 * @return <tt>true</tt> if such a template existed and was removed,
	 *         <tt>false</tt> otherwise.
	 * @throws StoreConfigException
	 *         If there was a problem removing the template.
	 */
	public boolean removeTemplate(String templateID)
		throws StoreConfigException;

	/**
	 * Gets the template schema's.
	 */
	public Model getSchemas()
		throws StoreConfigException;
}