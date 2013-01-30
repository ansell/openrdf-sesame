/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.resultio.sparqljson;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.openrdf.query.resultio.BooleanQueryResultFormat;
import org.openrdf.query.resultio.BooleanQueryResultWriter;

/**
 * A BooleanQueryResultWriter that writes query results in the <a
 * href="http://www.w3.org/TR/rdf-sparql-json-res/">SPARQL Query Results JSON
 * Format</a>.
 */
public class SPARQLBooleanJSONWriter extends SPARQLJSONWriterBase implements BooleanQueryResultWriter {

	/*--------------*
	 * Constructors *
	 *--------------*/

	public SPARQLBooleanJSONWriter(OutputStream out) {
		super(out);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public final BooleanQueryResultFormat getBooleanQueryResultFormat() {
		return BooleanQueryResultFormat.JSON;
	}

	@Override
	public void startDocument()
		throws IOException
	{
		documentOpen = true;
		headerComplete = false;
		openBraces();
	}

	@Override
	public void handleStylesheet(String stylesheetUrl)
		throws IOException
	{
		// Ignore, as JSON does not support stylesheets
	}

	@Override
	public void startHeader()
		throws IOException
	{
		// Write header
		writeKey("head");
		openBraces();
	}

	@Override
	public void handleLinks(List<String> linkUrls)
		throws IOException
	{
		writeKeyValue("link", linkUrls);
	}

	@Override
	public void endHeader()
		throws IOException
	{
		closeBraces();

		writeComma();
		headerComplete = true;
	}

	@Override
	public void write(boolean value)
		throws IOException
	{
		if (!documentOpen) {
			startDocument();
			startHeader();
		}

		if (!headerComplete) {
			endHeader();
		}

		if (value) {
			writeKeyValue("boolean", "true");
		}
		else {
			writeKeyValue("boolean", "false");
		}

		endDocument();
	}

}
