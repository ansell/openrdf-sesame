/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.accesscontrol.config;

import org.openrdf.sail.config.DelegatingSailImplConfigBase;
import org.openrdf.sail.config.SailImplConfig;

/**
 * @author Jeen Broekstra
 */
public class AccessControlSailConfig extends DelegatingSailImplConfigBase {

	public AccessControlSailConfig() {
		super(AccessControlSailFactory.SAIL_TYPE);
	}

	public AccessControlSailConfig(SailImplConfig delegate) {
		super(AccessControlSailFactory.SAIL_TYPE, delegate);
	}
}
