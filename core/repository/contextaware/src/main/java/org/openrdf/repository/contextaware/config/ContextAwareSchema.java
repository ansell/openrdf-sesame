/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
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

	/** The ContextAwareRepository schema namespace (<tt>http://www.openrdf.org/config/repository/contextaware#</tt>). */
	public static final String NAMESPACE = "http://www.openrdf.org/config/repository/contextaware#";

	/** <tt>http://www.openrdf.org/config/repository/contextaware#includeInferred</tt> */
	public final static URI INCLUDE_INFERRED;

	/** <tt>http://www.openrdf.org/config/repository/contextaware#ql</tt> */
	public final static URI QL;

	/** <tt>http://www.openrdf.org/config/repository/contextaware#readContext</tt> */
	public final static URI READ_CONTEXT;

	/** <tt>http://www.openrdf.org/config/repository/contextaware#addContext</tt> */
	public final static URI ADD_CONTEXT;

	/** <tt>http://www.openrdf.org/config/repository/contextaware#removeContext</tt> */
	public final static URI REMOVE_CONTEXT;

	/** <tt>http://www.openrdf.org/config/repository/contextaware#archiveContext</tt> */
	public final static URI ARCHIVE_CONTEXT;

	/** <tt>http://www.openrdf.org/config/repository/contextaware#delegate</tt> */
	public final static URI DELEGATE;

	static {
		ValueFactory factory = ValueFactoryImpl.getInstance();
		INCLUDE_INFERRED = factory.createURI(NAMESPACE, "includeInferred");
		QL = factory.createURI(NAMESPACE, "ql");
		READ_CONTEXT = factory.createURI(NAMESPACE, "readContext");
		ADD_CONTEXT = factory.createURI(NAMESPACE, "addContext");
		REMOVE_CONTEXT = factory.createURI(NAMESPACE, "removeContext");
		ARCHIVE_CONTEXT = factory.createURI(NAMESPACE, "archiveContext");
		DELEGATE = factory.createURI(NAMESPACE, "delegate");
	}
}
