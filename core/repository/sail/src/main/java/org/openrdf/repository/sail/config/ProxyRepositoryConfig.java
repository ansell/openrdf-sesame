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
import org.openrdf.model.Resource;
import org.openrdf.model.impl.SimpleValueFactory;
import org.openrdf.model.util.GraphUtil;
import org.openrdf.model.util.GraphUtilException;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.repository.config.AbstractRepositoryImplConfig;

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
	public void validate() throws RepositoryConfigException {
		super.validate();
		if (null == this.proxiedID) {
			throw new RepositoryConfigException(
					"No id specified for proxied repository");
		}
	}

	@Override
	public Resource export(Graph graph) {
		Resource implNode = super.export(graph);
		if (null != this.proxiedID) {
			graph.add(
					implNode,
					ProxyRepositorySchema.PROXIED_ID,
					SimpleValueFactory.getInstance().createLiteral(
							this.proxiedID));
		}
		return implNode;
	}

	@Override
	public void parse(Graph graph, Resource implNode)
			throws RepositoryConfigException {
		super.parse(graph, implNode);

		try {
			Literal proxiedID = GraphUtil.getOptionalObjectLiteral(graph,
					implNode, ProxyRepositorySchema.PROXIED_ID);
			if (proxiedID != null) {
				this.setProxiedRepositoryID(proxiedID.getLabel());
			}
		} catch (GraphUtilException e) {
			throw new RepositoryConfigException(e.getMessage(), e);
		}
	}
}