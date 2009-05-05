/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.resultio;

import info.aduna.lang.service.FileFormatService;

/**
 * A TupleQueryResultParserFactory returns {@link TupleQueryResultParser}s for a
 * specific tuple query result format.
 * 
 * @author Arjohn Kampman
 */
public interface TupleQueryResultParserFactory extends FileFormatService<TupleQueryResultFormat> {

	/**
	 * Returns a TupleQueryResultParser instance.
	 */
	public TupleQueryResultParser getParser();
}
