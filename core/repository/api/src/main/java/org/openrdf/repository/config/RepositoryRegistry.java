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
package org.openrdf.repository.config;

import info.aduna.lang.service.ServiceRegistry;

/**
 * A registry that keeps track of the available {@link RepositoryFactory}s.
 * 
 * @author Arjohn Kampman
 */
public class RepositoryRegistry extends ServiceRegistry<String, RepositoryFactory> {

	/**
	 * Internal helper class to avoid continuous synchronized checking.
	 */
	private static class RepositoryRegistryHolder {

		public static final RepositoryRegistry instance = new RepositoryRegistry();
	}

	/**
	 * Gets the default RepositoryRegistry.
	 * 
	 * @return The default registry.
	 */
	public static RepositoryRegistry getInstance() {
		return RepositoryRegistryHolder.instance;
	}

	public RepositoryRegistry() {
		super(RepositoryFactory.class);
	}

	@Override
	protected String getKey(RepositoryFactory factory) {
		return factory.getRepositoryType();
	}
}
