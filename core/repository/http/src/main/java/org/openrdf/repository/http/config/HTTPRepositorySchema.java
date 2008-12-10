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

	/** <tt>http://www.openrdf.org/config/repository/http#serverURL</tt> */
	public final static URI SERVERURL;

	/** <tt>http://www.openrdf.org/config/repository/http#repositoryID/tt> */
	public final static URI REPOSITORYID;

	/** <tt>http://www.openrdf.org/config/repository/http#username</tt> */
	public final static URI USERNAME;

	/** <tt>http://www.openrdf.org/config/repository/http#password</tt> */
	public final static URI PASSWORD;

	/** <tt>http://www.openrdf.org/config/repository/http#subjectSpace</tt> */
	public final static URI SUBJECTSPACE;

	/** <tt>http://www.openrdf.org/config/repository/http#typeSpace</tt> */
	public final static URI TYPESPACE;

	static {
		ValueFactory factory = ValueFactoryImpl.getInstance();
		REPOSITORYURL = factory.createURI(NAMESPACE, "repositoryURL");
		SERVERURL = factory.createURI(NAMESPACE, "serverURL");
		REPOSITORYID = factory.createURI(NAMESPACE, "repositoryID");
		USERNAME = factory.createURI(NAMESPACE, "username");
		PASSWORD = factory.createURI(NAMESPACE, "password");
		SUBJECTSPACE = factory.createURI(NAMESPACE, "subjectSpace");
		TYPESPACE = factory.createURI(NAMESPACE, "typeSpace");
	}
}
