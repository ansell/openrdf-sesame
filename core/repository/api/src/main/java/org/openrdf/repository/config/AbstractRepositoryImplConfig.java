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

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.impl.SimpleValueFactory;
import org.openrdf.model.util.ModelException;
import org.openrdf.model.util.Models;

/**
 * @author Herko ter Horst
 */
public class AbstractRepositoryImplConfig implements RepositoryImplConfig {

	private String type;

	/**
	 * Create a new RepositoryConfigImpl.
	 */
	public AbstractRepositoryImplConfig() {
	}

	/**
	 * Create a new RepositoryConfigImpl.
	 */
	public AbstractRepositoryImplConfig(String type) {
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

	public Resource export(Model model) {
		BNode implNode = SimpleValueFactory.getInstance().createBNode();

		if (type != null) {
			model.add(implNode, REPOSITORYTYPE, SimpleValueFactory.getInstance().createLiteral(type));
		}

		return implNode;
	}

	public void parse(Model model, Resource resource)
		throws RepositoryConfigException
	{
		Models.objectLiteral(model.filter(resource, REPOSITORYTYPE, null)).ifPresent(
				typeLit -> setType(typeLit.getLabel()));
	}

	/**
	 * Utility method to create a new {@link RepositoryImplConfig} by reading
	 * data from the supplied {@link Model}.
	 * 
	 * @param model
	 *        the {@link Model} to read configuration data from.
	 * @param implNode
	 *        the subject {@link Resource} identifying the configuration data in
	 *        the Model.
	 * @return a new {@link RepositoryImplConfig} initialized with the
	 *         configuration from the input Model, or {@code null} if no
	 *         {@link RepositoryConfigSchema#REPOSITORYTYPE} property was found
	 *         in the configuration data..
	 * @throws RepositoryConfigException
	 *         if an error occurred reading the configuration data from the
	 *         model.
	 */
	public static RepositoryImplConfig create(Model model, Resource resource)
		throws RepositoryConfigException
	{
		try {
			// Literal typeLit = GraphUtil.getOptionalObjectLiteral(graph,
			// implNode, REPOSITORYTYPE);

			final Literal typeLit = Models.objectLiteral(model.filter(resource, REPOSITORYTYPE, null)).orElse(
					null);
			if (typeLit != null) {
				RepositoryFactory factory = RepositoryRegistry.getInstance().get(typeLit.getLabel()).orElseThrow(
						() -> new RepositoryConfigException("Unsupported repository type: " + typeLit.getLabel()));

				RepositoryImplConfig implConfig = factory.getConfig();
				implConfig.parse(model, resource);
				return implConfig;
			}

			return null;
		}
		catch (ModelException e) {
			throw new RepositoryConfigException(e.getMessage(), e);
		}
	}
}
