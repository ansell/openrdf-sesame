/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.http.config;

import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.repository.http.HTTPRepository;

/**
 * Defines constants for the HTTPRepository schema which is used by
 * {@link HTTPRepositoryFactory}s to initialize {@link HTTPRepository}s.
 * 
 * @author Arjohn Kampman
 */
public class HTTPRepositorySchema {

	/** The HTTPRepository schema namespace (<tt>http://www.openrdf.org/config/repository/http#</tt>). */
	public static final String NAMESPACE = "http://www.openrdf.org/config/repository/http#";

	/** <tt>http://www.openrdf.org/config/repository/http#repositoryURL</tt> */
	public final static URI REPOSITORYURL;

	static {
		ValueFactory factory = ValueFactoryImpl.getInstance();
		REPOSITORYURL = factory.createURI(NAMESPACE, "repositoryURL");
	}
}
