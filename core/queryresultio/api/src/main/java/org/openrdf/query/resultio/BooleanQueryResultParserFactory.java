/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.resultio;

import info.aduna.lang.service.FileFormatService;

/**
 * Returns {@link BooleanQueryResultParser}s for a specific boolean query result
 * format.
 * 
 * @author Arjohn Kampman
 */
public interface BooleanQueryResultParserFactory extends FileFormatService<BooleanQueryResultFormat> {

	/**
	 * Returns a BooleanQueryResultParser instance.
	 */
	public BooleanQueryResultParser getParser();
}
