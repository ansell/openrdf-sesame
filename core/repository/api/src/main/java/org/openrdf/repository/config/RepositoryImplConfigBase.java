/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.config;

import static org.openrdf.repository.config.RepositoryConfigSchema.REPOSITORYTYPE;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.util.ModelUtil;
import org.openrdf.model.util.ModelUtilException;
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
			Literal typeLit = ModelUtil.getOptionalObjectLiteral(model, implNode, REPOSITORYTYPE);
			if (typeLit != null) {
				setType(typeLit.getLabel());
			}
		}
		catch (ModelUtilException e) {
			throw new StoreConfigException(e.getMessage(), e);
		}
	}

	public static RepositoryImplConfig create(Model model, Resource implNode)
		throws StoreConfigException
	{
		try {
			Literal typeLit = ModelUtil.getOptionalObjectLiteral(model, implNode, REPOSITORYTYPE);

			if (typeLit != null) {
				RepositoryFactory factory = RepositoryRegistry.getInstance().get(typeLit.getLabel());

				if (factory == null) {
					throw new StoreConfigException("Unsupported repository type: " + typeLit.getLabel());
				}

				RepositoryImplConfig implConfig = factory.getConfig();
				implConfig.parse(model, implNode);
				return implConfig;
			}

			return null;
		}
		catch (ModelUtilException e) {
			throw new StoreConfigException(e.getMessage(), e);
		}
	}
}
