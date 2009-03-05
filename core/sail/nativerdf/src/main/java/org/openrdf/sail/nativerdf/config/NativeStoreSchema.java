/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.nativerdf.config;

import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.sail.nativerdf.NativeStore;

/**
 * Defines constants for the NativeStore schema which is used by
 * {@link NativeStoreFactory}s to initialize {@link NativeStore}s.
 * 
 * @author Arjohn Kampman
 */
public class NativeStoreSchema {

	/**
	 * The NativeStore schema namespace (
	 * <tt>http://www.openrdf.org/config/sail/native#</tt>).
	 */
	public static final String NAMESPACE = "http://www.openrdf.org/config/sail/native#";

	/** <tt>http://www.openrdf.org/config/sail/native#tripleIndexes</tt> */
	public final static URI TRIPLE_INDEXES;

	/** <tt>http://www.openrdf.org/config/sail/native#forceSync</tt> */
	public final static URI FORCE_SYNC;

	static {
		ValueFactory factory = ValueFactoryImpl.getInstance();
		TRIPLE_INDEXES = factory.createURI(NAMESPACE, "tripleIndexes");
		FORCE_SYNC = factory.createURI(NAMESPACE, "forceSync");
	}
}
