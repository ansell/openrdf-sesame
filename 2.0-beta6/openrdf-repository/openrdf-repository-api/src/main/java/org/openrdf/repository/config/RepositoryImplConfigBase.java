/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.config;

import static org.openrdf.repository.config.RepositoryConfigSchema.REPOSITORYTYPE;

import org.openrdf.model.BNode;
import org.openrdf.model.Graph;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.util.GraphUtil;
import org.openrdf.model.util.GraphUtilException;

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

	public Resource export(Graph graph) {
		BNode implNode = graph.getValueFactory().createBNode();

		if (type != null) {
			graph.add(implNode, REPOSITORYTYPE, graph.getValueFactory().createLiteral(type));
		}

		return implNode;
	}

	public void parse(Graph graph, Resource implNode)
		throws RepositoryConfigException
	{
		try {
			Literal typeLit = GraphUtil.getOptionalObjectLiteral(graph, implNode, REPOSITORYTYPE);
			if (typeLit != null) {
				setType(typeLit.getLabel());
			}
		}
		catch (GraphUtilException e) {
			throw new RepositoryConfigException(e.getMessage(), e);
		}
	}

	public static RepositoryImplConfig create(Graph graph, Resource implNode)
		throws RepositoryConfigException
	{
		try {
			Literal typeLit = GraphUtil.getOptionalObjectLiteral(graph, implNode, REPOSITORYTYPE);

			if (typeLit != null) {
				RepositoryFactory factory = RepositoryRegistry.getInstance().get(typeLit.getLabel());

				if (factory == null) {
					throw new RepositoryConfigException("Unsupported repository type: " + typeLit.getLabel());
				}

				RepositoryImplConfig implConfig = factory.getConfig();
				implConfig.parse(graph, implNode);
				return implConfig;
			}

			return null;
		}
		catch (GraphUtilException e) {
			throw new RepositoryConfigException(e.getMessage(), e);
		}
	}
}
