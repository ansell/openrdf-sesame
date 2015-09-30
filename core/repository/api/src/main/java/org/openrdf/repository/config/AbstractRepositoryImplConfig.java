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
