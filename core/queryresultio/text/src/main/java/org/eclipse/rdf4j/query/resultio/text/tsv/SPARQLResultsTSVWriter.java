/*******************************************************************************
 * Copyright (c) 2015, Eclipse Foundation, Inc. and its licensors.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * - Neither the name of the Eclipse Foundation, Inc. nor the names of its
 *   contributors may be used to endorse or promote products derived from this
 *   software without specific prior written permission. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package org.eclipse.rdf4j.query.resultio.text.tsv;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.List;

import org.eclipse.rdf4j.common.text.StringUtil;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.datatypes.XMLDatatypeUtil;
import org.eclipse.rdf4j.model.util.Literals;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryResultHandlerException;
import org.eclipse.rdf4j.query.TupleQueryResultHandlerException;
import org.eclipse.rdf4j.query.resultio.AbstractQueryResultWriter;
import org.eclipse.rdf4j.query.resultio.TupleQueryResultFormat;
import org.eclipse.rdf4j.query.resultio.TupleQueryResultWriter;

/**
 * TupleQueryResultWriter for the SPARQL TSV (Tab-Separated Values) format.
 * 
 * @see <a href="http://www.w3.org/TR/sparql11-results-csv-tsv/#tsv">SPARQL 1.1
 *      Query Results TSV Format</a>
 * @author Jeen Broekstra
 */
public class SPARQLResultsTSVWriter extends AbstractQueryResultWriter implements TupleQueryResultWriter {

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
