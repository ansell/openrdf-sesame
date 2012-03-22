/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.contextaware.config;

import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 * @author James Leigh
 */
public class ContextAwareSchema {

	/**
	 * The ContextAwareRepository schema namespace (
	 * <tt>http://www.openrdf.org/config/repository/contextaware#</tt>).
	 */
	public static final String NAMESPACE = "http://www.openrdf.org/config/repository/contextaware#";

	/** <tt>http://www.openrdf.org/config/repository/contextaware#includeInferred</tt> */
	public final static URI INCLUDE_INFERRED;

	/** <tt>http://www.openrdf.org/config/repository/contextaware#maxQueryTime</tt> */
	public final static URI MAX_QUERY_TIME;

	/** <tt>http://www.openrdf.org/config/repository/contextaware#queryLanguage</tt> */
	public final static URI QUERY_LANGUAGE;

	/** <tt>http://www.openrdf.org/config/repository/contextaware#base</tt> */
	public final static URI BASE_URI;

	/** <tt>http://www.openrdf.org/config/repository/contextaware#readContext</tt> */
	public final static URI READ_CONTEXT;

	/** <tt>http://www.openrdf.org/config/repository/contextaware#updateContext</tt> */
	public final static URI UPDATE_CONTEXT;

	static {
		ValueFactory factory = ValueFactoryImpl.getInstance();
		INCLUDE_INFERRED = factory.createURI(NAMESPACE, "includeInferred");
		QUERY_LANGUAGE = factory.createURI(NAMESPACE, "ql");
		BASE_URI = factory.createURI(NAMESPACE, "base");
		READ_CONTEXT = factory.createURI(NAMESPACE, "readContext");
		UPDATE_CONTEXT = factory.createURI(NAMESPACE, "updateContext");
		MAX_QUERY_TIME = factory.createURI(NAMESPACE, "maxQueryTime");
	}
}
