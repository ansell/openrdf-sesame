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
package org.openrdf.sail.memory.config;

import org.openrdf.query.algebra.evaluation.federation.FederatedServiceResolver;
import org.openrdf.query.algebra.evaluation.federation.FederatedServiceResolverClient;
import org.openrdf.sail.Sail;
import org.openrdf.sail.config.SailConfigException;
import org.openrdf.sail.config.SailFactory;
import org.openrdf.sail.config.SailImplConfig;
import org.openrdf.sail.memory.MemoryStore;

/**
 * A {@link SailFactory} that creates {@link MemoryStore}s based on RDF
 * configuration data.
 * 
 * @author Arjohn Kampman
 */
public class MemoryStoreFactory implements SailFactory, FederatedServiceResolverClient {

	/**
	 * The type of repositories that are created by this factory.
	 * 
	 * @see SailFactory#getSailType()
	 */
	public static final String SAIL_TYPE = "openrdf:MemoryStore";

	private FederatedServiceResolver serviceResolver;

	/**
	 * Returns the Sail's type: <tt>openrdf:MemoryStore</tt>.
	 */
	public String getSailType() {
		return SAIL_TYPE;
	}

	public SailImplConfig getConfig() {
		return new MemoryStoreConfig();
	}

	public FederatedServiceResolver getFederatedServiceResolver() {
		return serviceResolver;
	}

	@Override
	public void setFederatedServiceResolver(FederatedServiceResolver resolver) {
		this.serviceResolver = resolver;
	}

	public Sail getSail(SailImplConfig config)
		throws SailConfigException
	{
		if (!SAIL_TYPE.equals(config.getType())) {
			throw new SailConfigException("Invalid Sail type: " + config.getType());
		}

		MemoryStore memoryStore = new MemoryStore();

		if (config instanceof MemoryStoreConfig) {
			MemoryStoreConfig memConfig = (MemoryStoreConfig)config;

			memoryStore.setPersist(memConfig.getPersist());
			memoryStore.setSyncDelay(memConfig.getSyncDelay());
			memoryStore.setFederatedServiceResolver(getFederatedServiceResolver());
			
			if (memConfig.getIterationCacheSize() > 0) {
				memoryStore.setIterationSyncThreshold(memConfig.getIterationCacheSize());
			}
		}

		return memoryStore;
	}
}
