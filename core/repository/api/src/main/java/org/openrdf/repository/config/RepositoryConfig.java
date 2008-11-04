/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.config;

import static org.openrdf.repository.config.RepositoryConfigSchema.REPOSITORY;
import static org.openrdf.repository.config.RepositoryConfigSchema.REPOSITORYID;
import static org.openrdf.repository.config.RepositoryConfigSchema.REPOSITORYIMPL;
import static org.openrdf.repository.config.RepositoryConfigSchema.REPOSITORYTITLE;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.util.ModelUtil;
import org.openrdf.model.util.ModelUtilException;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.store.StoreConfigException;

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
	 * Validates this configuration. A {@link StoreConfigException} is
	 * thrown when the configuration is invalid. The exception should contain an
	 * error message that indicates why the configuration is invalid.
	 * 
	 * @throws StoreConfigException
	 *         If the configuration is invalid.
	 */
	public void validate()
		throws StoreConfigException
	{
		if (id == null) {
			throw new StoreConfigException("Repository ID missing");
		}
		if (implConfig == null) {
			throw new StoreConfigException("Repository implementation for repository missing");
		}
		implConfig.validate();
	}

	public void export(Model model) {
		ValueFactory vf = ValueFactoryImpl.getInstance();

		BNode repositoryNode = vf.createBNode();

		model.add(repositoryNode, RDF.TYPE, REPOSITORY);

		if (id != null) {
			model.add(repositoryNode, REPOSITORYID, vf.createLiteral(id));
		}
		if (title != null) {
			model.add(repositoryNode, REPOSITORYTITLE, vf.createLiteral(title));
			model.add(repositoryNode, RDFS.LABEL, vf.createLiteral(title));
		}
		if (implConfig != null) {
			Resource implNode = implConfig.export(model);
			model.add(repositoryNode, REPOSITORYIMPL, implNode);
		}
	}

	public void parse(Model model, Resource repositoryNode)
		throws StoreConfigException
	{
		try {
			Literal idLit = ModelUtil.getOptionalObjectLiteral(model, repositoryNode, REPOSITORYID);
			if (idLit != null) {
				setID(idLit.getLabel());
			}

			Literal titleLit = ModelUtil.getOptionalObjectLiteral(model, repositoryNode, RDFS.LABEL);
			if (titleLit != null) {
				setTitle(titleLit.getLabel());
			}

			titleLit = ModelUtil.getOptionalObjectLiteral(model, repositoryNode, REPOSITORYTITLE);
			if (titleLit != null) {
				setTitle(titleLit.getLabel());
			}

			Resource implNode = ModelUtil.getOptionalObjectResource(model, repositoryNode, REPOSITORYIMPL);
			if (implNode != null) {
				setRepositoryImplConfig(RepositoryImplConfigBase.create(model, implNode));
			}
		}
		catch (ModelUtilException e) {
			throw new StoreConfigException(e.getMessage(), e);
		}
	}

	/**
	 * Creates a new <tt>RepositoryConfig</tt> object and initializes it by
	 * supplying the <tt>model</tt> and <tt>repositoryNode</tt> to its
	 * {@link #parse(Model, Resource) parse} method.
	 * 
	 * @param model
	 * @param repositoryNode
	 * @return
	 * @throws StoreConfigException
	 */
	public static RepositoryConfig create(Model model, Resource repositoryNode)
		throws StoreConfigException
	{
		RepositoryConfig config = new RepositoryConfig();
		config.parse(model, repositoryNode);
		return config;
	}
}
