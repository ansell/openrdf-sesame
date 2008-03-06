/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.resultio;

import java.io.IOException;

/**
 * The interface of objects that writer query results in a specific query result
 * format.
 * 
 * @author Arjohn Kampman
 */
public interface BooleanQueryResultWriter {

	/**
	 * Gets the query result format that this writer uses.
	 */
	public BooleanQueryResultFormat getBooleanQueryResultFormat();

	/**
	 * Writes the specified boolean value.
	 */
	public void write(boolean value)
		throws IOException;
}
