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
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

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
	 * 
	 * @param graph
	 * @param repositoryNode
	 * @return
	 * @throws RepositoryConfigException
	 */
	public static RepositoryConfig create(Graph graph, Resource repositoryNode)
		throws RepositoryConfigException
	{
		RepositoryConfig config = new RepositoryConfig();
		config.parse(graph, repositoryNode);
		return config;
	}

	/*----------------------------------------------------------------------------------*
	 * Deprecated code from XML-based configuration, will be removed in a future releae *
	 *----------------------------------------------------------------------------------*/

	/*-----------*
	 * Variables *
	 *-----------*/

	private String _className;

	/** Flag indicating whether the repository should be publicly readable. */
	private boolean _worldReadable = false;

	/** Flag indicating whether the repository should be publicly writable. */
	private boolean _worldWritable = false;

	private HashSet<String> _readACL = new HashSet<String>();

	private HashSet<String> _writeACL = new HashSet<String>();;

	/**
	 * Stack of SailConfig objects, representing the Sail stack.
	 */
	private Stack<SailConfig> _sailStack = new Stack<SailConfig>();

	/*--------------*
	 * Constructors *
	 *--------------*/

	@Deprecated
	public RepositoryConfig(String id, boolean worldReadable, boolean worldWritable)
	{
		this(id);
		setWorldReadable(worldReadable);
		setWorldWritable(worldWritable);
	}

	@Deprecated
	public RepositoryConfig(String id, String title, boolean worldReadable, boolean worldWritable)
	{
		this(id, title);
		setWorldReadable(worldReadable);
		setWorldWritable(worldWritable);
	}

	/*---------*
	 * Methods *
	 *---------*/

	@Deprecated
	public String getClassName()
	{
		return _className;
	}

	@Deprecated
	public void setClassName(String className)
	{
		_className = className;
	}

	@Deprecated
	public boolean isWorldReadable()
	{
		return _worldReadable;
	}

	@Deprecated
	public void setWorldReadable(boolean worldReadable)
	{
		_worldReadable = worldReadable;
	}

	@Deprecated
	public boolean isWorldWritable()
	{
		return _worldWritable;
	}

	@Deprecated
	public void setWorldWritable(boolean worldWritable)
	{
		_worldWritable = worldWritable;
	}

	@Deprecated
	public void makePrivate()
	{
		setWorldReadable(false);
		setWorldWritable(false);
	}

	@Deprecated
	public void makePublic()
	{
		setWorldReadable(true);
		setWorldWritable(true);
	}

	@Deprecated
	public void grantReadAccess(String login)
	{
		_readACL.add(login);
	}

	@Deprecated
	public void revokeReadAccess(String login)
	{
		_readACL.remove(login);
	}

	@Deprecated
	public boolean hasReadAccess(String login)
	{
		return _worldReadable || _readACL.contains(login);
	}

	@Deprecated
	public Set<String> getReadACL()
	{
		return Collections.unmodifiableSet(_readACL);
	}

	@Deprecated
	public void grantWriteAccess(String login)
	{
		_writeACL.add(login);
	}

	@Deprecated
	public void revokeWriteAccess(String login)
	{
		_writeACL.remove(login);
	}

	@Deprecated
	public boolean hasWriteAccess(String login)
	{
		return _worldWritable || _writeACL.contains(login);
	}

	@Deprecated
	public Set<String> getWriteACL()
	{
		return Collections.unmodifiableSet(_writeACL);
	}

	@Deprecated
	public void pushSailConfig(SailConfig sailConfig)
	{
		_sailStack.push(sailConfig);
	}

	@Deprecated
	public void addSailConfig(SailConfig sailConfig)
	{
		_sailStack.add(0, sailConfig);
	}

	@Deprecated
	public boolean removeSailConfig(SailConfig sailConfig)
	{
		return _sailStack.remove(sailConfig);
	}

	@Deprecated
	public SailConfig removeSailConfig(int index)
	{
		return _sailStack.remove(index);
	}

	@Deprecated
	public Stack<SailConfig> getSailConfigStack()
	{
		return _sailStack;
	}

	@Deprecated
	public void setSailConfigStack(Stack<SailConfig> sailStack)
	{
		_sailStack = sailStack;
	}
}
