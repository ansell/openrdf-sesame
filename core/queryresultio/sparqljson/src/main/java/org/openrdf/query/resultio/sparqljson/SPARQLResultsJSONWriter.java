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
import java.util.Iterator;
import java.util.List;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryResultHandlerException;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.query.resultio.TupleQueryResultFormat;
import org.openrdf.query.resultio.TupleQueryResultWriter;

/**
 * A TupleQueryResultWriter that writes query results in the <a
 * href="http://www.w3.org/TR/rdf-sparql-json-res/">SPARQL Query Results JSON
 * Format</a>.
 */
public class SPARQLResultsJSONWriter extends SPARQLJSONWriterBase implements TupleQueryResultWriter {

	/*-----------*
	 * Variables *
	 *-----------*/

	private boolean firstTupleWritten;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public SPARQLResultsJSONWriter(OutputStream out) {
		super(out);
	}

	/*---------*
	 * Methods *
	 *---------*/

	@Override
	public final TupleQueryResultFormat getTupleQueryResultFormat() {
		return TupleQueryResultFormat.JSON;
	}

	@Override
	public TupleQueryResultFormat getQueryResultFormat() {
		return getTupleQueryResultFormat();
	}

	@Override
	public void startQueryResult(List<String> columnHeaders)
		throws TupleQueryResultHandlerException
	{
		if (!documentOpen) {
			startDocument();
			startHeader();
		}
		try {
			writeKeyValue("vars", columnHeaders);
		}
		catch (IOException e) {
			throw new TupleQueryResultHandlerException(e);
		}
	}

	@Override
	public void endHeader()
		throws TupleQueryResultHandlerException
	{
		try {
			closeBraces();

			writeComma();

			// Write results
			writeKey("results");
			openBraces();

			writeKey("bindings");
			openArray();
			headerComplete = true;
		}
		catch (IOException e) {
			throw new TupleQueryResultHandlerException(e);
		}
	}

	@Override
	public void handleSolution(BindingSet bindingSet)
		throws TupleQueryResultHandlerException
	{
		if (!headerComplete) {
			endHeader();
		}
		try {
			if (firstTupleWritten) {
				writeComma();
			}
			else {
				firstTupleWritten = true;
			}

			openBraces(); // start of new solution

			Iterator<Binding> bindingIter = bindingSet.iterator();
			while (bindingIter.hasNext()) {
				Binding binding = bindingIter.next();

				writeKeyValue(binding.getName(), binding.getValue());

				if (bindingIter.hasNext()) {
					writeComma();
				}
			}

			closeBraces(); // end solution

			writer.flush();
		}
		catch (IOException e) {
			throw new TupleQueryResultHandlerException(e);
		}
	}

	@Override
	public void endQueryResult()
		throws TupleQueryResultHandlerException
	{
		try {
			closeArray(); // bindings array
			closeBraces(); // results braces
			endDocument();
		}
		catch (IOException e) {
			throw new TupleQueryResultHandlerException(e);
		}
	}

	@Override
	public void startDocument()
		throws TupleQueryResultHandlerException
	{
		documentOpen = true;
		headerComplete = false;
		try {
			openBraces();
		}
		catch (IOException e) {
			throw new TupleQueryResultHandlerException(e);
		}
	}

	@Override
	public void handleStylesheet(String stylesheetUrl)
		throws TupleQueryResultHandlerException
	{
		// Ignore, as JSON does not support stylesheets
	}

	@Override
	public void startHeader()
		throws TupleQueryResultHandlerException
	{
		try {
			// Write header
			writeKey("head");
			openBraces();
		}
		catch (IOException e) {
			throw new TupleQueryResultHandlerException(e);
		}
	}

	@Override
	public void handleLinks(List<String> linkUrls)
		throws TupleQueryResultHandlerException
	{
		try {
			writeKeyValue("link", linkUrls);
		}
		catch (IOException e) {
			throw new TupleQueryResultHandlerException(e);
		}
	}

	protected void writeKeyValue(String key, Value value)
		throws IOException, TupleQueryResultHandlerException
	{
		writeKey(key);
		writeValue(value);
	}

	protected void writeValue(Value value)
		throws IOException, TupleQueryResultHandlerException
	{
		writer.write("{ ");

		if (value instanceof URI) {
			writeKeyValue("type", "uri");
			writer.write(", ");
			writeKeyValue("value", ((URI)value).toString());
		}
		else if (value instanceof BNode) {
			writeKeyValue("type", "bnode");
			writer.write(", ");
			writeKeyValue("value", ((BNode)value).getID());
		}
		else if (value instanceof Literal) {
			Literal lit = (Literal)value;

			if (lit.getDatatype() != null) {
				writeKeyValue("type", "typed-literal");
				writer.write(", ");
				writeKeyValue("datatype", lit.getDatatype().toString());
			}
			else {
				writeKeyValue("type", "literal");
				if (lit.getLanguage() != null) {
					writer.write(", ");
					writeKeyValue("xml:lang", lit.getLanguage());
				}
			}

			writer.write(", ");
			writeKeyValue("value", lit.getLabel());
		}
		else {
			throw new TupleQueryResultHandlerException("Unknown Value object type: " + value.getClass());
		}

		writer.write(" }");
	}

	@Override
	public void handleBoolean(boolean value)
		throws QueryResultHandlerException
	{
		throw new UnsupportedOperationException("Cannot handle boolean results");
	}
}
