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
 * 
 */
public class RdbmsStoreSchema {

	public static final String NAMESPACE = "http://www.openrdf.org/config/sail/rdbms#";

	public final static URI JDBC_DRIVER;

	public final static URI URL;

	public final static URI USER;

	public final static URI PASSWORD;

	public final static URI LAYOUT;

	public final static URI INDEXED;

	static {
		ValueFactory factory = ValueFactoryImpl.getInstance();
		JDBC_DRIVER = factory.createURI(NAMESPACE, "jdbc-driver");
		URL = factory.createURI(NAMESPACE, "url");
		USER = factory.createURI(NAMESPACE, "user");
		PASSWORD = factory.createURI(NAMESPACE, "password");
		LAYOUT = factory.createURI(NAMESPACE, "layout");
		INDEXED = factory.createURI(NAMESPACE, "indexed");
	}
}
