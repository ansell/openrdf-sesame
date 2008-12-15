/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.config;

import static org.openrdf.sail.config.SailConfigSchema.DELEGATE;

import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.util.ModelUtil;
import org.openrdf.model.util.ModelUtilException;
import org.openrdf.store.StoreConfigException;

/**
 * @author Herko ter Horst
 */
public class DelegatingSailImplConfigBase extends SailImplConfigBase implements DelegatingSailImplConfig {

	private SailImplConfig delegate;

	/**
	 * Create a new RepositoryConfigImpl.
	 */
	public DelegatingSailImplConfigBase() {
		super();
	}

	/**
	 * Create a new RepositoryConfigImpl.
	 */
	public DelegatingSailImplConfigBase(String type) {
		super(type);
	}

	/**
	 * Create a new RepositoryConfigImpl.
	 */
	public DelegatingSailImplConfigBase(String type, SailImplConfig delegate) {
		this(type);
		setDelegate(delegate);
	}

	public SailImplConfig getDelegate() {
		return delegate;
	}

	public void setDelegate(SailImplConfig delegate) {
		this.delegate = delegate;
	}

	@Override
	public void validate()
		throws StoreConfigException
	{
		super.validate();
		if (delegate == null) {
			throw new StoreConfigException("No delegate specified for " + getType() + " Sail");
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
			Resource delegateNode = model.filter(implNode, DELEGATE, null).resource();
			if (delegateNode != null) {
				setDelegate(SailConfigUtil.parseRepositoryImpl(model, delegateNode));
			}
		}
		catch (ModelUtilException e) {
			throw new StoreConfigException(e.getMessage(), e);
		}
	}
}
