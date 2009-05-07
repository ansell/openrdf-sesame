/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.rio;

import info.aduna.lang.service.FileFormatServiceRegistry;

/**
 * A registry that keeps track of the available {@link RDFWriterFactory}s.
 * 
 * @author Arjohn Kampman
 */
public class RDFWriterRegistry extends FileFormatServiceRegistry<RDFFormat, RDFWriterFactory> {

	private static RDFWriterRegistry defaultRegistry;

	/**
	 * Gets the default RDFWriterRegistry.
	 * 
	 * @return The default registry.
	 */
	public static synchronized RDFWriterRegistry getInstance() {
		if (defaultRegistry == null) {
			defaultRegistry = new RDFWriterRegistry();
		}

		return defaultRegistry;
	}

	public RDFWriterRegistry() {
		super(RDFWriterFactory.class);
	}

	@Override
	protected RDFFormat getKey(RDFWriterFactory factory) {
		return factory.getRDFFormat();
	}
}
