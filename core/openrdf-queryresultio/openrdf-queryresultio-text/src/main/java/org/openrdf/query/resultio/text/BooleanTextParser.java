/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.resultio.text;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;

import info.aduna.io.IOUtil;

import org.openrdf.query.resultio.BooleanQueryResultFormat;
import org.openrdf.query.resultio.BooleanQueryResultParser;
import org.openrdf.query.resultio.QueryResultParseException;

/**
 * Reader for the plain text boolean result format.
 */
public class BooleanTextParser implements BooleanQueryResultParser {

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new parser for the plain text boolean query result format.
	 */
	public BooleanTextParser() {
		super();
	}

	/*---------*
	 * Methods *
	 *---------*/

	public final BooleanQueryResultFormat getBooleanQueryResultFormat() {
		return BooleanQueryResultFormat.TEXT;
	}

	public synchronized boolean parse(InputStream in)
		throws IOException, QueryResultParseException
	{
		Reader reader = new InputStreamReader(in, Charset.forName("US-ASCII"));
		String value = IOUtil.readString(reader, 16);
		value = value.trim();

		if (value.equalsIgnoreCase("true")) {
			return true;
		}
		else if (value.equalsIgnoreCase("false")) {
			return false;
		}
		else {
			throw new QueryResultParseException("Invalid value: " + value);
		}
	}
}
