/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.config;

import static org.openrdf.repository.config.RepositoryConfigSchema.DELEGATE;

import org.openrdf.model.Graph;
import org.openrdf.model.Resource;
import org.openrdf.model.util.GraphUtil;
import org.openrdf.model.util.GraphUtilException;

/**
 * @author Herko ter Horst
 */
public class DelegatingRepositoryImplConfigBase extends RepositoryImplConfigBase implements
		DelegatingRepositoryImplConfig
{

	private RepositoryImplConfig delegate;

	/**
	 * Create a new DelegatingRepositoryImplConfigBase.
	 */
	public DelegatingRepositoryImplConfigBase() {
		super();
	}

	/**
	 * Create a new DelegatingRepositoryImplConfigBase.
	 */
	public DelegatingRepositoryImplConfigBase(String type) {
		super(type);
	}

	/**
	 * Create a new DelegatingRepositoryImplConfigBase.
	 */
	public DelegatingRepositoryImplConfigBase(String type, RepositoryImplConfig delegate) {
		this(type);
		setDelegate(delegate);
	}

	public RepositoryImplConfig getDelegate() {
		return delegate;
	}

	public void setDelegate(RepositoryImplConfig delegate) {
		this.delegate = delegate;
	}

	@Override
	public void validate()
		throws RepositoryConfigException
	{
		super.validate();
		if (delegate == null) {
			throw new RepositoryConfigException("No delegate specified for " + getType() + " repository");
		}
		delegate.validate();
	}

	@Override
	public Resource export(Graph graph)
	{
		Resource implNode = super.export(graph);

		if (delegate != null) {
			Resource delegateNode = delegate.export(graph);
			graph.add(implNode, DELEGATE, delegateNode);
		}

		return implNode;
	}

	@Override
	public void parse(Graph graph, Resource implNode)
		throws RepositoryConfigException
	{
		super.parse(graph, implNode);

		try {
			Resource delegateNode = GraphUtil.getOptionalObjectResource(graph, implNode, DELEGATE);
			if (delegateNode != null) {
				setDelegate(create(graph, delegateNode));
			}
		}
		catch (GraphUtilException e) {
			throw new RepositoryConfigException(e.getMessage(), e);
		}
	}
}
