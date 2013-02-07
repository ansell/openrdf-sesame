/* 
 * Licensed to Aduna under one or more contributor license agreements.  
 * See the NOTICE.txt file distributed with this work for additional 
 * information regarding copyright ownership. 
 *
 * Aduna licenses this file to you under the terms of the Aduna BSD 
 * License (the "License"); you may not use this file except in compliance 
 * with the License. See the LICENSE.txt file distributed with this work 
 * for the full License.
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package org.openrdf.query.resultio.sparqljson;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.openrdf.query.BindingSet;
import org.openrdf.query.BooleanQueryResultHandlerException;
import org.openrdf.query.QueryResultHandlerException;
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
		throws QueryResultHandlerException
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
		throws QueryResultHandlerException
	{
		// Ignore, as JSON does not support stylesheets
	}

	@Override
	public void startHeader()
		throws QueryResultHandlerException
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
		throws QueryResultHandlerException
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
		throws QueryResultHandlerException
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
		catch (QueryResultHandlerException e) {
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
		throws QueryResultHandlerException
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

	@Override
	public void startQueryResult(List<String> bindingNames)
		throws TupleQueryResultHandlerException
	{
		throw new UnsupportedOperationException("Cannot handle tuple results");
	}

	@Override
	public void endQueryResult()
		throws TupleQueryResultHandlerException
	{
		throw new UnsupportedOperationException("Cannot handle tuple results");
	}

	@Override
	public void handleSolution(BindingSet bindingSet)
		throws TupleQueryResultHandlerException
	{
		throw new UnsupportedOperationException("Cannot handle tuple results");
	}

}
