/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.resultio.sparqljson;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.openrdf.query.TupleQueryResultHandlerException;
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

	public void startQueryResult(List<String> columnHeaders)
		throws TupleQueryResultHandlerException
	{
		try {
			writeKeyValue("vars", columnHeaders);
		}
		catch (IOException e) {
			throw new TupleQueryResultHandlerException(e);
		}
	}

	public void endHeader()
		throws TupleQueryResultHandlerException
	{
		try {
			closeBraces();

			writeComma();
		}
		catch (IOException e) {
			throw new TupleQueryResultHandlerException(e);
		}
	}

	public void write(boolean value)
		throws IOException
	{
		if (value) {
			writeKeyValue("boolean", "true");
		}
		else {
			writeKeyValue("boolean", "false");
		}

	}

}
