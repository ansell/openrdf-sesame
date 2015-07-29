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
package org.openrdf.sail.federation.config;

import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.repository.config.RepositoryFactory;
import org.openrdf.repository.config.RepositoryImplConfig;
import org.openrdf.repository.config.RepositoryRegistry;
import org.openrdf.sail.Sail;
import org.openrdf.sail.config.SailConfigException;
import org.openrdf.sail.config.SailFactory;
import org.openrdf.sail.config.SailImplConfig;
import org.openrdf.sail.federation.Federation;

/**
 * Creates a federation based on its configuration.
 * 
 * @see FederationConfig
 * @author James Leigh
 */
public class FederationFactory implements SailFactory {

	/**
	 * The type of repositories that are created by this factory.
	 * 
	 * @see SailFactory#getSailType()
	 */
	public static final String SAIL_TYPE = "openrdf:Federation";

	/**
	 * Returns the Sail's type: <tt>openrdf:Federation</tt>.
	 */
	public String getSailType() {
		return SAIL_TYPE;
	}

	public SailImplConfig getConfig() {
		return new FederationConfig();
	}

	public Sail getSail(SailImplConfig config)
		throws SailConfigException
	{
		if (!SAIL_TYPE.equals(config.getType())) {
			throw new SailConfigException("Invalid Sail type: " + config.getType());
		}
		assert config instanceof FederationConfig;
		FederationConfig cfg = (FederationConfig)config;
		Federation sail = new Federation();
		for (RepositoryImplConfig member : cfg.getMembers()) {
			RepositoryFactory factory = RepositoryRegistry.getInstance().get(member.getType()).orElseThrow(
					() -> new SailConfigException("Unsupported repository type: " + config.getType()));
			try {
				sail.addMember(factory.getRepository(member));
			}
			catch (RepositoryConfigException e) {
				throw new SailConfigException(e);
			}
		}
		sail.setLocalPropertySpace(cfg.getLocalPropertySpace());
		sail.setDistinct(cfg.isDistinct());
		sail.setReadOnly(cfg.isReadOnly());
		return sail;
	}
}
