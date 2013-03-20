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

import org.openrdf.repository.Repository;

/**
 * A RepositoryFactory takes care of creating and initializing a specific type
 * of {@link Repository}s based on RDF configuration data.
 * 
 * @author Arjohn Kampman
 */
public interface RepositoryFactory {

	/**
	 * Returns the type of the repositories that this factory creates. Repository
	 * types are used for identification and should uniquely identify specific
	 * implementations of the Repository API. This type <em>can</em> be equal to
	 * the fully qualified class name of the repository, but this is not
	 * required.
	 */
	public String getRepositoryType();

	public RepositoryImplConfig getConfig();

	/**
	 * Returns a Repository instance that has been initialized using the supplied
	 * configuration data.
	 * 
	 * @param config
	 *        TODO
	 * @return The created (but un-initialized) repository.
	 * @throws RepositoryConfigException
	 *         If no repository could be created due to invalid or incomplete
	 *         configuration data.
	 */
	public Repository getRepository(RepositoryImplConfig config)
		throws RepositoryConfigException;
}
