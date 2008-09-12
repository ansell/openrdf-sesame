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
import org.openrdf.model.Value;
import org.openrdf.model.impl.ValueFactoryImpl;

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
		throws RepositoryConfigException
	{
		if (type == null) {
			throw new RepositoryConfigException("No type specified for repository implementation");
		}
	}

	public Resource export(Model model) {
		ValueFactoryImpl vf = new ValueFactoryImpl();
		BNode implNode = vf.createBNode();

		if (type != null) {
			model.add(implNode, REPOSITORYTYPE, vf.createLiteral(type));
		}

		return implNode;
	}

	public void parse(Model model, Resource implNode)
		throws RepositoryConfigException
	{
		try {
			for (Value obj : model.objects(implNode, REPOSITORYTYPE)) {
				Literal typeLit = (Literal)obj;
				setType(typeLit.getLabel());
			}
		}
		catch (Exception e) {
			throw new RepositoryConfigException(e.getMessage(), e);
		}
	}

	public static RepositoryImplConfig create(Model model, Resource implNode)
		throws RepositoryConfigException
	{
		try {
			if (model.contains(implNode, REPOSITORYTYPE, null)) {
				Literal typeLit = (Literal)model.objects(implNode, REPOSITORYTYPE).iterator().next();

				RepositoryFactory factory = RepositoryRegistry.getInstance().get(typeLit.getLabel());

				if (factory == null) {
					throw new RepositoryConfigException("Unsupported repository type: " + typeLit.getLabel());
				}

				RepositoryImplConfig implConfig = factory.getConfig();
				implConfig.parse(model, implNode);
				return implConfig;
			}

			return null;
		}
		catch (Exception e) {
			throw new RepositoryConfigException(e.getMessage(), e);
		}
	}
}
