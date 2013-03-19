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
import org.openrdf.model.datatypes.XMLDatatypeUtil;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryResultHandlerException;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.query.resultio.QueryResultWriterBase;
import org.openrdf.query.resultio.TupleQueryResultFormat;
import org.openrdf.query.resultio.TupleQueryResultWriter;

/**
 * TupleQueryResultWriter for the SPARQL CSV (Comma-Separated Values) format.
 * 
 * @see <a href="http://www.w3.org/TR/sparql11-results-csv-tsv/#csv">SPARQL 1.1
 *      Query Results CSV Format</a>
 * @author Jeen Broekstra
 */
public class SPARQLResultsCSVWriter extends QueryResultWriterBase implements TupleQueryResultWriter {

	private Writer writer;

	private List<String> bindingNames;

	/**
	 * @param out
	 */
	public SPARQLResultsCSVWriter(OutputStream out) {
		Writer w = new OutputStreamWriter(out, Charset.forName("UTF-8"));
		writer = new BufferedWriter(w, 1024);
	}

	@Override
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

	@Override
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

	@Override
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

	@Override
	public final TupleQueryResultFormat getTupleQueryResultFormat() {
		return TupleQueryResultFormat.CSV;
	}

	@Override
	public final TupleQueryResultFormat getQueryResultFormat() {
		return getTupleQueryResultFormat();
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

		if (datatype != null
				&& (XMLDatatypeUtil.isIntegerDatatype(datatype) || XMLDatatypeUtil.isDecimalDatatype(datatype) || XMLSchema.DOUBLE.equals(datatype)))
		{
			try {
				String normalized = XMLDatatypeUtil.normalize(label, datatype);
				writer.write(normalized);
				return; // done
			}
			catch (IllegalArgumentException e) {
				// not a valid numeric datatyped literal. ignore error and write as
				// (optionally quoted) string instead.
			}
		}

		if (label.contains(",") || label.contains("\r") || label.contains("\n") || label.contains("\"")) {
			quoted = true;

			// escape quotes inside the string
			label = label.replaceAll("\"", "\"\"");

			// add quotes around the string (escaped with a second quote for the
			// CSV parser)
			// label = "\"\"" + label + "\"\"";
		}

		if (quoted) {
			// write opening quote for entire value
			writer.write("\"");
		}

		writer.write(label);

		if (quoted) {
			// write closing quote for entire value
			writer.write("\"");
		}

	}

	@Override
	public void startDocument()
		throws QueryResultHandlerException
	{
		// Ignored by SPARQLResultsCSVWriter
	}

	@Override
	public void handleStylesheet(String stylesheetUrl)
		throws QueryResultHandlerException
	{
		// Ignored by SPARQLResultsCSVWriter
	}

	@Override
	public void startHeader()
		throws QueryResultHandlerException
	{
		// Ignored by SPARQLResultsCSVWriter
	}

	@Override
	public void handleLinks(List<String> linkUrls)
		throws QueryResultHandlerException
	{
		// Ignored by SPARQLResultsCSVWriter
	}

	@Override
	public void endHeader()
		throws QueryResultHandlerException
	{
		// Ignored by SPARQLResultsCSVWriter
	}

	@Override
	public void handleBoolean(boolean value)
		throws QueryResultHandlerException
	{
		throw new UnsupportedOperationException("Cannot handle boolean results");
	}

	@Override
	public void handleNamespace(String prefix, String uri)
		throws QueryResultHandlerException
	{
		// Ignored by SPARQLResultsCSVWriter
	}

}
