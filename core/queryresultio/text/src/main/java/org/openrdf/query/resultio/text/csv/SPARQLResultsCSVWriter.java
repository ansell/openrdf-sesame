/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2011.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.resultio.text.csv;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.List;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.query.resultio.TupleQueryResultFormat;
import org.openrdf.query.resultio.TupleQueryResultWriter;

/**
 * TupleQueryResultWriter for the SPARQL CSV (Comma-Separated Values) format.
 * 
 * @see http://www.w3.org/TR/sparql11-results-csv-tsv/#csv
 * @author Jeen Broekstra
 */
public class SPARQLResultsCSVWriter implements TupleQueryResultWriter {

	private Writer writer;

	private List<String> bindingNames;

	/**
	 * @param out
	 */
	public SPARQLResultsCSVWriter(OutputStream out) {
		Writer w = new OutputStreamWriter(out, Charset.forName("UTF-8"));
		writer = new BufferedWriter(w, 1024);
	}

	public void startQueryResult(List<String> bindingNames)
		throws TupleQueryResultHandlerException
	{
		this.bindingNames = bindingNames;

		try {
			for (int i = 0; i < bindingNames.size(); i++) {
				writer.write(bindingNames.get(i));
				if (i < bindingNames.size() - 1) {
					writer.write(",");
				}
			}
			writer.write("\r\n");
		}
		catch (IOException e) {
			throw new TupleQueryResultHandlerException(e);
		}
	}

	public void endQueryResult()
		throws TupleQueryResultHandlerException
	{
		try {
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
			for (int i = 0; i < bindingNames.size(); i++) {
				String name = bindingNames.get(i);
				Value value = bindingSet.getValue(name);
				if (value != null) {
					writeValue(value);
				}

				if (i < bindingNames.size() - 1) {
					writer.write(",");
				}
			}
			writer.write("\r\n");
		}
		catch (IOException e) {
			throw new TupleQueryResultHandlerException(e);
		}
	}

	public TupleQueryResultFormat getTupleQueryResultFormat() {
		return TupleQueryResultFormat.CSV;
	}

	protected void writeValue(Value val)
		throws IOException
	{
		if (val instanceof Resource) {
			writeResource((Resource)val);
		}
		else {
			writeLiteral((Literal)val);
		}
	}

	protected void writeResource(Resource res)
		throws IOException
	{
		if (res instanceof URI) {
			writeURI((URI)res);
		}
		else {
			writeBNode((BNode)res);
		}
	}

	protected void writeURI(URI uri)
		throws IOException
	{
		String uriString = uri.toString();
		writer.write(uriString);
	}

	protected void writeBNode(BNode bNode)
		throws IOException
	{
		writer.write("_:");
		writer.write(bNode.getID());
	}

	private void writeLiteral(Literal literal)
		throws IOException
	{
		String label = literal.getLabel();
		URI datatype = literal.getDatatype();
		String language = literal.getLanguage();

		boolean quoted = false;

		if (datatype != null || language != null || label.contains("\"") || label.contains(",")
				|| label.contains("\r") || label.contains("\n"))
		{
			quoted = true;

			// escape quotes inside the string 
			label = label.replaceAll("\"", "\"\"");

			// add quotes around the string (escaped with a second quote for the CSV parser)
			label = "\"\"" + label + "\"\"";
		}

		if (quoted) {
			// write opening quote for entire value
			writer.write("\"");
		}

		writer.write(label);

		if (datatype != null) {
			// Append the literal's datatype
			writer.write("^^");
			writeURI(datatype);
		}
		else if (language != null) {
			// Append the literal's language
			writer.write("@");
			writer.write(language);
		}

		if (quoted) {
			// write closing quote for entire value
			writer.write("\"");
		}

	}
}
