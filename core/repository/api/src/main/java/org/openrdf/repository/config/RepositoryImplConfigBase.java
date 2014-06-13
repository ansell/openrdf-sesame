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

import static org.openrdf.repository.config.RepositoryConfigSchema.REPOSITORYTYPE;

import java.util.Optional;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.util.Models;
import org.openrdf.model.util.GraphUtilException;

/**
 * @author Herko ter Horst
 */
public class RepositoryImplConfigBase implements RepositoryImplConfig {

	private String type;

	/**
	 * Create a new RepositoryConfigImpl.
	 */
	public RepositoryImplConfigBase() {
	}

	/**
	 * Create a new RepositoryConfigImpl.
	 */
	public RepositoryImplConfigBase(String type) {
		this();
		setType(type);
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void validate()
		throws RepositoryConfigException
	{
		if (type == null) {
			throw new RepositoryConfigException("No type specified for repository implementation");
		}
	}

	public Resource export(Model graph) {
		BNode implNode = ValueFactoryImpl.getInstance().createBNode();

		if (type != null) {
			graph.add(implNode, REPOSITORYTYPE, ValueFactoryImpl.getInstance().createLiteral(type));
		}

		return implNode;
	}

	public void parse(Model graph, Resource implNode)
		throws RepositoryConfigException
	{
		try {
			Optional<Literal> typeLit = Models.getOptionalObjectLiteral(graph, implNode, REPOSITORYTYPE);
			if (typeLit.isPresent()) {
				setType(typeLit.get().getLabel());
			}
		}
		catch (GraphUtilException e) {
			throw new RepositoryConfigException(e.getMessage(), e);
		}
	}

	public static RepositoryImplConfig create(Model graph, Resource implNode)
		throws RepositoryConfigException
	{
		try {
			Optional<Literal> typeLit = Models.getOptionalObjectLiteral(graph, implNode, REPOSITORYTYPE);

			if (typeLit.isPresent()) {
				RepositoryFactory factory = RepositoryRegistry.getInstance().get(typeLit.get().getLabel());

				if (factory == null) {
					throw new RepositoryConfigException("Unsupported repository type: " + typeLit.get().getLabel());
				}

				RepositoryImplConfig implConfig = factory.getConfig();
				implConfig.parse(graph, implNode);
				return implConfig;
			}

			return null;
		}
		catch (GraphUtilException e) {
			throw new RepositoryConfigException(e.getMessage(), e);
		}
	}
}
