/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2013.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.resultio.sparqlxml;

import static org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLConstants.HEAD_TAG;
import static org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLConstants.HREF_ATT;
import static org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLConstants.LINK_TAG;
import static org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLConstants.NAMESPACE;
import static org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLConstants.ROOT_TAG;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import info.aduna.xml.XMLWriter;

import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.query.resultio.QueryResultWriter;

/**
 * An abstract class to implement the base functionality for both
 * SPARQLBooleanXMLWriter and SPARQLResultsXMLWriter.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
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

	public void startDocument()
		throws TupleQueryResultHandlerException
	{
		documentOpen = true;
		headerComplete = false;

		try {
			xmlWriter.startDocument();

			xmlWriter.setAttribute("xmlns", NAMESPACE);
		}
		catch (IOException e) {
			throw new TupleQueryResultHandlerException(e);
		}
	}

	public void handleStylesheet(String url)
		throws TupleQueryResultHandlerException
	{
		try {
			xmlWriter.writeStylesheet(url);
		}
		catch (IOException e) {
			throw new TupleQueryResultHandlerException(e);
		}
	}

	public void startHeader()
		throws TupleQueryResultHandlerException
	{
		try {
			xmlWriter.startTag(ROOT_TAG);

			xmlWriter.startTag(HEAD_TAG);
		}
		catch (IOException e) {
			throw new TupleQueryResultHandlerException(e);
		}
	}

	public void handleLinks(List<String> linkUrls)
		throws TupleQueryResultHandlerException
	{
		try {
			// Write link URLs
			for (String name : linkUrls) {
				xmlWriter.setAttribute(HREF_ATT, name);
				xmlWriter.emptyElement(LINK_TAG);
			}
		}
		catch (IOException e) {
			throw new TupleQueryResultHandlerException(e);
		}
	}

	protected void endDocument()
		throws IOException
	{
		xmlWriter.endTag(ROOT_TAG);

		xmlWriter.endDocument();

		headerComplete = false;
		documentOpen = false;
	}

}
