/*
 * Copyright Aduna, DFKI and L3S (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.lucene.config;

import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.sail.lucene.LuceneSail;

/**
 * Defines constants for the LuceneSail schema which is used by
 * {@link LuceneSailFactory}s to initialize {@link LuceneSail}s.
 */
public class LuceneSailConfigSchema {
	
	/** The LuceneSail schema namespace (<tt>http://www.openrdf.org/config/sail/lucene#</tt>). */
	public static final String NAMESPACE = "http://www.openrdf.org/config/sail/lucene#";

	public static final URI INDEX_DIR;

	static {
		ValueFactory factory = ValueFactoryImpl.getInstance();
		INDEX_DIR = factory.createURI(NAMESPACE, "indexDir");
	}
}
