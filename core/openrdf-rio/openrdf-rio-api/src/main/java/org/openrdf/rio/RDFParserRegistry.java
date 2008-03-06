/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.rio;

import info.aduna.lang.service.ServiceRegistry;

/**
 * A registry that keeps track of the available {@link RDFParserFactory}s.
 * 
 * @author Arjohn Kampman
 */
public class RDFParserRegistry extends ServiceRegistry<RDFFormat, RDFParserFactory> {

	public RDFParserRegistry() {
		super(RDFParserFactory.class);
	}

	protected RDFFormat getKey(RDFParserFactory factory) {
		return factory.getRDFFormat();
	}
}
