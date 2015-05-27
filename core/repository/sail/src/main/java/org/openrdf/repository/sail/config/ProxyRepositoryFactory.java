/* 
 * Licensed to Aduna under one or more contributor license agreements.  
 * See the NOTICE.txt file distributed with this work for additional 
 * information regarding copyright ownership. 
 *
 * Aduna licenses this file to you under the terms of the Aduna BSD 
 * License (the "License"); you may not use this file except in compliance 
 * with the License. See the LICENSE.txt file distributed with this work 
 * for the full License.
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package org.openrdf.repository.sail.config;

import org.openrdf.repository.Repository;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.repository.config.RepositoryFactory;
import org.openrdf.repository.config.RepositoryImplConfig;
import org.openrdf.repository.sail.ProxyRepository;

public class ProxyRepositoryFactory implements RepositoryFactory {

	/**
	 * The type of repositories that are created by this factory.
	 * 
	 * @see RepositoryFactory#getRepositoryType()
	 */
	public static final String REPOSITORY_TYPE = "openrdf:ProxyRepository";

	@Override
	public String getRepositoryType() {
		return REPOSITORY_TYPE;
	}

	@Override
	public RepositoryImplConfig getConfig() {
		return new ProxyRepositoryConfig();
	}

	@Override
	public Repository getRepository(RepositoryImplConfig config)
		throws RepositoryConfigException
	{
		ProxyRepository result = null;

		if (config instanceof ProxyRepositoryConfig) {
			result = new ProxyRepository(((ProxyRepositoryConfig)config).getProxiedRepositoryID());
		}
		else {
			throw new RepositoryConfigException("Invalid configuration class: " + config.getClass());
		}
		return result;
	}
}
