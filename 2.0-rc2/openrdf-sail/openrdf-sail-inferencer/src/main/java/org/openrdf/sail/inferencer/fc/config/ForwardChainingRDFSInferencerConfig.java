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
public class ForwardChainingRDFSInferencerConfig extends DelegatingSailImplConfigBase {

	public ForwardChainingRDFSInferencerConfig() {
		super(ForwardChainingRDFSInferencerFactory.SAIL_TYPE);
	}

	public ForwardChainingRDFSInferencerConfig(SailImplConfig delegate) {
		super(ForwardChainingRDFSInferencerFactory.SAIL_TYPE, delegate);
	}
}
