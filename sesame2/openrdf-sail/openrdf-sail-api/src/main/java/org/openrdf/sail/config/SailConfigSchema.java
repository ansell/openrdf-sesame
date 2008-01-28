/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.config;

import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 * Defines constants for the SailRepository schema which is used by
 * {@link SailRepositoryFactory}s to initialize {@link SailRepository}s.
 * 
 * @author Arjohn Kampman
 */
public class SailConfigSchema {

	/** The Sail API schema namespace (<tt>http://www.openrdf.org/config/sail#</tt>). */
	public static final String NAMESPACE = "http://www.openrdf.org/config/sail#";

	/** <tt>http://www.openrdf.org/config/sail#sailType</tt> */
	public final static URI SAILTYPE;

	/** <tt>http://www.openrdf.org/config/sail#delegate</tt> */
	public final static URI DELEGATE;

	static {
		ValueFactory factory = ValueFactoryImpl.getInstance();
		SAILTYPE = factory.createURI(NAMESPACE, "sailType");
		DELEGATE = factory.createURI(NAMESPACE, "delegate");
	}
}
