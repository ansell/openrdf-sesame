/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.resultio;

import java.io.IOException;
import java.io.InputStream;

/**
 * A general interface for boolean query result parsers.
 * 
 * @author Arjohn Kampman
 */
public interface BooleanQueryResultParser {

	/**
	 * Gets the query result format that this parser can parse.
	 */
	public BooleanQueryResultFormat getBooleanQueryResultFormat();

	/**
	 * Parses the data from the supplied InputStream.
	 * 
	 * @param in
	 *        The InputStream from which to read the data.
	 * @throws IOException
	 *         If an I/O error occurred while data was read from the InputStream.
	 * @throws QueryResultParseException
	 *         If the parser has encountered an unrecoverable parse error.
	 */
	public boolean parse(InputStream in)
		throws IOException, QueryResultParseException;
}
