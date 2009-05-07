/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.resultio;

/**
 * Returns {@link BooleanQueryResultParser}s for a specific boolean query result
 * format.
 * 
 * @author Arjohn Kampman
 */
public interface BooleanQueryResultParserFactory {

	/**
	 * Returns the boolean query result format for this factory.
	 */
	public BooleanQueryResultFormat getBooleanQueryResultFormat();

	/**
	 * Returns a BooleanQueryResultParser instance.
	 */
	public BooleanQueryResultParser getParser();
}
