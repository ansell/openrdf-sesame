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
package org.openrdf.query.resultio.text.tsv;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.List;

import info.aduna.text.StringUtil;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.IRI;
import org.openrdf.model.Value;
import org.openrdf.model.datatypes.XMLDatatypeUtil;
import org.openrdf.model.util.Literals;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryResultHandlerException;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.query.resultio.QueryResultWriterBase;
import org.openrdf.query.resultio.TupleQueryResultFormat;
import org.openrdf.query.resultio.TupleQueryResultWriter;

/**
 * TupleQueryResultWriter for the SPARQL TSV (Tab-Separated Values) format.
 * 
 * @see <a href="http://www.w3.org/TR/sparql11-results-csv-tsv/#tsv">SPARQL 1.1
 *      Query Results TSV Format</a>
 * @author Jeen Broekstra
 */
public class SPARQLResultsTSVWriter extends QueryResultWriterBase implements TupleQueryResultWriter {

	private Writer writer;

	private List<String> bindingNames;

	protected boolean tupleVariablesFound = false;

	/**
	 * @param out
	 */
	public SPARQLResultsTSVWriter(OutputStream out) {
		Writer w = new OutputStreamWriter(out, Charset.forName("UTF-8"));
		writer = new BufferedWriter(w, 1024);
	}

	@Override
	public void startQueryResult(List<String> bindingNames)
		throws TupleQueryResultHandlerException
	{
		tupleVariablesFound = true;

		this.bindingNames = bindingNames;

		try {
			for (int i = 0; i < bindingNames.size(); i++) {
				writer.write("?"); // mandatory prefix in TSV
				writer.write(bindingNames.get(i));
				if (i < bindingNames.size() - 1) {
					writer.write("\t");
				}
			}
			writer.write("\n");
		}
		catch (IOException e) {
			throw new TupleQueryResultHandlerException(e);
		}
	}

	@Override
	public void endQueryResult()
		throws TupleQueryResultHandlerException
	{
		if (!tupleVariablesFound) {
			throw new IllegalStateException(
					"Could not end query result as startQueryResult was not called first.");
		}

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
		if (!tupleVariablesFound) {
			throw new IllegalStateException("Must call startQueryResult before handleSolution");
		}

		try {
			for (int i = 0; i < bindingNames.size(); i++) {
				String name = bindingNames.get(i);
				Value value = bindingSet.getValue(name);
				if (value != null) {
					writeValue(value);
				}

				if (i < bindingNames.size() - 1) {
					writer.write("\t");
				}
			}
			writer.write("\n");
		}
		catch (IOException e) {
			throw new TupleQueryResultHandlerException(e);
		}
	}

	@Override
	public final TupleQueryResultFormat getTupleQueryResultFormat() {
		return TupleQueryResultFormat.TSV;
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
		if (res instanceof IRI) {
			writeURI((IRI)res);
		}
		else {
			writeBNode((BNode)res);
		}
	}

	protected void writeURI(IRI uri)
		throws IOException
	{
		String uriString = uri.toString();
		writer.write("<" + uriString + ">");
	}

	protected void writeBNode(BNode bNode)
		throws IOException
	{
		writer.write("_:");
		writer.write(bNode.getID());
	}

	private void writeLiteral(Literal lit)
		throws IOException
	{
		String label = lit.getLabel();

		IRI datatype = lit.getDatatype();

		if (XMLSchema.INTEGER.equals(datatype) || XMLSchema.DECIMAL.equals(datatype)
				|| XMLSchema.DOUBLE.equals(datatype))
		{
			try {
				writer.write(XMLDatatypeUtil.normalize(label, datatype));
				return; // done
			}
			catch (IllegalArgumentException e) {
				// not a valid numeric typed literal. ignore error and write as
				// quoted string instead.
			}
		}

		writer.write("\"");

		writer.write(encodeString(label));

		writer.write("\"");

		if (Literals.isLanguageLiteral(lit)) {
			// Append the literal's language
			writer.write("@");
			writer.write(lit.getLanguage().get());
		}
		else if (!XMLSchema.STRING.equals(datatype) || !xsdStringToPlainLiteral()) {
			// Append the literal's datatype
			writer.write("^^");
			writeURI(datatype);
		}
	}

	private static String encodeString(String s) {
		s = StringUtil.gsub("\\", "\\\\", s);
		s = StringUtil.gsub("\t", "\\t", s);
		s = StringUtil.gsub("\n", "\\n", s);
		s = StringUtil.gsub("\r", "\\r", s);
		s = StringUtil.gsub("\"", "\\\"", s);
		return s;
	}

	@Override
	public void startDocument()
		throws TupleQueryResultHandlerException
	{
		// Ignored in SPARQLResultsTSVWriter
	}

	@Override
	public void handleStylesheet(String stylesheetUrl)
		throws TupleQueryResultHandlerException
	{
		// Ignored in SPARQLResultsTSVWriter
	}

	@Override
	public void startHeader()
		throws TupleQueryResultHandlerException
	{
		// Ignored in SPARQLResultsTSVWriter
	}

	@Override
	public void handleLinks(List<String> linkUrls)
		throws TupleQueryResultHandlerException
	{
		// Ignored in SPARQLResultsTSVWriter
	}

	@Override
	public void endHeader()
		throws TupleQueryResultHandlerException
	{
		// Ignored in SPARQLResultsTSVWriter
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
		// Ignored in SPARQLResultsTSVWriter
	}
}
