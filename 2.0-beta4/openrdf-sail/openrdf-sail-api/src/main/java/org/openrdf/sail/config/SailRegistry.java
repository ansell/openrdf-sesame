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

	private static SailRegistry sharedInstance;

	public static SailRegistry getInstance() {
		if (sharedInstance == null) {
			sharedInstance = new SailRegistry();
		}

		return sharedInstance;
	}

	public SailRegistry() {
		super(SailFactory.class);
	}

	protected String getKey(SailFactory factory) {
		return factory.getSailType();
	}
}
