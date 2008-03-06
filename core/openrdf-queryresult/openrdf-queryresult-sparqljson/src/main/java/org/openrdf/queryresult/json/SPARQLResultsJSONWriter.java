/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.queryresult.json;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.List;

import org.openrdf.queryresult.Binding;
import org.openrdf.queryresult.TupleQueryResultFormat;
import org.openrdf.queryresult.TupleQueryResultHandlerException;
import org.openrdf.queryresult.TupleQueryResultWriter;
import org.openrdf.queryresult.Solution;
import org.openrdf.util.StringUtil;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;


/**
 * A TupleQueryResultWriter that writes query results in the <a
 * href="http://www.mindswap.org/~kendall/sparql-results-json/">SPARQL Query
 * Results JSON Format</a>.
 */
public class SPARQLResultsJSONWriter implements TupleQueryResultWriter {

	/*-----------*
	 * Variables *
	 *-----------*/

	private BufferedWriter _writer;

	private String _indentString = "\t";

	private int _indentLevel = 0;

	private boolean _firstTupleWritten;

	/*---------*
	 * Methods *
	 *---------*/

	public void setOutputStream(OutputStream out) {
		try {
			_writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"), 1024);
		}
		catch (UnsupportedEncodingException e) {
			// UTF-8 is required to be supported on all JVMs
			throw new RuntimeException(e);
		}
	}

	public final TupleQueryResultFormat getQueryResultFormat() {
		return TupleQueryResultFormat.JSON;
	}

	public void startQueryResult(List<String> columnHeaders, boolean distinct, boolean ordered)
		throws TupleQueryResultHandlerException
	{
		try {
			_openBraces();

			// Write header
			_indent();
			_writeKey("head");
			_openBraces();
			_indent();
			_writeKeyValue("vars", columnHeaders);
			_closeBraces();

			_writeComma();

			// Write results
			_indent();
			_writeKey("results");
			_openBraces();

			_indent();
			_writeKeyValue("ordered", ordered);
			_writeComma();

			_indent();
			_writeKeyValue("distinct", distinct);
			_writeComma();

			_indent();
			_writeKey("bindings");
			_openArray();

			_firstTupleWritten = false;
		}
		catch (IOException e) {
			throw new TupleQueryResultHandlerException(e);
		}
	}

	public void endQueryResult()
		throws TupleQueryResultHandlerException
	{
		try {
			_closeArray(); // bindings array
			_closeBraces(); // results braces
			_closeBraces(); // root braces
			_writer.flush();
		}
		catch (IOException e) {
			throw new TupleQueryResultHandlerException(e);
		}
	}

	public void handleSolution(Solution solution)
		throws TupleQueryResultHandlerException
	{
		try {
			if (_firstTupleWritten) {
				_writeComma();
			}

			_indent();
			_openBraces(); // start of new solution

			Iterator<Binding> bindingIter = solution.iterator();
			while (bindingIter.hasNext()) {
				Binding binding = bindingIter.next();

				_indent();
				_writeKeyValue(binding.getName(), binding.getValue());

				if (bindingIter.hasNext()) {
					_writeComma();
				}
			}

			_closeBraces(); // end solution

			_writer.flush();
			_firstTupleWritten = true;
		}
		catch (IOException e) {
			throw new TupleQueryResultHandlerException(e);
		}
	}

	private void _writeKeyValue(String key, String value)
		throws IOException
	{
		_writeKey(key);
		_writeValue(value);
	}

	private void _writeKeyValue(String key, boolean value)
		throws IOException
	{
		_writeKey(key);
		_writeValue(value);
	}

	private void _writeKeyValue(String key, Value value)
		throws IOException, TupleQueryResultHandlerException
	{
		_writeKey(key);
		_writeValue(value);
	}

	private void _writeKeyValue(String key, Iterable<String> array)
		throws IOException
	{
		_writeKey(key);
		_writeArray(array);
	}

	private void _writeKey(String key)
		throws IOException
	{
		_writeValue(key);
		_writer.write(": ");
	}

	private void _writeValue(Value value)
		throws IOException, TupleQueryResultHandlerException
	{
		_writer.write("{ ");

		if (value instanceof URI) {
			_writeKeyValue("type", "uri");
			_writer.write(", ");
			_writeKeyValue("value", ((URI)value).toString());
		}
		else if (value instanceof BNode) {
			_writeKeyValue("type", "bnode");
			_writer.write(", ");
			_writeKeyValue("value", ((BNode)value).getID());
		}
		else if (value instanceof Literal) {
			Literal lit = (Literal)value;

			if (lit.getDatatype() != null) {
				_writeKeyValue("type", "typed-literal");
				_writer.write(", ");
				_writeKeyValue("datatype", lit.getDatatype().toString());
			}
			else {
				_writeKeyValue("type", "literal");
				if (lit.getLanguage() != null) {
					_writer.write(", ");
					_writeKeyValue("xml:lang", lit.getLanguage());
				}
			}

			_writer.write(", ");
			_writeKeyValue("value", lit.getLabel());
		}
		else {
			throw new TupleQueryResultHandlerException("Unknown Value object type: " + value.getClass());
		}

		_writer.write(" }");
	}

	private void _writeValue(String value)
		throws IOException
	{
		// Escape double quotes
		value = StringUtil.gsub("\"", "\\\"", value);

		_writer.write("\"");
		_writer.write(value);
		_writer.write("\"");
	}

	private void _writeValue(boolean value)
		throws IOException
	{
		_writer.write(value == true ? "true" : "false");
	}

	private void _writeArray(Iterable<String> array)
		throws IOException
	{
		_writer.write("[ ");

		Iterator<String> iter = array.iterator();
		while (iter.hasNext()) {
			String value = iter.next();

			_writeValue(value);

			if (iter.hasNext()) {
				_writer.write(", ");
			}
		}

		_writer.write(" ]");
	}

	private void _openArray()
		throws IOException
	{
		_writer.write("[");
		_writer.newLine();
		_indentLevel++;
	}

	private void _closeArray()
		throws IOException
	{
		_writer.newLine();
		_indentLevel--;
		_indent();
		_writer.write("]");
	}

	private void _openBraces()
		throws IOException
	{
		_writer.write("{");
		_writer.newLine();
		_indentLevel++;
	}

	private void _closeBraces()
		throws IOException
	{
		_writer.newLine();
		_indentLevel--;
		_indent();
		_writer.write("}");
	}

	private void _writeComma()
		throws IOException
	{
		_writer.write(", ");
		_writer.newLine();
	}

	private void _indent()
		throws IOException
	{
		for (int i = 0; i < _indentLevel; i++) {
			_writer.write(_indentString);
		}
	}
}
