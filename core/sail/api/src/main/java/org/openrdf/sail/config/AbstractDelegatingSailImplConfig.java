/* 
 * Licensed to Aduna under one or more contributor license agreements.  
 * See the NOTICE.txt file distributed with this work for additional 
 * information regarding copyright ownership. 
 *
 * Aduna licenses this file to you under the terms of the Aduna BSD 
 * License (the "License"); you may not use this file except in compliance 
 * with the License. See the LICENSE.txt file distributed with this work 
 * for the full License.
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package org.openrdf.sail.config;

import static org.openrdf.sail.config.SailConfigSchema.DELEGATE;

import org.openrdf.model.Graph;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.util.GraphUtil;
import org.openrdf.model.util.GraphUtilException;
import org.openrdf.model.util.ModelException;
import org.openrdf.model.util.Models;

/**
 * @author Herko ter Horst
 */
public abstract class AbstractDelegatingSailImplConfig extends AbstractSailImplConfig
		implements DelegatingSailImplConfig
{

	private SailImplConfig delegate;

	/**
	 * Create a new RepositoryConfigImpl.
	 */
	public AbstractDelegatingSailImplConfig() {
		super();
	}

	/**
	 * Create a new RepositoryConfigImpl.
	 */
	public AbstractDelegatingSailImplConfig(String type) {
		super(type);
	}

	/**
	 * Create a new RepositoryConfigImpl.
	 */
	public AbstractDelegatingSailImplConfig(String type, SailImplConfig delegate) {
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
		throws SailConfigException
	{
		super.validate();
		if (delegate == null) {
			throw new SailConfigException("No delegate specified for " + getType() + " Sail");
		}
		delegate.validate();
	}

	@Override
	public Resource export(Model m) {
		Resource implNode = super.export(m);

		if (delegate != null) {
			Resource delegateNode = delegate.export(m);
			m.add(implNode, DELEGATE, delegateNode);
		}

		return implNode;
	}

	@Override
	public void parse(Model m, Resource implNode)
		throws SailConfigException
	{
		super.parse(m, implNode);

		try {
			Models.objectResource(m.filter(implNode, DELEGATE, null)).ifPresent(
					delegate -> setDelegate(SailConfigUtil.parseRepositoryImpl(m, delegate)));
		}
		catch (ModelException e) {
			throw new SailConfigException(e.getMessage(), e);
		}
	}
}
