/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.memory.config;

import org.openrdf.sail.config.DelegatingSailImplConfigBase;
import org.openrdf.sail.config.SailImplConfig;

/**
 * @author Arjohn Kampman
 */
public class MemoryStoreRDFSInferencerConfig extends DelegatingSailImplConfigBase {

	public MemoryStoreRDFSInferencerConfig() {
		super(MemoryStoreRDFSInferencerFactory.SAIL_TYPE);
	}

	public MemoryStoreRDFSInferencerConfig(SailImplConfig delegate) {
		super(MemoryStoreRDFSInferencerFactory.SAIL_TYPE, delegate);
	}
}
