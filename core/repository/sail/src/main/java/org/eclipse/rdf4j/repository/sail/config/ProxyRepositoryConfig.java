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
package org.eclipse.rdf4j.repository.sail.config;

import org.eclipse.rdf4j.model.Graph;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.GraphUtil;
import org.eclipse.rdf4j.model.util.GraphUtilException;
import org.eclipse.rdf4j.model.util.ModelException;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.repository.config.AbstractRepositoryImplConfig;
import org.eclipse.rdf4j.repository.config.RepositoryConfigException;

public class ProxyRepositoryConfig extends AbstractRepositoryImplConfig {

	private String proxiedID;

	public ProxyRepositoryConfig() {
		super(ProxyRepositoryFactory.REPOSITORY_TYPE);
	}

	public ProxyRepositoryConfig(String proxiedID) {
		this();
		this.setProxiedRepositoryID(proxiedID);
	}

	public final void setProxiedRepositoryID(String value) {
		this.proxiedID = value;
	}

	public String getProxiedRepositoryID() {
		return this.proxiedID;
	}

	@Override
	public void validate()
		throws RepositoryConfigException
	{
		super.validate();
		if (null == this.proxiedID) {
			throw new RepositoryConfigException("No id specified for proxied repository");
		}
	}

	@Override
	public Resource export(Model model) {
		Resource implNode = super.export(model);
		if (null != this.proxiedID) {
			model.add(implNode, ProxyRepositorySchema.PROXIED_ID,
					SimpleValueFactory.getInstance().createLiteral(this.proxiedID));
		}
		return implNode;
	}

	@Override
	public void parse(Model model, Resource implNode)
		throws RepositoryConfigException
	{
		super.parse(model, implNode);

		try {
			Models.objectLiteral(model.filter(implNode, ProxyRepositorySchema.PROXIED_ID, null)).ifPresent(
					lit -> setProxiedRepositoryID(lit.getLabel()));
		}
		catch (ModelException e) {
			throw new RepositoryConfigException(e.getMessage(), e);
		}
	}
}