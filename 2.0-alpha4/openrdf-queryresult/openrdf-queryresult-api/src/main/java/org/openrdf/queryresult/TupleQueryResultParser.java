/*  Copyright (C) 2001-2006 Aduna (http://www.aduna-software.com/)
 *
 *  This software is part of the Sesame Framework. It is licensed under
 *  the following two licenses as alternatives:
 *
 *   1. Open Software License (OSL) v3.0
 *   2. GNU Lesser General Public License (LGPL) v2.1 or any newer
 *      version
 *
 *  By using, modifying or distributing this software you agree to be 
 *  bound by the terms of at least one of the above licenses.
 *
 *  See the file LICENSE.txt that is distributed with this software
 *  for the complete terms and further details.
 */
package org.openrdf.queryresult;

import java.io.IOException;
import java.io.InputStream;

import org.openrdf.model.ValueFactory;

/**
 * A general interface for query result parsers.
 */
public interface TupleQueryResultParser {

	/*---------*
	 * Methods *
	 *---------*/

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
	 * Sets the TupleQueryResultHandler that will handle the parsed query result data.
	 */
	public void setTupleQueryResultHandler(TupleQueryResultHandler handler);

	/**
	 * Parses the data from the supplied InputStream, using the supplied baseURI
	 * to resolve any relative URI references.
	 * 
	 * @param in
	 *        The InputStream from which to read the data.
	 * @param baseURI
	 *        The URI associated with the data in the InputStream.
	 * @throws IOException
	 *         If an I/O error occurred while data was read from the InputStream.
	 * @throws TupleQueryResultParseException
	 *         If the parser has found an unrecoverable parse error.
	 * @throws TupleQueryResultHandlerException
	 *         If the configured query result handler has encountered an
	 *         unrecoverable error.
	 */
	public void parse(InputStream in)
		throws IOException, TupleQueryResultParseException, TupleQueryResultHandlerException;
}
