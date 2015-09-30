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
