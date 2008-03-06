/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.queryresult.xml;

import static org.openrdf.queryresult.xml.SPARQLResultsXMLConstants.BINDING_NAME_ATT;
import static org.openrdf.queryresult.xml.SPARQLResultsXMLConstants.BINDING_TAG;
import static org.openrdf.queryresult.xml.SPARQLResultsXMLConstants.BNODE_TAG;
import static org.openrdf.queryresult.xml.SPARQLResultsXMLConstants.HEAD_TAG;
import static org.openrdf.queryresult.xml.SPARQLResultsXMLConstants.LITERAL_DATATYPE_ATT;
import static org.openrdf.queryresult.xml.SPARQLResultsXMLConstants.LITERAL_LANG_ATT;
import static org.openrdf.queryresult.xml.SPARQLResultsXMLConstants.LITERAL_TAG;
import static org.openrdf.queryresult.xml.SPARQLResultsXMLConstants.NAMESPACE;
import static org.openrdf.queryresult.xml.SPARQLResultsXMLConstants.RESULT_SET_DISTINCT_ATT;
import static org.openrdf.queryresult.xml.SPARQLResultsXMLConstants.RESULT_SET_ORDERED_ATT;
import static org.openrdf.queryresult.xml.SPARQLResultsXMLConstants.RESULT_SET_TAG;
import static org.openrdf.queryresult.xml.SPARQLResultsXMLConstants.RESULT_TAG;
import static org.openrdf.queryresult.xml.SPARQLResultsXMLConstants.ROOT_TAG;
import static org.openrdf.queryresult.xml.SPARQLResultsXMLConstants.URI_TAG;
import static org.openrdf.queryresult.xml.SPARQLResultsXMLConstants.VAR_NAME_ATT;
import static org.openrdf.queryresult.xml.SPARQLResultsXMLConstants.VAR_TAG;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.openrdf.queryresult.Binding;
import org.openrdf.queryresult.TupleQueryResultFormat;
import org.openrdf.queryresult.TupleQueryResultHandlerException;
import org.openrdf.queryresult.TupleQueryResultWriter;
import org.openrdf.queryresult.Solution;
import org.openrdf.util.xml.XMLWriter;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;


/**
 * A TupleQueryResultWriter that writes query results in the <a
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
	private XMLWriter _xmlWriter;


	/*---------*
	 * Methods *
	 *---------*/
	public void setOutputStream(OutputStream out) {
		setWriter(new XMLWriter(out));
	}

	public void setWriter(XMLWriter xmlWriter) {
		_xmlWriter = xmlWriter;
		_xmlWriter.setPrettyPrint(true);
	}

	public final TupleQueryResultFormat getQueryResultFormat() {
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
		_xmlWriter.setPrettyPrint(prettyPrint);
	}

	public void startQueryResult(List<String> bindingNames, boolean distinct, boolean ordered)
		throws TupleQueryResultHandlerException
	{
		try {
			_xmlWriter.startDocument();

			_xmlWriter.setAttribute("xmlns", NAMESPACE);
			_xmlWriter.startTag(ROOT_TAG);

			// Write header
			_xmlWriter.startTag(HEAD_TAG);
			for (String name : bindingNames) {
				_xmlWriter.setAttribute(VAR_NAME_ATT, name);
				_xmlWriter.emptyElement(VAR_TAG);
			}
			_xmlWriter.endTag(HEAD_TAG);

			// Write start of results
			_xmlWriter.setAttribute(RESULT_SET_ORDERED_ATT, ordered);
			_xmlWriter.setAttribute(RESULT_SET_DISTINCT_ATT, distinct);
			_xmlWriter.startTag(RESULT_SET_TAG);
		}
		catch (IOException e) {
			throw new TupleQueryResultHandlerException(e);
		}
	}

	public void endQueryResult()
		throws TupleQueryResultHandlerException
	{
		try {
			_xmlWriter.endTag(RESULT_SET_TAG);
			_xmlWriter.endTag(ROOT_TAG);

			_xmlWriter.endDocument();
		}
		catch (IOException e) {
			throw new TupleQueryResultHandlerException(e);
		}
	}

	public void handleSolution(Solution solution)
		throws TupleQueryResultHandlerException
	{
		try {
			_xmlWriter.startTag(RESULT_TAG);

			for (Binding binding : solution) {
				_xmlWriter.setAttribute(BINDING_NAME_ATT, binding.getName());
				_xmlWriter.startTag(BINDING_TAG);

				_writeValue(binding.getValue());

				_xmlWriter.endTag(BINDING_TAG);
			}

			_xmlWriter.endTag(RESULT_TAG);
		}
		catch (IOException e) {
			throw new TupleQueryResultHandlerException(e);
		}
	}

	private void _writeValue(Value value)
		throws IOException
	{
		if (value instanceof URI) {
			_writeURI((URI)value);
		}
		else if (value instanceof BNode) {
			_writeBNode((BNode)value);
		}
		else if (value instanceof Literal) {
			_writeLiteral((Literal)value);
		}
	}

	private void _writeURI(URI uri)
		throws IOException
	{
		_xmlWriter.textElement(URI_TAG, uri.toString());
	}

	private void _writeBNode(BNode bNode)
		throws IOException
	{
		_xmlWriter.textElement(BNODE_TAG, bNode.getID());
	}

	private void _writeLiteral(Literal literal)
		throws IOException
	{
		if (literal.getLanguage() != null) {
			_xmlWriter.setAttribute(LITERAL_LANG_ATT, literal.getLanguage());
		}
		else if (literal.getDatatype() != null) {
			URI datatype = literal.getDatatype();
			_xmlWriter.setAttribute(LITERAL_DATATYPE_ATT, datatype.toString());
		}

		_xmlWriter.textElement(LITERAL_TAG, literal.getLabel());
	}
}
