/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.config;

import static org.openrdf.repository.config.RepositoryConfigSchema.REPOSITORYTYPE;

import org.openrdf.model.BNode;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.util.ModelException;
import org.openrdf.store.StoreConfigException;

/**
 * @author Herko ter Horst
 */
public class RepositoryImplConfigBase implements RepositoryImplConfig {

	private String type;

	/**
	 * Create a new RepositoryConfigImpl.
	 */
	public RepositoryImplConfigBase() {
	}

	/**
	 * Create a new RepositoryConfigImpl.
	 */
	public RepositoryImplConfigBase(String type) {
		this();
		setType(type);
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void validate()
		throws StoreConfigException
	{
		if (type == null) {
			throw new StoreConfigException("No type specified for repository implementation");
		}
	}

	public Resource export(Model model) {
		ValueFactoryImpl vf = ValueFactoryImpl.getInstance();

		BNode implNode = vf.createBNode();

		if (type != null) {
			model.add(implNode, REPOSITORYTYPE, vf.createLiteral(type));
		}

		return implNode;
	}

	public void parse(Model model, Resource implNode)
		throws StoreConfigException
	{
		try {
			String type = model.filter(implNode, REPOSITORYTYPE, null).objectString();
			if (type != null) {
				setType(type);
			}
		}
		catch (ModelException e) {
			throw new StoreConfigException(e.getMessage(), e);
		}
	}

	public static RepositoryImplConfig create(Model model, Resource implNode)
		throws StoreConfigException
	{
		try {
			String type = model.filter(implNode, REPOSITORYTYPE, null).objectString();

			if (type != null) {
				RepositoryFactory factory = RepositoryRegistry.getInstance().get(type);

				if (factory == null) {
					throw new StoreConfigException("Unsupported repository type: " + type);
				}

				RepositoryImplConfig implConfig = factory.getConfig();
				implConfig.parse(model, implNode);
				return implConfig;
			}

			return null;
		}
		catch (ModelException e) {
			throw new StoreConfigException(e.getMessage(), e);
		}
	}
}
