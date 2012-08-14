/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.mysql;

import org.openrdf.sail.rdbms.RdbmsConnectionFactory;
import org.openrdf.sail.rdbms.RdbmsProvider;

/**
 * Checks the database product name and version to be compatible with this
 * Sesame store.
 * 
 * @author James Leigh
 * 
 */
public class MySqlProvider implements RdbmsProvider {

	public RdbmsConnectionFactory createRdbmsConnectionFactory(String dbName, String dbVersion) {
		if ("MySQL".equalsIgnoreCase(dbName))
			return new MySqlConnectionFactory();
		return null;
	}

}
