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
package org.openrdf.repository.config;

import static org.openrdf.repository.config.RepositoryConfigSchema.DELEGATE;

import org.openrdf.model.Graph;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.util.GraphUtil;
import org.openrdf.model.util.GraphUtilException;
import org.openrdf.model.util.Models;

/**
 * @author Herko ter Horst
 */
public abstract class AbstractDelegatingRepositoryImplConfig extends AbstractRepositoryImplConfig
		implements DelegatingRepositoryImplConfig
{

	private RepositoryImplConfig delegate;

	/**
	 * Create a new DelegatingRepositoryImplConfigBase.
	 */
	public AbstractDelegatingRepositoryImplConfig() {
		super();
	}

	/**
	 * Create a new DelegatingRepositoryImplConfigBase.
	 */
	public AbstractDelegatingRepositoryImplConfig(String type) {
		super(type);
	}

	/**
	 * Create a new DelegatingRepositoryImplConfigBase.
	 */
	public AbstractDelegatingRepositoryImplConfig(String type, RepositoryImplConfig delegate) {
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
	public Resource export(Model model) {
		Resource resource = super.export(model);

		if (delegate != null) {
			Resource delegateNode = delegate.export(model);
			model.add(resource, DELEGATE, delegateNode);
		}

		return resource;
	}

	@Override
	public void parse(Model model, Resource resource)
		throws RepositoryConfigException
	{
		super.parse(model, resource);

		Models.objectResource(model.filter(resource, DELEGATE, null)).ifPresent(
				delegate -> setDelegate(create(model, delegate)));
	}
}
