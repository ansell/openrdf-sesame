/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.config;

import static org.openrdf.repository.config.RepositoryConfigSchema.DELEGATE;

import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.util.ModelUtil;
import org.openrdf.model.util.ModelUtilException;
import org.openrdf.store.StoreConfigException;

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
		throws StoreConfigException
	{
		super.validate();
		if (delegate == null) {
			throw new StoreConfigException("No delegate specified for " + getType() + " repository");
		}
		delegate.validate();
	}

	@Override
	public Resource export(Model model) {
		Resource implNode = super.export(model);

		if (delegate != null) {
			Resource delegateNode = delegate.export(model);
			model.add(implNode, DELEGATE, delegateNode);
		}

		return implNode;
	}

	@Override
	public void parse(Model model, Resource implNode)
		throws StoreConfigException
	{
		super.parse(model, implNode);

		try {
			Resource delegateNode = ModelUtil.getOptionalObjectResource(model, implNode, DELEGATE);
			if (delegateNode != null) {
				setDelegate(create(model, delegateNode));
			}
		}
		catch (ModelUtilException e) {
			throw new StoreConfigException(e.getMessage(), e);
		}
	}
}
