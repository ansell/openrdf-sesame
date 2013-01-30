/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.resultio.sparqljson;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.openrdf.query.BooleanQueryResultHandlerException;
import org.openrdf.query.resultio.BooleanQueryResultFormat;
import org.openrdf.query.resultio.BooleanQueryResultWriter;

/**
 * A BooleanQueryResultWriter that writes query results in the <a
 * href="http://www.w3.org/TR/rdf-sparql-json-res/">SPARQL Query Results JSON
 * Format</a>.
 */
public class SPARQLBooleanJSONWriter extends SPARQLJSONWriterBase<BooleanQueryResultFormat> implements
		BooleanQueryResultWriter
{

	/*--------------*
	 * Constructors *
	 *--------------*/

	public SPARQLBooleanJSONWriter(OutputStream out) {
		super(out);
	}

	/*---------*
	 * Methods *
	 *---------*/

	@Override
	public final BooleanQueryResultFormat getBooleanQueryResultFormat() {
		return BooleanQueryResultFormat.JSON;
	}

	@Override
	public final BooleanQueryResultFormat getQueryResultFormat() {
		return getBooleanQueryResultFormat();
	}

	@Override
	public void startDocument()
		throws BooleanQueryResultHandlerException
	{
		documentOpen = true;
		headerComplete = false;
		try {
			openBraces();
		}
		catch (IOException e) {
			throw new BooleanQueryResultHandlerException(e);
		}
	}

	@Override
	public void handleStylesheet(String stylesheetUrl)
		throws BooleanQueryResultHandlerException
	{
		// Ignore, as JSON does not support stylesheets
	}

	@Override
	public void startHeader()
		throws BooleanQueryResultHandlerException
	{
		try {
			// Write header
			writeKey("head");
			openBraces();
		}
		catch (IOException e) {
			throw new BooleanQueryResultHandlerException(e);
		}
	}

	@Override
	public void handleLinks(List<String> linkUrls)
		throws BooleanQueryResultHandlerException
	{
		try {
			writeKeyValue("link", linkUrls);
		}
		catch (IOException e) {
			throw new BooleanQueryResultHandlerException(e);
		}
	}

	@Override
	public void endHeader()
		throws BooleanQueryResultHandlerException
	{
		try {
			closeBraces();

			writeComma();
			headerComplete = true;
		}
		catch (IOException e) {
			throw new BooleanQueryResultHandlerException(e);
		}
	}

	@Override
	public void write(boolean value)
		throws IOException
	{
		try {
			handleBoolean(value);
		}
		catch (BooleanQueryResultHandlerException e) {
			if (e.getCause() != null && e.getCause() instanceof IOException) {
				throw (IOException)e.getCause();
			}
			else {
				throw new IOException(e);
			}
		}
	}

	@Override
	public void handleBoolean(boolean value)
		throws BooleanQueryResultHandlerException
	{
		if (!documentOpen) {
			startDocument();
			startHeader();
		}

		if (!headerComplete) {
			endHeader();
		}

		try {
			if (value) {
				writeKeyValue("boolean", "true");
			}
			else {
				writeKeyValue("boolean", "false");
			}

			endDocument();
		}
		catch (IOException e) {
			throw new BooleanQueryResultHandlerException(e);
		}
	}

}
