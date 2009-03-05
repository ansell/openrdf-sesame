/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.resultio;

/**
 * A TupleQueryResultParserFactory returns {@link TupleQueryResultParser}s for a
 * specific tuple query result format.
 * 
 * @author Arjohn Kampman
 */
public interface TupleQueryResultParserFactory {

	/**
	 * Returns the tuple query result format for this factory.
	 */
	public TupleQueryResultFormat getTupleQueryResultFormat();

	/**
	 * Returns a TupleQueryResultParser instance.
	 */
	public TupleQueryResultParser getParser();
}
