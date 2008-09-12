/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.config;

import org.openrdf.model.Model;
import org.openrdf.model.Resource;

/**
 * @author Arjohn Kampman
 */
public interface RepositoryImplConfig {

	public String getType();

	/**
	 * Validates this configuration. A {@link RepositoryConfigException} is
	 * thrown when the configuration is invalid. The exception should contain an
	 * error message that indicates why the configuration is invalid.
	 * 
	 * @throws RepositoryConfigException
	 *         If the configuration is invalid.
	 */
	public void validate()
		throws RepositoryConfigException;

	public Resource export(Model model);

	public void parse(Model model, Resource implNode)
		throws RepositoryConfigException;
}