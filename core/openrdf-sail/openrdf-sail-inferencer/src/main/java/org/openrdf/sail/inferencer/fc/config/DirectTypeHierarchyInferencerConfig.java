/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.inferencer.fc.config;

import org.openrdf.sail.config.DelegatingSailImplConfigBase;
import org.openrdf.sail.config.SailImplConfig;

/**
 * @author Arjohn Kampman
 */
public class DirectTypeHierarchyInferencerConfig extends DelegatingSailImplConfigBase {

	public DirectTypeHierarchyInferencerConfig() {
		super(DirectTypeHierarchyInferencerFactory.SAIL_TYPE);
	}

	public DirectTypeHierarchyInferencerConfig(SailImplConfig delegate) {
		super(DirectTypeHierarchyInferencerFactory.SAIL_TYPE, delegate);
	}
}
