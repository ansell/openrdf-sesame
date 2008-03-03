/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.config;

import org.openrdf.sail.config.SailConfigException;
import org.openrdf.sail.config.SailFactory;
import org.openrdf.sail.config.SailImplConfig;
import org.openrdf.sail.rdbms.RdbmsStore;

/**
 * Creates a {@link RdbmsStore} from a {@link RdbmsStoreConfig}.
 * 
 * @author James Leigh
 * 
 */
public class RdbmsStoreFactory implements SailFactory {
	public static final String SAIL_TYPE = "openrdf:RdbmsStore";

	public String getSailType() {
		return SAIL_TYPE;
	}

	public RdbmsStoreConfig getConfig() {
		return new RdbmsStoreConfig();
	}

	public RdbmsStore getSail(SailImplConfig config) throws SailConfigException {
		if (!SAIL_TYPE.equals(config.getType())) {
			throw new SailConfigException("Invalid Sail type: "
					+ config.getType());
		}

		RdbmsStoreConfig rdbms = (RdbmsStoreConfig) config;
		String jdbcDriver = rdbms.getJdbcDriver();
		String url = rdbms.getUrl();
		String user = rdbms.getUser();
		String password = rdbms.getPassword();
		String layout = rdbms.getLayout();
		RdbmsStore store = new RdbmsStore(jdbcDriver, url, user, password);
		if ("layout2".equals(layout)) {
			store.setMaxNumberOfTripleTables(1);
		} else {
			assert "layout3".equals(layout) : layout;
		}
		return store;
	}

}
