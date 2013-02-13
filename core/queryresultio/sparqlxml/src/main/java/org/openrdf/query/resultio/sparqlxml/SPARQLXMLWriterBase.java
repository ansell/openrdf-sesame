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
package org.openrdf.query.resultio.sparqlxml;

import static org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLConstants.BINDING_NAME_ATT;
import static org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLConstants.BINDING_TAG;
import static org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLConstants.BNODE_TAG;
import static org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLConstants.BOOLEAN_FALSE;
import static org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLConstants.BOOLEAN_TAG;
import static org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLConstants.BOOLEAN_TRUE;
import static org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLConstants.HEAD_TAG;
import static org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLConstants.HREF_ATT;
import static org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLConstants.LINK_TAG;
import static org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLConstants.LITERAL_DATATYPE_ATT;
import static org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLConstants.LITERAL_LANG_ATT;
import static org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLConstants.LITERAL_TAG;
import static org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLConstants.NAMESPACE;
import static org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLConstants.RESULT_SET_TAG;
import static org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLConstants.RESULT_TAG;
import static org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLConstants.ROOT_TAG;
import static org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLConstants.URI_TAG;
import static org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLConstants.VAR_NAME_ATT;
import static org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLConstants.VAR_TAG;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import info.aduna.xml.XMLWriter;

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
 * SPARQLBooleanXMLWriter and SPARQLResultsXMLWriter.
 * 
 * @author Peter Ansell
 */
abstract class SPARQLXMLWriterBase implements QueryResultWriter {

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * XMLWriter to write XML to.
	 */
	protected XMLWriter xmlWriter;

	protected boolean documentOpen = false;

	protected boolean headerComplete = false;

	protected boolean tupleVariablesFound = false;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public SPARQLXMLWriterBase(OutputStream out) {
		this(new XMLWriter(out));
	}

	public SPARQLXMLWriterBase(XMLWriter xmlWriter) {
		this.xmlWriter = xmlWriter;
		this.xmlWriter.setPrettyPrint(true);
	}

	/*---------*
	 * Methods *
	 *---------*/

	/**
	 * Enables/disables addition of indentation characters and newlines in the
	 * XML document. By default, pretty-printing is set to <tt>true</tt>. If set
	 * to <tt>false</tt>, no indentation and newlines are added to the XML
	 * document. This method has to be used before writing starts (that is,
	 * before {@link #startDocument} is called).
	 */
	public void setPrettyPrint(boolean prettyPrint) {
		xmlWriter.setPrettyPrint(prettyPrint);
	}

	protected void endDocument()
		throws IOException
	{
		xmlWriter.endTag(ROOT_TAG);

		xmlWriter.endDocument();

		headerComplete = false;
		documentOpen = false;
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
				xmlWriter.textElement(BOOLEAN_TAG, BOOLEAN_TRUE);
			}
			else {
				xmlWriter.textElement(BOOLEAN_TAG, BOOLEAN_FALSE);
			}

			endDocument();
		}
		catch (IOException e) {
			throw new QueryResultHandlerException(e);
		}
	}

	@Override
	public void startDocument()
		throws QueryResultHandlerException
	{
		documentOpen = true;
		headerComplete = false;

		try {
			xmlWriter.startDocument();

			xmlWriter.setAttribute("xmlns", NAMESPACE);
		}
		catch (IOException e) {
			throw new QueryResultHandlerException(e);
		}
	}

	@Override
	public void handleStylesheet(String url)
		throws QueryResultHandlerException
	{
		try {
			xmlWriter.writeStylesheet(url);
		}
		catch (IOException e) {
			throw new QueryResultHandlerException(e);
		}
	}

	@Override
	public void startHeader()
		throws QueryResultHandlerException
	{
		try {
			xmlWriter.startTag(ROOT_TAG);

			xmlWriter.startTag(HEAD_TAG);
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
			// Write link URLs
			for (String name : linkUrls) {
				xmlWriter.setAttribute(HREF_ATT, name);
				xmlWriter.emptyElement(LINK_TAG);
			}
		}
		catch (IOException e) {
			throw new QueryResultHandlerException(e);
		}
	}

	@Override
	public void endHeader()
		throws QueryResultHandlerException
	{
		try {
			xmlWriter.endTag(HEAD_TAG);

			if (tupleVariablesFound) {
				// Write start of results, which must always exist, even if there
				// are
				// no result bindings
				xmlWriter.startTag(RESULT_SET_TAG);
			}

			headerComplete = true;
		}
		catch (IOException e) {
			throw new QueryResultHandlerException(e);
		}
	}

	@Override
	public void startQueryResult(List<String> bindingNames)
		throws TupleQueryResultHandlerException
	{
		try {
			if (!documentOpen) {
				startDocument();
				startHeader();
			}
			tupleVariablesFound = true;
			// Write binding names
			for (String name : bindingNames) {
				xmlWriter.setAttribute(VAR_NAME_ATT, name);
				xmlWriter.emptyElement(VAR_TAG);
			}
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
			xmlWriter.endTag(RESULT_SET_TAG);
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
	public void handleSolution(BindingSet bindingSet)
		throws TupleQueryResultHandlerException
	{
		try {
			if (!headerComplete) {
				endHeader();
			}

			xmlWriter.startTag(RESULT_TAG);

			for (Binding binding : bindingSet) {
				xmlWriter.setAttribute(BINDING_NAME_ATT, binding.getName());
				xmlWriter.startTag(BINDING_TAG);

				writeValue(binding.getValue());

				xmlWriter.endTag(BINDING_TAG);
			}

			xmlWriter.endTag(RESULT_TAG);
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

	private void writeValue(Value value)
		throws IOException
	{
		if (value instanceof URI) {
			writeURI((URI)value);
		}
		else if (value instanceof BNode) {
			writeBNode((BNode)value);
		}
		else if (value instanceof Literal) {
			writeLiteral((Literal)value);
		}
	}

	private void writeURI(URI uri)
		throws IOException
	{
		xmlWriter.textElement(URI_TAG, uri.toString());
	}

	private void writeBNode(BNode bNode)
		throws IOException
	{
		xmlWriter.textElement(BNODE_TAG, bNode.getID());
	}

	private void writeLiteral(Literal literal)
		throws IOException
	{
		if (literal.getLanguage() != null) {
			xmlWriter.setAttribute(LITERAL_LANG_ATT, literal.getLanguage());
		}

		if (literal.getDatatype() != null) {
			URI datatype = literal.getDatatype();
			xmlWriter.setAttribute(LITERAL_DATATYPE_ATT, datatype.toString());
		}

		xmlWriter.textElement(LITERAL_TAG, literal.getLabel());
	}
}
