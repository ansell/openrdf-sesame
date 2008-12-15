/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.sail.config;

import static org.openrdf.repository.sail.config.SailRepositorySchema.SAILIMPL;
import static org.openrdf.sail.config.SailConfigSchema.SAILTYPE;

import org.openrdf.model.Literal;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.util.ModelUtil;
import org.openrdf.model.util.ModelUtilException;
import org.openrdf.repository.config.RepositoryImplConfigBase;
import org.openrdf.sail.config.SailFactory;
import org.openrdf.sail.config.SailImplConfig;
import org.openrdf.sail.config.SailRegistry;
import org.openrdf.store.StoreConfigException;

/**
 * @author Arjohn Kampman
 */
public class SailRepositoryConfig extends RepositoryImplConfigBase {

	private SailImplConfig sailImplConfig;

	public SailRepositoryConfig() {
		super(SailRepositoryFactory.REPOSITORY_TYPE);
	}

	public SailRepositoryConfig(SailImplConfig sailImplConfig) {
		this();
		setSailImplConfig(sailImplConfig);
	}

	public SailImplConfig getSailImplConfig() {
		return sailImplConfig;
	}

	public void setSailImplConfig(SailImplConfig sailImplConfig) {
		this.sailImplConfig = sailImplConfig;
	}

	@Override
	public void validate()
		throws StoreConfigException
	{
		super.validate();
		if (sailImplConfig == null) {
			throw new StoreConfigException("No Sail implementation specified for Sail repository");
		}

		sailImplConfig.validate();
	}

	@Override
	public Resource export(Model model) {
		Resource repImplNode = super.export(model);

		if (sailImplConfig != null) {
			Resource sailImplNode = sailImplConfig.export(model);
			model.add(repImplNode, SAILIMPL, sailImplNode);
		}

		return repImplNode;
	}

	@Override
	public void parse(Model model, Resource repImplNode)
		throws StoreConfigException
	{
		try {
			Resource sailImplNode = model.filter(repImplNode, SAILIMPL, null).resource();

			if (sailImplNode != null) {
				Literal typeLit = model.filter(sailImplNode, SAILTYPE, null).literal();

				if (typeLit != null) {
					SailFactory factory = SailRegistry.getInstance().get(typeLit.getLabel());

					if (factory == null) {
						throw new StoreConfigException("Unsupported Sail type: " + typeLit.getLabel());
					}

					sailImplConfig = factory.getConfig();
					sailImplConfig.parse(model, sailImplNode);
				}
			}
		}
		catch (ModelUtilException e) {
			throw new StoreConfigException(e.getMessage(), e);
		}
	}
}
