/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.resultio.sparqlxml;

import static org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLConstants.BINDING_NAME_ATT;
import static org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLConstants.BINDING_TAG;
import static org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLConstants.BNODE_TAG;
import static org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLConstants.HEAD_TAG;
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
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.query.resultio.TupleQueryResultFormat;
import org.openrdf.query.resultio.TupleQueryResultWriter;

/**
 * A {@link TupleQueryResultWriter} that writes tuple query results in the <a
 * href="http://www.w3.org/TR/rdf-sparql-XMLres/">SPARQL Query Results XML
 * Format</a>.
 */
public class SPARQLResultsXMLWriter implements TupleQueryResultWriter {

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * XMLWriter to write XML to.
	 */
	private XMLWriter xmlWriter;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public SPARQLResultsXMLWriter(OutputStream out) {
		this(new XMLWriter(out));
	}

	public SPARQLResultsXMLWriter(XMLWriter xmlWriter) {
		this.xmlWriter = xmlWriter;
		this.xmlWriter.setPrettyPrint(true);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public final TupleQueryResultFormat getTupleQueryResultFormat() {
		return TupleQueryResultFormat.SPARQL;
	}

	/**
	 * Enables/disables addition of indentation characters and newlines in the
	 * XML document. By default, pretty-printing is set to <tt>true</tt>. If
	 * set to <tt>false</tt>, no indentation and newlines are added to the XML
	 * document. This method has to be used before writing starts (that is,
	 * before {@link #startTupleSet} is called).
	 */
	public void setPrettyPrint(boolean prettyPrint) {
		xmlWriter.setPrettyPrint(prettyPrint);
	}

	public void startQueryResult(List<String> bindingNames)
		throws TupleQueryResultHandlerException
	{
		try {
			xmlWriter.startDocument();

			xmlWriter.setAttribute("xmlns", NAMESPACE);
			xmlWriter.startTag(ROOT_TAG);

			// Write header
			xmlWriter.startTag(HEAD_TAG);
			for (String name : bindingNames) {
				xmlWriter.setAttribute(VAR_NAME_ATT, name);
				xmlWriter.emptyElement(VAR_TAG);
			}
			xmlWriter.endTag(HEAD_TAG);

			// Write start of results
			xmlWriter.startTag(RESULT_SET_TAG);
		}
		catch (IOException e) {
			throw new TupleQueryResultHandlerException(e);
		}
	}

	public void endQueryResult()
		throws TupleQueryResultHandlerException
	{
		try {
			xmlWriter.endTag(RESULT_SET_TAG);
			xmlWriter.endTag(ROOT_TAG);

			xmlWriter.endDocument();
		}
		catch (IOException e) {
			throw new TupleQueryResultHandlerException(e);
		}
	}

	public void handleSolution(BindingSet bindingSet)
		throws TupleQueryResultHandlerException
	{
		try {
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
		else if (literal.getDatatype() != null) {
			URI datatype = literal.getDatatype();
			xmlWriter.setAttribute(LITERAL_DATATYPE_ATT, datatype.toString());
		}

		xmlWriter.textElement(LITERAL_TAG, literal.getLabel());
	}
}
