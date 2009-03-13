/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008-2009.
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

	private final SesameClient client;

	public RemoteTemplateManager(SesameClient client) {
		this.client = client;
	}

	public URL getLocation()
		throws MalformedURLException
	{
		return new URL(client.templates().getURL());
	}

	public void addTemplate(String id, Model model)
		throws StoreConfigException
	{
		client.templates().put(id, model);
	}

	public void removeTemplate(String id)
		throws StoreConfigException
	{
		client.templates().delete(id);
	}

	public ConfigTemplate getTemplate(String key)
		throws StoreConfigException
	{
		Model statements = client.templates().get(key);
		if (statements == null) {
			return null;
		}
		return new ConfigTemplate(statements, client.schemas().get());
	}

	public Set<String> getIDs()
		throws StoreConfigException
	{
		return new HashSet<String>(client.templates().list());
	}

	public Model getSchemas()
		throws StoreConfigException
	{
		return client.schemas().get();
	}
}
