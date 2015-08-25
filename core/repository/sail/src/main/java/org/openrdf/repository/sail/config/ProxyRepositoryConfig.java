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
package org.openrdf.repository.sail.config;

import org.openrdf.model.Graph;
import org.openrdf.model.Literal;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.impl.SimpleValueFactory;
import org.openrdf.model.util.GraphUtil;
import org.openrdf.model.util.GraphUtilException;
import org.openrdf.model.util.ModelException;
import org.openrdf.model.util.Models;
import org.openrdf.repository.config.AbstractRepositoryImplConfig;
import org.openrdf.repository.config.RepositoryConfigException;

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