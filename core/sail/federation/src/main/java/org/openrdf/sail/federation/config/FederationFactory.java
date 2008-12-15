/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.federation.config;

import org.openrdf.repository.config.RepositoryFactory;
import org.openrdf.repository.config.RepositoryImplConfig;
import org.openrdf.repository.config.RepositoryRegistry;
import org.openrdf.sail.Sail;
import org.openrdf.sail.config.SailFactory;
import org.openrdf.sail.config.SailImplConfig;
import org.openrdf.sail.federation.Federation;
import org.openrdf.store.StoreConfigException;

/**
 * Creates a federation based on its configuration.
 * 
 * @see FederationConfig
 * @author James Leigh
 */
public class FederationFactory implements SailFactory {

	/**
	 * The type of repositories that are created by this factory.
	 * 
	 * @see SailFactory#getSailType()
	 */
	public static final String SAIL_TYPE = "openrdf:Federation";

	/**
	 * Returns the Sail's type: <tt>openrdf:Federation</tt>.
	 */
	public String getSailType() {
		return SAIL_TYPE;
	}

	public SailImplConfig getConfig() {
		return new FederationConfig();
	}

	public Sail getSail(SailImplConfig config)
		throws StoreConfigException
	{
		if (!SAIL_TYPE.equals(config.getType())) {
			throw new StoreConfigException("Invalid Sail type: " + config.getType());
		}
		assert config instanceof FederationConfig;
		FederationConfig cfg = (FederationConfig)config;
		Federation sail = new Federation();
		for (RepositoryImplConfig member : cfg.getMembers()) {
			RepositoryFactory factory = RepositoryRegistry.getInstance().get(member.getType());
			if (factory == null) {
				throw new StoreConfigException("Unsupported repository type: " + config.getType());
			}
			sail.addMember(factory.getRepository(member));
		}
		sail.setLocalPropertySpace(cfg.getLocalPropertySpace());
		sail.setDisjoint(cfg.isDisjoint());
		return sail;
	}
}
