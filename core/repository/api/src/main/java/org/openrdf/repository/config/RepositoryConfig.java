/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.config;

import static org.openrdf.repository.config.RepositoryConfigSchema.REPOSITORY;
import static org.openrdf.repository.config.RepositoryConfigSchema.REPOSITORYID;
import static org.openrdf.repository.config.RepositoryConfigSchema.REPOSITORYIMPL;

import org.openrdf.model.BNode;
import org.openrdf.model.Graph;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.util.GraphUtil;
import org.openrdf.model.util.GraphUtilException;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;

/**
 * @author Arjohn Kampman
 */
public class RepositoryConfig {

	private String id;

	private String title;

	private RepositoryImplConfig implConfig;

	/**
	 * Create a new RepositoryConfig.
	 */
	public RepositoryConfig() {
	}

	/**
	 * Create a new RepositoryConfigImpl.
	 */
	public RepositoryConfig(String id) {
		this();
		setID(id);
	}

	/**
	 * Create a new RepositoryConfigImpl.
	 */
	public RepositoryConfig(String id, RepositoryImplConfig implConfig) {
		this(id);
		setRepositoryImplConfig(implConfig);
	}

	/**
	 * Create a new RepositoryConfigImpl.
	 */
	public RepositoryConfig(String id, String title) {
		this(id);
		setTitle(title);
	}

	/**
	 * Create a new RepositoryConfigImpl.
	 */
	public RepositoryConfig(String id, String title, RepositoryImplConfig implConfig) {
		this(id, title);
		setRepositoryImplConfig(implConfig);
	}

	public String getID() {
		return id;
	}

	public void setID(String id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public RepositoryImplConfig getRepositoryImplConfig() {
		return implConfig;
	}

	public void setRepositoryImplConfig(RepositoryImplConfig implConfig) {
		this.implConfig = implConfig;
	}

	/**
	 * Validates this configuration. A {@link RepositoryConfigException} is
	 * thrown when the configuration is invalid. The exception should contain an
	 * error message that indicates why the configuration is invalid.
	 * 
	 * @throws RepositoryConfigException
	 *         If the configuration is invalid.
	 */
	public void validate()
		throws RepositoryConfigException
	{
		if (id == null) {
			throw new RepositoryConfigException("Repository ID missing");
		}
		if (implConfig == null) {
			throw new RepositoryConfigException("Repository implementation for repository missing");
		}
		implConfig.validate();
	}

	public void export(Graph graph) {
		ValueFactory vf = graph.getValueFactory();

		BNode repositoryNode = vf.createBNode();

		graph.add(repositoryNode, RDF.TYPE, REPOSITORY);

		if (id != null) {
			graph.add(repositoryNode, REPOSITORYID, vf.createLiteral(id));
		}
		if (title != null) {
			graph.add(repositoryNode, RDFS.LABEL, vf.createLiteral(title));
		}
		if (implConfig != null) {
			Resource implNode = implConfig.export(graph);
			graph.add(repositoryNode, REPOSITORYIMPL, implNode);
		}
	}

	public void parse(Graph graph, Resource repositoryNode)
		throws RepositoryConfigException
	{
		try {
			Literal idLit = GraphUtil.getOptionalObjectLiteral(graph, repositoryNode, REPOSITORYID);
			if (idLit != null) {
				setID(idLit.getLabel());
			}

			Literal titleLit = GraphUtil.getOptionalObjectLiteral(graph, repositoryNode, RDFS.LABEL);
			if (titleLit != null) {
				setTitle(titleLit.getLabel());
			}

			Resource implNode = GraphUtil.getOptionalObjectResource(graph, repositoryNode, REPOSITORYIMPL);
			if (implNode != null) {
				setRepositoryImplConfig(RepositoryImplConfigBase.create(graph, implNode));
			}
		}
		catch (GraphUtilException e) {
			throw new RepositoryConfigException(e.getMessage(), e);
		}
	}

	/**
	 * Creates a new <tt>RepositoryConfig</tt> object and initializes it by
	 * supplying the <tt>graph</tt> and <tt>repositoryNode</tt> to its
	 * {@link #parse(Graph, Resource) parse} method.
	 */
	public static RepositoryConfig create(Graph graph, Resource repositoryNode)
		throws RepositoryConfigException
	{
		RepositoryConfig config = new RepositoryConfig();
		config.parse(graph, repositoryNode);
		return config;
	}
}
