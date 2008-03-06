/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.sail.config;

import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.repository.sail.SailRepository;

/**
 * Defines constants for the SailRepository schema which is used by
 * {@link SailRepositoryFactory}s to initialize {@link SailRepository}s.
 * 
 * @author Arjohn Kampman
 */
public class SailRepositorySchema {

	/** The SailRepository schema namespace (<tt>http://www.openrdf.org/config/repository/sail#</tt>). */
	public static final String NAMESPACE = "http://www.openrdf.org/config/repository/sail#";

	/** <tt>http://www.openrdf.org/config/repository/sail#sailImpl</tt> */
	public final static URI SAILIMPL;

	static {
		ValueFactory factory = new ValueFactoryImpl();
		SAILIMPL = factory.createURI(NAMESPACE, "sailImpl");
	}
}
