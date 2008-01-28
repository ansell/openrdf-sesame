/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.resultio;

import java.io.IOException;
import java.io.InputStream;

import org.openrdf.model.ValueFactory;
import org.openrdf.query.TupleQueryResultHandler;
import org.openrdf.query.TupleQueryResultHandlerException;

/**
 * A general interface for tuple query result parsers.
 */
public interface TupleQueryResultParser {

	/**
	 * Gets the query result format that this parser can parse.
	 */
	public TupleQueryResultFormat getTupleQueryResultFormat();

	/**
	 * Sets the ValueFactory that the parser will use to create Value objects for
	 * the parsed query result.
	 * 
	 * @param valueFactory
	 *        The value factory that the parser should use.
	 */
	public void setValueFactory(ValueFactory valueFactory);

	/**
	 * Sets the TupleQueryResultHandler that will handle the parsed query result
	 * data.
	 */
	public void setTupleQueryResultHandler(TupleQueryResultHandler handler);

	/**
	 * Parses the data from the supplied InputStream.
	 * 
	 * @param in
	 *        The InputStream from which to read the data.
	 * @throws IOException
	 *         If an I/O error occurred while data was read from the InputStream.
	 * @throws QueryResultParseException
	 *         If the parser has encountered an unrecoverable parse error.
	 * @throws TupleQueryResultHandlerException
	 *         If the configured query result handler has encountered an
	 *         unrecoverable error.
	 */
	public void parse(InputStream in)
		throws IOException, QueryResultParseException, TupleQueryResultHandlerException;
}
