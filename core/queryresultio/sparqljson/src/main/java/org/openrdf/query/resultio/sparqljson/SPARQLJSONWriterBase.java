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

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;

import info.aduna.io.IndentingWriter;
import info.aduna.text.StringUtil;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryResultHandlerException;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.query.resultio.QueryResultWriter;

/**
 * An abstract class to implement the base functionality for both
 * SPARQLBooleanJSONWriter and SPARQLResultsJSONWriter.
 * 
 * @author Peter Ansell
 */
abstract class SPARQLJSONWriterBase implements QueryResultWriter {

	/*-----------*
	 * Variables *
	 *-----------*/

	protected IndentingWriter writer;

	protected boolean firstTupleWritten = false;

	protected boolean documentOpen = false;

	protected boolean headerComplete = false;

	protected boolean tupleVariablesFound = false;

	public SPARQLJSONWriterBase(OutputStream out) {
		Writer w = new OutputStreamWriter(out, Charset.forName("UTF-8"));
		w = new BufferedWriter(w, 1024);
		writer = new IndentingWriter(w);
	}

	@Override
	public void endHeader()
		throws QueryResultHandlerException
	{
		try {
			closeBraces();

			writeComma();

			if (tupleVariablesFound) {
				// Write results
				writeKey("results");
				openBraces();

				writeKey("bindings");
				openArray();
			}

			headerComplete = true;
		}
		catch (IOException e) {
			throw new QueryResultHandlerException(e);
		}
	}

	@Override
	public void startQueryResult(List<String> columnHeaders)
		throws TupleQueryResultHandlerException
	{
		try {
			if (!documentOpen) {
				startDocument();
				startHeader();
			}
			tupleVariablesFound = true;
			try {
				writeKeyValue("vars", columnHeaders);
			}
			catch (IOException e) {
				throw new TupleQueryResultHandlerException(e);
			}
		}
		catch (TupleQueryResultHandlerException e) {
			throw e;
		}
		catch (QueryResultHandlerException e) {
			throw new TupleQueryResultHandlerException(e);
		}
	}

	@Override
	public void handleSolution(BindingSet bindingSet)
		throws TupleQueryResultHandlerException
	{
		try {
			if (!headerComplete) {
				endHeader();
			}
			if (firstTupleWritten) {
				writeComma();
			}
			firstTupleWritten = true;

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
		catch (TupleQueryResultHandlerException e) {
			throw e;
		}
		catch (QueryResultHandlerException e) {
			throw new TupleQueryResultHandlerException(e);
		}
	}

	@Override
	public void endQueryResult()
		throws TupleQueryResultHandlerException
	{
		try {
			if (!headerComplete) {
				endHeader();
			}
			closeArray(); // bindings array
			closeBraces(); // results braces
			endDocument();
		}
		catch (IOException e) {
			throw new TupleQueryResultHandlerException(e);
		}
		catch (TupleQueryResultHandlerException e) {
			throw e;
		}
		catch (QueryResultHandlerException e) {
			throw new TupleQueryResultHandlerException(e);
		}
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
			throw new QueryResultHandlerException(e);
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
			throw new QueryResultHandlerException(e);
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
			throw new QueryResultHandlerException(e);
		}
	}

	protected void writeKeyValue(String key, Value value)
		throws IOException, QueryResultHandlerException
	{
		writeKey(key);
		writeValue(value);
	}

	protected void writeValue(Value value)
		throws IOException, QueryResultHandlerException
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

			if (lit.getLanguage() != null) {
				writeKeyValue("xml:lang", lit.getLanguage());
				writer.write(", ");
			}
			if (lit.getDatatype() != null) {
				writeKeyValue("datatype", lit.getDatatype().toString());
				writer.write(", ");
			}

			writeKeyValue("type", "literal");

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
			throw new QueryResultHandlerException(e);
		}
	}

	protected void endDocument()
		throws IOException
	{
		closeBraces(); // root braces
		writer.flush();
		documentOpen = false;
		headerComplete = false;
	}

	protected void writeKeyValue(String key, String value)
		throws IOException
	{
		writeKey(key);
		writeString(value);
	}

	protected void writeKeyValue(String key, Iterable<String> array)
		throws IOException
	{
		writeKey(key);
		writeArray(array);
	}

	protected void writeKey(String key)
		throws IOException
	{
		writeString(key);
		writer.write(": ");
	}

	protected void writeString(String value)
		throws IOException
	{
		// Escape special characters
		value = StringUtil.gsub("\\", "\\\\", value);
		value = StringUtil.gsub("\"", "\\\"", value);
		value = StringUtil.gsub("/", "\\/", value);
		value = StringUtil.gsub("\b", "\\b", value);
		value = StringUtil.gsub("\f", "\\f", value);
		value = StringUtil.gsub("\n", "\\n", value);
		value = StringUtil.gsub("\r", "\\r", value);
		value = StringUtil.gsub("\t", "\\t", value);

		writer.write("\"");
		writer.write(value);
		writer.write("\"");
	}

	protected void writeArray(Iterable<String> array)
		throws IOException
	{
		openArray();
		Iterator<String> iter = array.iterator();
		while (iter.hasNext()) {
			String value = iter.next();

			writeString(value);

			if (iter.hasNext()) {
				writer.write(", ");
			}
		}
		closeArray();
	}

	protected void openArray()
		throws IOException
	{
		writer.write("[");
		writer.writeEOL();
		writer.increaseIndentation();
	}

	protected void closeArray()
		throws IOException
	{
		writer.writeEOL();
		writer.decreaseIndentation();
		writer.write("]");
	}

	protected void openBraces()
		throws IOException
	{
		writer.write("{");
		writer.writeEOL();
		writer.increaseIndentation();
	}

	protected void closeBraces()
		throws IOException
	{
		writer.writeEOL();
		writer.decreaseIndentation();
		writer.write("}");
	}

	protected void writeComma()
		throws IOException
	{
		writer.write(", ");
		writer.writeEOL();
	}
}
