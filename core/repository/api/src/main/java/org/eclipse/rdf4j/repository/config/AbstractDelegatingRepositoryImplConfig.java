/*******************************************************************************
 * Copyright (c) 2015, Eclipse Foundation, Inc. and its licensors.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * - Neither the name of the Eclipse Foundation, Inc. nor the names of its
 *   contributors may be used to endorse or promote products derived from this
 *   software without specific prior written permission. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package org.eclipse.rdf4j.repository.config;

import static org.eclipse.rdf4j.repository.config.RepositoryConfigSchema.DELEGATE;

import org.eclipse.rdf4j.model.Graph;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.util.GraphUtil;
import org.eclipse.rdf4j.model.util.GraphUtilException;
import org.eclipse.rdf4j.model.util.Models;

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
