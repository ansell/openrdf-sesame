/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.memory.config;

import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.sail.memory.MemoryStore;

/**
 * Defines constants for the MemoryStore schema which is used by
 * {@link MemoryStoreFactory}s to initialize {@link MemoryStore}s.
 * 
 * @author Arjohn Kampman
 */
public class MemoryStoreSchema {

	/** The SailRepository schema namespace (<tt>http://www.openrdf.org/config/sail/memory#</tt>). */
	public static final String NAMESPACE = "http://www.openrdf.org/config/sail/memory#";

	/** <tt>http://www.openrdf.org/config/sail/memory#persist</tt> */
	public final static URI PERSIST;

	/** <tt>http://www.openrdf.org/config/sail/memory#syncDelay</tt> */
	public final static URI SYNC_DELAY;

	static {
		ValueFactory factory = new ValueFactoryImpl();
		PERSIST = factory.createURI(NAMESPACE, "persist");
		SYNC_DELAY = factory.createURI(NAMESPACE, "syncDelay");
	}
}
