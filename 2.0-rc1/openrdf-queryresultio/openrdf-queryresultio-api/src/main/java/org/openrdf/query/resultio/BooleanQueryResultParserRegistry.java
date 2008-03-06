/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.resultio;

import info.aduna.lang.service.FileFormatServiceRegistry;

/**
 * A registry that keeps track of the available
 * {@link BooleanQueryResultParserFactory}s.
 * 
 * @author Arjohn Kampman
 */
public class BooleanQueryResultParserRegistry extends
		FileFormatServiceRegistry<BooleanQueryResultFormat, BooleanQueryResultParserFactory>
{

	private static BooleanQueryResultParserRegistry defaultRegistry;

	/**
	 * Gets the default BooleanQueryResultParserRegistry.
	 * 
	 * @return The default registry.
	 */
	public static BooleanQueryResultParserRegistry getInstance() {
		if (defaultRegistry == null) {
			defaultRegistry = new BooleanQueryResultParserRegistry();
		}

		return defaultRegistry;
	}

	public BooleanQueryResultParserRegistry() {
		super(BooleanQueryResultParserFactory.class);
	}

	@Override
	protected BooleanQueryResultFormat getKey(BooleanQueryResultParserFactory factory) {
		return factory.getBooleanQueryResultFormat();
	}
}
