/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.resultio.binary;

import org.openrdf.query.resultio.TupleQueryResultFormat;
import org.openrdf.query.resultio.TupleQueryResultParser;
import org.openrdf.query.resultio.TupleQueryResultParserFactory;

/**
 * A {@link TupleQueryResultParserFactory} for binary tuple query result
 * parsers.
 * 
 * @author Arjohn Kampman
 */
public class BinaryQueryResultParserFactory implements TupleQueryResultParserFactory {

	/**
	 * Returns {@link TupleQueryResultFormat#BINARY}.
	 */
	public TupleQueryResultFormat getFileFormat() {
		return TupleQueryResultFormat.BINARY;
	}

	/**
	 * Returns a new instance of BinaryQueryResultParser.
	 */
	public TupleQueryResultParser getParser() {
		return new BinaryQueryResultParser();
	}
}
