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
package org.openrdf.sail.nativerdf.config;

import org.openrdf.sail.Sail;
import org.openrdf.sail.config.SailConfigException;
import org.openrdf.sail.config.SailFactory;
import org.openrdf.sail.config.SailImplConfig;
import org.openrdf.sail.nativerdf.NativeStore;

/**
 * A {@link SailFactory} that creates {@link NativeStore}s based on RDF
 * configuration data.
 * 
 * @author Arjohn Kampman
 */
public class NativeStoreFactory implements SailFactory {

	/**
	 * The type of repositories that are created by this factory.
	 * 
	 * @see SailFactory#getSailType()
	 */
	public static final String SAIL_TYPE = "openrdf:NativeStore";

	/**
	 * Returns the Sail's type: <tt>openrdf:NativeStore</tt>.
	 */
	public String getSailType() {
		return SAIL_TYPE;
	}

	public SailImplConfig getConfig() {
		return new NativeStoreConfig();
	}

	public Sail getSail(SailImplConfig config)
		throws SailConfigException
	{
		if (!SAIL_TYPE.equals(config.getType())) {
			throw new SailConfigException("Invalid Sail type: " + config.getType());
		}

		NativeStore nativeStore = new NativeStore();

		if (config instanceof NativeStoreConfig) {
			NativeStoreConfig nativeConfig = (NativeStoreConfig)config;

			nativeStore.setTripleIndexes(nativeConfig.getTripleIndexes());
			nativeStore.setForceSync(nativeConfig.getForceSync());

			if (nativeConfig.getValueCacheSize() >= 0) {
				nativeStore.setValueCacheSize(nativeConfig.getValueCacheSize());
			}
			if (nativeConfig.getValueIDCacheSize() >= 0) {
				nativeStore.setValueIDCacheSize(nativeConfig.getValueIDCacheSize());
			}
			if (nativeConfig.getNamespaceCacheSize() >= 0) {
				nativeStore.setNamespaceCacheSize(nativeConfig.getNamespaceCacheSize());
			}
			if (nativeConfig.getNamespaceIDCacheSize() >= 0) {
				nativeStore.setNamespaceIDCacheSize(nativeConfig.getNamespaceIDCacheSize());
			}
			if (nativeConfig.getIterationCacheSize() > 0) {
				nativeStore.setIterationSyncThreshold(nativeConfig.getIterationCacheSize());
			}
		}

		return nativeStore;
	}
}
