/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.resultio.text;

import org.openrdf.query.resultio.BooleanQueryResultFormat;
import org.openrdf.query.resultio.BooleanQueryResultParser;
import org.openrdf.query.resultio.BooleanQueryResultParserFactory;

/**
 * A {@link BooleanQueryResultParserFactory} for plain text boolean result
 * parsers.
 * 
 * @author Arjohn Kampman
 */
public class BooleanTextParserFactory implements BooleanQueryResultParserFactory {

	/**
	 * Returns {@link BooleanQueryResultFormat#BINARY}.
	 */
	public BooleanQueryResultFormat getBooleanQueryResultFormat() {
		return BooleanQueryResultFormat.TEXT;
	}

	/**
	 * Returns a new instance of BooleanTextParser.
	 */
	public BooleanQueryResultParser getParser() {
		return new BooleanTextParser();
	}
}
