/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.rio;

import info.aduna.lang.service.ServiceRegistry;

/**
 * A registry that keeps track of the available {@link RDFWriterFactory}s.
 * 
 * @author Arjohn Kampman
 */
public class RDFWriterRegistry extends ServiceRegistry<RDFFormat, RDFWriterFactory> {

	public RDFWriterRegistry() {
		super(RDFWriterFactory.class);
	}

	protected RDFFormat getKey(RDFWriterFactory factory) {
		return factory.getRDFFormat();
	}
}
