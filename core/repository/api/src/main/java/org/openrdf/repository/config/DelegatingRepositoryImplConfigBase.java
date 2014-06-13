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

import java.util.Optional;

import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.util.Models;
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
	public Resource export(Model graph)
	{
		Resource implNode = super.export(graph);

		if (delegate != null) {
			Resource delegateNode = delegate.export(graph);
			graph.add(implNode, DELEGATE, delegateNode);
		}

		return implNode;
	}

	@Override
	public void parse(Model graph, Resource implNode)
		throws RepositoryConfigException
	{
		super.parse(graph, implNode);

		try {
			Optional<Resource> delegateNode = Models.getOptionalObjectResource(graph, implNode, DELEGATE);
			if (delegateNode.isPresent()) {
				setDelegate(create(graph, delegateNode.get()));
			}
		}
		catch (GraphUtilException e) {
			throw new RepositoryConfigException(e.getMessage(), e);
		}
	}
}
