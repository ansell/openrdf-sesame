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
			if (nativeConfig.getIterationCacheSyncThreshold() > 0) {
				nativeStore.setIterationCacheSyncThreshold(nativeConfig.getIterationCacheSyncThreshold());
			}
		}

		return nativeStore;
	}
}
