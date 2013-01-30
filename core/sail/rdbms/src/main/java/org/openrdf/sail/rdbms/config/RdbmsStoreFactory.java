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
package org.openrdf.sail.rdbms.config;

import org.openrdf.sail.config.SailConfigException;
import org.openrdf.sail.config.SailFactory;
import org.openrdf.sail.config.SailImplConfig;
import org.openrdf.sail.rdbms.RdbmsStore;

/**
 * A {@link SailFactory} that creates {@link RdbmsStore}s based on RDF
 * configuration data.
 * 
 * @author James Leigh
 */
public class RdbmsStoreFactory implements SailFactory {

	/**
	 * The type of repositories that are created by this factory.
	 * 
	 * @see SailFactory#getSailType()
	 */
	public static final String SAIL_TYPE = "openrdf:RdbmsStore";

	/**
	 * Returns the Sail's type: <tt>openrdf:RdbmsStore</tt>.
	 */
	public String getSailType() {
		return SAIL_TYPE;
	}

	public RdbmsStoreConfig getConfig() {
		return new RdbmsStoreConfig();
	}

	public RdbmsStore getSail(SailImplConfig config)
		throws SailConfigException
	{
		if (!SAIL_TYPE.equals(config.getType())) {
			throw new SailConfigException("Invalid Sail type: " + config.getType());
		}

		if (config instanceof RdbmsStoreConfig) {
			RdbmsStoreConfig rdbmsConfig = (RdbmsStoreConfig)config;

			String jdbcDriver = rdbmsConfig.getJdbcDriver();
			String url = rdbmsConfig.getUrl();
			String user = rdbmsConfig.getUser();
			String password = rdbmsConfig.getPassword();

			RdbmsStore store = new RdbmsStore(jdbcDriver, url, user, password);

			store.setMaxNumberOfTripleTables(rdbmsConfig.getMaxTripleTables());

			return store;
		}

		throw new IllegalArgumentException("Supplied config objects should be an RdbmsStoreConfig, is: "
				+ config.getClass());
	}
}
