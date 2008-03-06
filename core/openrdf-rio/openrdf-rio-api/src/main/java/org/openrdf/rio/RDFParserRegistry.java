/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.rio;

import info.aduna.lang.service.FileFormatServiceRegistry;

/**
 * A registry that keeps track of the available {@link RDFParserFactory}s.
 * 
 * @author Arjohn Kampman
 */
public class RDFParserRegistry extends FileFormatServiceRegistry<RDFFormat, RDFParserFactory> {

	private static RDFParserRegistry defaultRegistry;

	/**
	 * Gets the default RDFParserRegistry.
	 * 
	 * @return The default registry.
	 */
	public static RDFParserRegistry getInstance() {
		if (defaultRegistry == null) {
			defaultRegistry = new RDFParserRegistry();
		}

		return defaultRegistry;
	}

	public RDFParserRegistry() {
		super(RDFParserFactory.class);
	}

	@Override
	protected RDFFormat getKey(RDFParserFactory factory) {
		return factory.getRDFFormat();
	}
}
