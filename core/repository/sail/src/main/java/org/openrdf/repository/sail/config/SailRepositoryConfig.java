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

import static org.openrdf.repository.sail.config.SailRepositorySchema.SAILIMPL;
import static org.openrdf.sail.config.SailConfigSchema.SAILTYPE;

import java.util.Optional;

import org.openrdf.model.Literal;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.util.GraphUtil;
import org.openrdf.model.util.GraphUtilException;
import org.openrdf.model.util.ModelException;
import org.openrdf.model.util.Models;
import org.openrdf.repository.config.AbstractRepositoryImplConfig;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.sail.config.SailConfigException;
import org.openrdf.sail.config.SailFactory;
import org.openrdf.sail.config.SailImplConfig;
import org.openrdf.sail.config.SailRegistry;

/**
 * @author Arjohn Kampman
 */
public class SailRepositoryConfig extends AbstractRepositoryImplConfig {

	private SailImplConfig sailImplConfig;

	public SailRepositoryConfig() {
		super(SailRepositoryFactory.REPOSITORY_TYPE);
	}

	public SailRepositoryConfig(SailImplConfig sailImplConfig) {
		this();
		setSailImplConfig(sailImplConfig);
	}

	public SailImplConfig getSailImplConfig() {
		return sailImplConfig;
	}

	public void setSailImplConfig(SailImplConfig sailImplConfig) {
		this.sailImplConfig = sailImplConfig;
	}

	@Override
	public void validate()
		throws RepositoryConfigException
	{
		super.validate();
		if (sailImplConfig == null) {
			throw new RepositoryConfigException("No Sail implementation specified for Sail repository");
		}

		try {
			sailImplConfig.validate();
		}
		catch (SailConfigException e) {
			throw new RepositoryConfigException(e.getMessage(), e);
		}
	}

	@Override
	public Resource export(Model model) {
		Resource repImplNode = super.export(model);

		if (sailImplConfig != null) {
			Resource sailImplNode = sailImplConfig.export(model);
			model.add(repImplNode, SAILIMPL, sailImplNode);
		}

		return repImplNode;
	}

	@Override
	public void parse(Model model, Resource repImplNode)
		throws RepositoryConfigException
	{
		try {
			Optional<Resource> sailImplNode = Models.objectResource(model.filter(repImplNode, SAILIMPL, null));
			if (sailImplNode.isPresent()) {
				Models.objectLiteral(model.filter(sailImplNode.get(), SAILTYPE, null)).ifPresent(typeLit -> {
					SailFactory factory = SailRegistry.getInstance().get(typeLit.getLabel()).orElseThrow(
							() -> new RepositoryConfigException("Unsupported Sail type: " + typeLit.getLabel()));

					sailImplConfig = factory.getConfig();
					sailImplConfig.parse(model, sailImplNode.get());
				});
			}
		}
		catch (ModelException e) {
			throw new RepositoryConfigException(e.getMessage(), e);
		}
		catch (SailConfigException e) {
			throw new RepositoryConfigException(e.getMessage(), e);
		}
	}
}
