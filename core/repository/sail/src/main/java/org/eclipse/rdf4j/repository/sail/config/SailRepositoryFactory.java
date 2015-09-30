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

import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.config.RepositoryConfigException;
import org.eclipse.rdf4j.repository.config.RepositoryFactory;
import org.eclipse.rdf4j.repository.config.RepositoryImplConfig;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.Sail;
import org.eclipse.rdf4j.sail.StackableSail;
import org.eclipse.rdf4j.sail.config.DelegatingSailImplConfig;
import org.eclipse.rdf4j.sail.config.SailConfigException;
import org.eclipse.rdf4j.sail.config.SailFactory;
import org.eclipse.rdf4j.sail.config.SailImplConfig;
import org.eclipse.rdf4j.sail.config.SailRegistry;

/**
 * A {@link RepositoryFactory} that creates {@link SailRepository}s based on RDF
 * configuration data.
 * 
 * @author Arjohn Kampman
 */
public class SailRepositoryFactory implements RepositoryFactory {

	/*-----------*
	 * Constants *
	 *-----------*/

	/**
	 * The type of repositories that are created by this factory.
	 * 
	 * @see RepositoryFactory#getRepositoryType()
	 */
	public static final String REPOSITORY_TYPE = "openrdf:SailRepository";

	/*---------*
	 * Methods *
	 *---------*/

	/**
	 * Returns the repository's type: <tt>openrdf:SailRepository</tt>.
	 */
	public String getRepositoryType() {
		return REPOSITORY_TYPE;
	}

	public RepositoryImplConfig getConfig() {
		return new SailRepositoryConfig();
	}

	public Repository getRepository(RepositoryImplConfig config)
		throws RepositoryConfigException
	{
		if (config instanceof SailRepositoryConfig) {
			SailRepositoryConfig sailRepConfig = (SailRepositoryConfig)config;

			try {
				Sail sail = createSailStack(sailRepConfig.getSailImplConfig());
				return new SailRepository(sail);
			}
			catch (SailConfigException e) {
				throw new RepositoryConfigException(e.getMessage(), e);
			}
		}

		throw new RepositoryConfigException("Invalid configuration class: " + config.getClass());
	}

	private Sail createSailStack(SailImplConfig config)
		throws RepositoryConfigException, SailConfigException
	{
		Sail sail = createSail(config);

		if (config instanceof DelegatingSailImplConfig) {
			SailImplConfig delegateConfig = ((DelegatingSailImplConfig)config).getDelegate();
			if (delegateConfig != null) {
				addDelegate(delegateConfig, sail);
			}
		}

		return sail;
	}

	private Sail createSail(SailImplConfig config)
		throws RepositoryConfigException, SailConfigException
	{
		SailFactory sailFactory = SailRegistry.getInstance().get(config.getType()).orElseThrow(
				() -> new RepositoryConfigException("Unsupported Sail type: " + config.getType()));
		return sailFactory.getSail(config);
	}

	private void addDelegate(SailImplConfig config, Sail sail)
		throws RepositoryConfigException, SailConfigException
	{
		Sail delegateSail = createSailStack(config);

		try {
			((StackableSail)sail).setBaseSail(delegateSail);
		}
		catch (ClassCastException e) {
			throw new RepositoryConfigException("Delegate configured but " + sail.getClass()
					+ " is not a StackableSail");
		}
	}
}
