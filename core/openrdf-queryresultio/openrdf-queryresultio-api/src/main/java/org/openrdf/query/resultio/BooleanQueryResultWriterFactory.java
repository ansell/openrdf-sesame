/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.resultio;

import java.io.OutputStream;

/**
 * Returns {@link BooleanQueryResultWriter}s for a specific boolean query
 * result format.
 * 
 * @author Arjohn Kampman
 */
public interface BooleanQueryResultWriterFactory {

	/**
	 * Returns the boolean query result format for this factory.
	 */
	public BooleanQueryResultFormat getBooleanQueryResultFormat();

	/**
	 * Returns a {@link BooleanQueryResultWriter} instance that will write to the
	 * supplied output stream.
	 * 
	 * @param out
	 *        The OutputStream to write the result to.
	 */
	public BooleanQueryResultWriter getWriter(OutputStream out);
}
