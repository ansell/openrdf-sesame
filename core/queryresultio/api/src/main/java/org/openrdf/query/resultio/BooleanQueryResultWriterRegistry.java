/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.resultio;

import info.aduna.lang.service.FileFormatServiceRegistry;

/**
 * A registry that keeps track of the available
 * {@link BooleanQueryResultWriterFactory}s.
 * 
 * @author Arjohn Kampman
 */
public class BooleanQueryResultWriterRegistry extends
		FileFormatServiceRegistry<BooleanQueryResultFormat, BooleanQueryResultWriterFactory>
{

	private static BooleanQueryResultWriterRegistry defaultRegistry;

	/**
	 * Gets the default BooleanQueryResultWriterRegistry.
	 * 
	 * @return The default registry.
	 */
	public static synchronized BooleanQueryResultWriterRegistry getInstance() {
		if (defaultRegistry == null) {
			defaultRegistry = new BooleanQueryResultWriterRegistry();
		}

		return defaultRegistry;
	}

	public BooleanQueryResultWriterRegistry() {
		super(BooleanQueryResultWriterFactory.class);
	}

	@Override
	protected BooleanQueryResultFormat getKey(BooleanQueryResultWriterFactory factory) {
		return factory.getBooleanQueryResultFormat();
	}
}
