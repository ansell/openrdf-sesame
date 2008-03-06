/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.resultio.sparqljson;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;

import info.aduna.text.StringUtil;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.query.resultio.TupleQueryResultFormat;
import org.openrdf.query.resultio.TupleQueryResultWriter;

/**
 * A TupleQueryResultWriter that writes query results in the <a
 * href="http://www.mindswap.org/~kendall/sparql-results-json/">SPARQL Query
 * Results JSON Format</a>.
 */
public class SPARQLResultsJSONWriter implements TupleQueryResultWriter {

	/*-----------*
	 * Variables *
	 *-----------*/

	private BufferedWriter writer;

	private String indentString = "\t";

	private int indentLevel = 0;

	private boolean firstTupleWritten;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public SPARQLResultsJSONWriter(OutputStream out) {
		writer = new BufferedWriter(new OutputStreamWriter(out, Charset.forName("UTF-8")), 1024);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public final TupleQueryResultFormat getTupleQueryResultFormat() {
		return TupleQueryResultFormat.JSON;
	}

	public void startQueryResult(List<String> columnHeaders, boolean distinct, boolean ordered)
		throws TupleQueryResultHandlerException
	{
		try {
			openBraces();

			// Write header
			indent();
			writeKey("head");
			openBraces();
			indent();
			writeKeyValue("vars", columnHeaders);
			closeBraces();

			writeComma();

			// Write results
			indent();
			writeKey("results");
			openBraces();

			indent();
			writeKeyValue("ordered", ordered);
			writeComma();

			indent();
			writeKeyValue("distinct", distinct);
			writeComma();

			indent();
			writeKey("bindings");
			openArray();

			firstTupleWritten = false;
		}
		catch (IOException e) {
			throw new TupleQueryResultHandlerException(e);
		}
	}

	public void endQueryResult()
		throws TupleQueryResultHandlerException
	{
		try {
			closeArray(); // bindings array
			closeBraces(); // results braces
			closeBraces(); // root braces
			writer.flush();
		}
		catch (IOException e) {
			throw new TupleQueryResultHandlerException(e);
		}
	}

	public void handleSolution(BindingSet bindingSet)
		throws TupleQueryResultHandlerException
	{
		try {
			if (firstTupleWritten) {
				writeComma();
			}

			indent();
			openBraces(); // start of new solution

			Iterator<Binding> bindingIter = bindingSet.iterator();
			while (bindingIter.hasNext()) {
				Binding binding = bindingIter.next();

				indent();
				writeKeyValue(binding.getName(), binding.getValue());

				if (bindingIter.hasNext()) {
					writeComma();
				}
			}

			closeBraces(); // end solution

			writer.flush();
			firstTupleWritten = true;
		}
		catch (IOException e) {
			throw new TupleQueryResultHandlerException(e);
		}
	}

	private void writeKeyValue(String key, String value)
		throws IOException
	{
		writeKey(key);
		writeValue(value);
	}

	private void writeKeyValue(String key, boolean value)
		throws IOException
	{
		writeKey(key);
		writeValue(value);
	}

	private void writeKeyValue(String key, Value value)
		throws IOException, TupleQueryResultHandlerException
	{
		writeKey(key);
		writeValue(value);
	}

	private void writeKeyValue(String key, Iterable<String> array)
		throws IOException
	{
		writeKey(key);
		writeArray(array);
	}

	private void writeKey(String key)
		throws IOException
	{
		writeValue(key);
		writer.write(": ");
	}

	private void writeValue(Value value)
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

	private void writeValue(String value)
		throws IOException
	{
		// Escape double quotes
		value = StringUtil.gsub("\"", "\\\"", value);

		writer.write("\"");
		writer.write(value);
		writer.write("\"");
	}

	private void writeValue(boolean value)
		throws IOException
	{
		writer.write(value == true ? "true" : "false");
	}

	private void writeArray(Iterable<String> array)
		throws IOException
	{
		writer.write("[ ");

		Iterator<String> iter = array.iterator();
		while (iter.hasNext()) {
			String value = iter.next();

			writeValue(value);

			if (iter.hasNext()) {
				writer.write(", ");
			}
		}

		writer.write(" ]");
	}

	private void openArray()
		throws IOException
	{
		writer.write("[");
		writer.newLine();
		indentLevel++;
	}

	private void closeArray()
		throws IOException
	{
		writer.newLine();
		indentLevel--;
		indent();
		writer.write("]");
	}

	private void openBraces()
		throws IOException
	{
		writer.write("{");
		writer.newLine();
		indentLevel++;
	}

	private void closeBraces()
		throws IOException
	{
		writer.newLine();
		indentLevel--;
		indent();
		writer.write("}");
	}

	private void writeComma()
		throws IOException
	{
		writer.write(", ");
		writer.newLine();
	}

	private void indent()
		throws IOException
	{
		for (int i = 0; i < indentLevel; i++) {
			writer.write(indentString);
		}
	}
}
