/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.config;

import info.aduna.lang.service.ServiceRegistry;

/**
 * A registry that keeps track of the available {@link SailFactory}s.
 * 
 * @author Arjohn Kampman
 */
public class SailRegistry extends ServiceRegistry<String, SailFactory> {

	private static SailRegistry defaultRegistry;

	/**
	 * Gets the default SailRegistry.
	 * 
	 * @return The default registry.
	 */
	public static SailRegistry getInstance() {
		if (defaultRegistry == null) {
			defaultRegistry = new SailRegistry();
		}

		return defaultRegistry;
	}

	public SailRegistry() {
		super(SailFactory.class);
	}

	protected String getKey(SailFactory factory) {
		return factory.getSailType();
	}
}
