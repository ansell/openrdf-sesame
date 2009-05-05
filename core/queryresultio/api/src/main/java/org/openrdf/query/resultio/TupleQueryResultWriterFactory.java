/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.resultio;

import java.io.OutputStream;

import info.aduna.lang.service.FileFormatService;

/**
 * Returns {@link TupleQueryResultWriter}s for a specific tuple query result
 * format.
 * 
 * @author Arjohn Kampman
 */
public interface TupleQueryResultWriterFactory extends FileFormatService<TupleQueryResultFormat> {

	/**
	 * Returns a TupleQueryResultWriter instance that will write to the supplied
	 * output stream.
	 * 
	 * @param out
	 *        The OutputStream to write the result to.
	 */
	public TupleQueryResultWriter getWriter(OutputStream out);
}
