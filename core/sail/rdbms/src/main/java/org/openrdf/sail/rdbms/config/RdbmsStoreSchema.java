/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.config;

import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 * Vocabulary for the RDBMS configuration.
 * 
 * @author James Leigh
 */
public class RdbmsStoreSchema {

	public static final String NAMESPACE = "http://www.openrdf.org/config/sail/rdbms#";

	public final static URI JDBC_DRIVER;

	public final static URI URL_TEMPLATE;

	public final static URI HOST;

	public final static URI PORT;

	public final static URI DATABASE;

	public final static URI URL_PROPERTIES;

	public final static URI URL;

	public final static URI USER;

	public final static URI PASSWORD;

	public final static URI MAX_TRIPLE_TABLES;

	static {
		ValueFactory factory = ValueFactoryImpl.getInstance();
		JDBC_DRIVER = factory.createURI(NAMESPACE, "jdbcDriver");
		URL_TEMPLATE = factory.createURI(NAMESPACE, "urlTemplate");
		HOST = factory.createURI(NAMESPACE, "host");
		PORT = factory.createURI(NAMESPACE, "port");
		DATABASE = factory.createURI(NAMESPACE, "database");
		URL_PROPERTIES = factory.createURI(NAMESPACE, "urlProperties");
		URL = factory.createURI(NAMESPACE, "url");
		USER = factory.createURI(NAMESPACE, "user");
		PASSWORD = factory.createURI(NAMESPACE, "password");
		MAX_TRIPLE_TABLES = factory.createURI(NAMESPACE, "maxTripleTables");
	}
}
