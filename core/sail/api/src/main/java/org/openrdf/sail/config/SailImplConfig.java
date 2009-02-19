/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.config;

import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.store.StoreConfigException;

/**
 * @author Arjohn Kampman
 */
public interface SailImplConfig {

	public String getType();

	/**
	 * Validates this configuration. A {@link StoreConfigException} is thrown when
	 * the configuration is invalid. The exception should contain an error
	 * message that indicates why the configuration is invalid.
	 * 
	 * @throws StoreConfigException
	 *         If the configuration is invalid.
	 */
	public void validate()
		throws StoreConfigException;

	public Resource export(Model model);

	public void parse(Model model, Resource implNode)
		throws StoreConfigException;
}