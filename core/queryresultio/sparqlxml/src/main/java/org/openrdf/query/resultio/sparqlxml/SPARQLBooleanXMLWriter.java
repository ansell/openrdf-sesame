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

import static org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLConstants.BOOLEAN_FALSE;
import static org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLConstants.BOOLEAN_TAG;
import static org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLConstants.BOOLEAN_TRUE;
import static org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLConstants.HEAD_TAG;
import static org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLConstants.HREF_ATT;
import static org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLConstants.LINK_TAG;
import static org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLConstants.NAMESPACE;
import static org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLConstants.ROOT_TAG;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import info.aduna.xml.XMLWriter;

import org.openrdf.query.BooleanQueryResultHandlerException;
import org.openrdf.query.resultio.BooleanQueryResultFormat;
import org.openrdf.query.resultio.BooleanQueryResultWriter;

/**
 * A {@link BooleanQueryResultWriter} that writes boolean query results in the
 * <a href="http://www.w3.org/TR/rdf-sparql-XMLres/">SPARQL Query Results XML
 * Format</a>.
 */
public class SPARQLBooleanXMLWriter extends
		SPARQLXMLWriterBase<BooleanQueryResultFormat, BooleanQueryResultHandlerException> implements
		BooleanQueryResultWriter
{

	/*--------------*
	 * Constructors *
	 *--------------*/

	public SPARQLBooleanXMLWriter(OutputStream out) {
		super(out);
	}

	public SPARQLBooleanXMLWriter(XMLWriter xmlWriter) {
		super(xmlWriter);
	}

	/*---------*
	 * Methods *
	 *---------*/

	@Override
	public final BooleanQueryResultFormat getBooleanQueryResultFormat() {
		return BooleanQueryResultFormat.SPARQL;
	}

	@Override
	public final BooleanQueryResultFormat getQueryResultFormat() {
		return getBooleanQueryResultFormat();
	}

	@Override
	public void startDocument()
		throws BooleanQueryResultHandlerException
	{
		documentOpen = true;
		headerComplete = false;

		try {
			xmlWriter.startDocument();
			xmlWriter.setAttribute("xmlns", NAMESPACE);
		}
		catch (IOException e) {
			throw new BooleanQueryResultHandlerException(e);
		}

	}

	@Override
	public void handleStylesheet(String url)
		throws BooleanQueryResultHandlerException
	{
		try {
			xmlWriter.writeStylesheet(url);
		}
		catch (IOException e) {
			throw new BooleanQueryResultHandlerException(e);
		}
	}

	@Override
	public void startHeader()
		throws BooleanQueryResultHandlerException
	{
		try {
			xmlWriter.startTag(ROOT_TAG);

			xmlWriter.startTag(HEAD_TAG);
		}
		catch (IOException e) {
			throw new BooleanQueryResultHandlerException(e);
		}

	}

	@Override
	public void handleLinks(List<String> linkUrls)
		throws BooleanQueryResultHandlerException
	{
		try {
			// Write link URLs
			for (String name : linkUrls) {
				xmlWriter.setAttribute(HREF_ATT, name);
				xmlWriter.emptyElement(LINK_TAG);
			}
		}
		catch (IOException e) {
			throw new BooleanQueryResultHandlerException(e);
		}
	}

	@Override
	public void endHeader()
		throws BooleanQueryResultHandlerException
	{
		try {
			xmlWriter.endTag(HEAD_TAG);
			headerComplete = true;
		}
		catch (IOException e) {
			throw new BooleanQueryResultHandlerException(e);
		}
	}

	@Override
	public void write(boolean value)
		throws IOException
	{
		try {
			handleBoolean(value);
		}
		catch (BooleanQueryResultHandlerException e) {
			if (e.getCause() != null && e.getCause() instanceof IOException) {
				throw (IOException)e.getCause();
			}
			else {
				throw new IOException(e);
			}
		}
	}

	@Override
	public void handleBoolean(boolean value)
		throws BooleanQueryResultHandlerException
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
			throw new BooleanQueryResultHandlerException(e);
		}
	}
}
