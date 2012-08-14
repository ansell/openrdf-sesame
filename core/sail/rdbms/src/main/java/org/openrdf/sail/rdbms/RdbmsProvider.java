/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms;

/**
 * Each supported relation store should implement this service provider
 * interface to initiate a connection factory tailored to the given product name
 * and version. If a provider does not support the given database name and
 * version it should not return a connection factory, but rather a null value.
 * 
 * @author James Leigh
 * 
 */
public interface RdbmsProvider {

	RdbmsConnectionFactory createRdbmsConnectionFactory(String dbName, String dbVersion);
}
