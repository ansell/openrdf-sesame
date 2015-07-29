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
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.Sail;
import org.openrdf.sail.StackableSail;
import org.openrdf.sail.config.DelegatingSailImplConfig;
import org.openrdf.sail.config.SailConfigException;
import org.openrdf.sail.config.SailFactory;
import org.openrdf.sail.config.SailImplConfig;
import org.openrdf.sail.config.SailRegistry;

/**
 * A {@link RepositoryFactory} that creates {@link SailRepository}s based on RDF
 * configuration data.
 * 
 * @author Arjohn Kampman
 */
public class SailRepositoryFactory implements RepositoryFactory {

	/*-----------*
	 * Constants *
	 *-----------*/

	/**
	 * The type of repositories that are created by this factory.
	 * 
	 * @see RepositoryFactory#getRepositoryType()
	 */
	public static final String REPOSITORY_TYPE = "openrdf:SailRepository";

	/*---------*
	 * Methods *
	 *---------*/

	/**
	 * Returns the repository's type: <tt>openrdf:SailRepository</tt>.
	 */
	public String getRepositoryType() {
		return REPOSITORY_TYPE;
	}

	public RepositoryImplConfig getConfig() {
		return new SailRepositoryConfig();
	}

	public Repository getRepository(RepositoryImplConfig config)
		throws RepositoryConfigException
	{
		if (config instanceof SailRepositoryConfig) {
			SailRepositoryConfig sailRepConfig = (SailRepositoryConfig)config;

			try {
				Sail sail = createSailStack(sailRepConfig.getSailImplConfig());
				return new SailRepository(sail);
			}
			catch (SailConfigException e) {
				throw new RepositoryConfigException(e.getMessage(), e);
			}
		}

		throw new RepositoryConfigException("Invalid configuration class: " + config.getClass());
	}

	private Sail createSailStack(SailImplConfig config)
		throws RepositoryConfigException, SailConfigException
	{
		Sail sail = createSail(config);

		if (config instanceof DelegatingSailImplConfig) {
			SailImplConfig delegateConfig = ((DelegatingSailImplConfig)config).getDelegate();
			if (delegateConfig != null) {
				addDelegate(delegateConfig, sail);
			}
		}

		return sail;
	}

	private Sail createSail(SailImplConfig config)
		throws RepositoryConfigException, SailConfigException
	{
		SailFactory sailFactory = SailRegistry.getInstance().get(config.getType()).orElseThrow(
				() -> new RepositoryConfigException("Unsupported Sail type: " + config.getType()));
		return sailFactory.getSail(config);
	}

	private void addDelegate(SailImplConfig config, Sail sail)
		throws RepositoryConfigException, SailConfigException
	{
		Sail delegateSail = createSailStack(config);

		try {
			((StackableSail)sail).setBaseSail(delegateSail);
		}
		catch (ClassCastException e) {
			throw new RepositoryConfigException("Delegate configured but " + sail.getClass()
					+ " is not a StackableSail");
		}
	}
}
