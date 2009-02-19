/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.sail.config;

import org.openrdf.repository.Repository;
import org.openrdf.repository.config.RepositoryFactory;
import org.openrdf.repository.config.RepositoryImplConfig;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.Sail;
import org.openrdf.sail.StackableSail;
import org.openrdf.sail.config.DelegatingSailImplConfig;
import org.openrdf.sail.config.SailFactory;
import org.openrdf.sail.config.SailImplConfig;
import org.openrdf.sail.config.SailRegistry;
import org.openrdf.store.StoreConfigException;

/**
 * A {@link RepositoryFactory} that creates {@link SailRepository}s based on
 * RDF configuration data.
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
		throws StoreConfigException
	{
		if (config instanceof SailRepositoryConfig) {
			SailRepositoryConfig sailRepConfig = (SailRepositoryConfig)config;

			Sail sail = createSailStack(sailRepConfig.getSailImplConfig());
			return new SailRepository(sail);
		}

		throw new StoreConfigException("Invalid configuration class: " + config.getClass());
	}

	private Sail createSailStack(SailImplConfig config)
		throws StoreConfigException
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
		throws StoreConfigException
	{
		SailFactory sailFactory = SailRegistry.getInstance().get(config.getType());

		if (sailFactory != null) {
			return sailFactory.getSail(config);
		}

		throw new StoreConfigException("Unsupported Sail type: " + config.getType());
	}

	private void addDelegate(SailImplConfig config, Sail sail)
		throws StoreConfigException
	{
		Sail delegateSail = createSailStack(config);

		try {
			((StackableSail)sail).setDelegate(delegateSail);
		}
		catch (ClassCastException e) {
			throw new StoreConfigException("Delegate configured but " + sail.getClass()
					+ " is not a StackableSail");
		}
	}
}
