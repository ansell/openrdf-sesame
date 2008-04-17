/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.config;

import org.openrdf.sail.SailException;
import org.openrdf.sail.config.SailConfigException;
import org.openrdf.sail.config.SailFactory;
import org.openrdf.sail.config.SailImplConfig;
import org.openrdf.sail.rdbms.RdbmsStore;
import org.openrdf.sail.rdbms.config.RdbmsStoreConfig.DatabaseLayout;

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

			DatabaseLayout layout = rdbmsConfig.getLayout();
			if (DatabaseLayout.MONOLITHIC.equals(layout)) {
				store.setMaxNumberOfTripleTables(1);
			}

			return store;
		}
		
		throw new IllegalArgumentException("Supplied config objects should be a RdbmsStoreConfig, is: " + config.getClass());
	}

}
