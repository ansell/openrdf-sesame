/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.resultio;

import info.aduna.lang.service.FileFormatServiceRegistry;

/**
 * A registry that keeps track of the available
 * {@link TupleQueryResultWriterFactory}s.
 * 
 * @author Arjohn Kampman
 */
public class TupleQueryResultWriterRegistry extends
		FileFormatServiceRegistry<TupleQueryResultFormat, TupleQueryResultWriterFactory>
{

	private static TupleQueryResultWriterRegistry defaultRegistry;

	/**
	 * Gets the default TupleQueryResultWriterRegistry.
	 * 
	 * @return The default registry.
	 */
	public static synchronized TupleQueryResultWriterRegistry getInstance() {
		if (defaultRegistry == null) {
			defaultRegistry = new TupleQueryResultWriterRegistry();
		}

		return defaultRegistry;
	}

	public TupleQueryResultWriterRegistry() {
		super(TupleQueryResultWriterFactory.class);
	}
}
