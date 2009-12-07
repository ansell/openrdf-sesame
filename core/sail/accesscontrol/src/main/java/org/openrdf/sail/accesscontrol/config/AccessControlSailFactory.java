/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.accesscontrol.config;

import org.openrdf.sail.Sail;
import org.openrdf.sail.accesscontrol.AccessControlSail;
import org.openrdf.sail.config.SailFactory;
import org.openrdf.sail.config.SailImplConfig;
import org.openrdf.store.StoreConfigException;

/**
 * A {@link SailFactory} that creates {@link NativeStore}s based on RDF
 * configuration data.
 * 
 * @author Arjohn Kampman
 */
public class AccessControlSailFactory implements SailFactory {

	/**
	 * The type of repositories that are created by this factory.
	 * 
	 * @see SailFactory#getSailType()
	 */
	public static final String SAIL_TYPE = "openrdf:AccessControlSail";

	/**
	 * Returns the Sail's type: <tt>openrdf:AccessControlSail</tt>.
	 */
	public String getSailType() {
		return SAIL_TYPE;
	}

	public SailImplConfig getConfig() {
		return new AccessControlSailConfig();
	}

	public Sail getSail(SailImplConfig config)
		throws StoreConfigException
	{
		if (!SAIL_TYPE.equals(config.getType())) {
			throw new StoreConfigException("Invalid Sail type: " + config.getType());
		}

		return new AccessControlSail();
	}
}
