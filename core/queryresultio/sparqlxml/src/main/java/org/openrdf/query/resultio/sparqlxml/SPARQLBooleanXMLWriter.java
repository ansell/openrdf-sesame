/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.resultio.sparqlxml;

import static org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLConstants.BOOLEAN_TAG;
import static org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLConstants.BOOLEAN_TRUE;
import static org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLConstants.HEAD_TAG;
import static org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLConstants.NAMESPACE;
import static org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLConstants.ROOT_TAG;

import java.io.IOException;
import java.io.OutputStream;

import info.aduna.xml.XMLWriter;

import org.openrdf.query.resultio.BooleanQueryResultFormat;
import org.openrdf.query.resultio.BooleanQueryResultWriter;

/**
 * A {@link BooleanQueryResultWriter} that writes boolean query results in the
 * <a href="http://www.w3.org/TR/rdf-sparql-XMLres/">SPARQL Query Results XML
 * Format</a>.
 */
public class SPARQLBooleanXMLWriter implements BooleanQueryResultWriter {

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

	public SPARQLBooleanXMLWriter(OutputStream out) {
		this(new XMLWriter(out));
	}

	public SPARQLBooleanXMLWriter(XMLWriter xmlWriter) {
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
	 * before {@link #write} is called).
	 */
	public void setPrettyPrint(boolean prettyPrint) {
		xmlWriter.setPrettyPrint(prettyPrint);
	}

	public final BooleanQueryResultFormat getBooleanQueryResultFormat() {
		return BooleanQueryResultFormat.SPARQL;
	}

	public void write(boolean value)
		throws IOException
	{
		xmlWriter.startDocument();
		xmlWriter.setAttribute("xmlns", NAMESPACE);
		xmlWriter.startTag(ROOT_TAG);
		xmlWriter.emptyElement(HEAD_TAG);

		if (value) {
			xmlWriter.textElement(BOOLEAN_TAG, BOOLEAN_TRUE);
		}
		else {
			xmlWriter.textElement(BOOLEAN_TAG, SPARQLResultsXMLConstants.BOOLEAN_FALSE);
		}

		xmlWriter.endTag(ROOT_TAG);
		xmlWriter.endDocument();
	}
}
