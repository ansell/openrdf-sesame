/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.config;

import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 * Defines constants for the repository configuration schema that is used by
 * {@link RepositoryManager}s.
 * 
 * @author Arjohn Kampman
 */
public class RepositoryConfigSchema {

	/** The HTTPRepository schema namespace (<tt>http://www.openrdf.org/config/repository#</tt>). */
	public static final String NAMESPACE = "http://www.openrdf.org/config/repository#";

	/** <tt>http://www.openrdf.org/config/repository#RepositoryContext</tt> */
	public final static URI REPOSITORY_CONTEXT;

	/** <tt>http://www.openrdf.org/config/repository#Repository</tt> */
	public final static URI REPOSITORY;

	/** <tt>http://www.openrdf.org/config/repository#repositoryID</tt> */
	public final static URI REPOSITORYID;

	/** <tt>http://www.openrdf.org/config/repository#repositoryTitle</tt> */
	public final static URI REPOSITORYTITLE;

	/** <tt>http://www.openrdf.org/config/repository#repositoryImpl</tt> */
	public final static URI REPOSITORYIMPL;

	/** <tt>http://www.openrdf.org/config/repository#repositoryType</tt> */
	public final static URI REPOSITORYTYPE;

	/** <tt>http://www.openrdf.org/config/repository#delegate</tt> */
	public final static URI DELEGATE;

	static {
		ValueFactory factory = ValueFactoryImpl.getInstance();
		REPOSITORY_CONTEXT = factory.createURI(NAMESPACE, "RepositoryContext");
		REPOSITORY = factory.createURI(NAMESPACE, "Repository");
		REPOSITORYID = factory.createURI(NAMESPACE, "repositoryID");
		REPOSITORYTITLE = factory.createURI(NAMESPACE, "repositoryTitle");
		REPOSITORYIMPL = factory.createURI(NAMESPACE, "repositoryImpl");
		REPOSITORYTYPE = factory.createURI(NAMESPACE, "repositoryType");
		DELEGATE = factory.createURI(NAMESPACE, "delegate");
	}
}
