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

import static org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLConstants.ROOT_TAG;

import java.io.IOException;
import java.io.OutputStream;

import info.aduna.xml.XMLWriter;

import org.openrdf.query.resultio.QueryResultFormat;
import org.openrdf.query.resultio.QueryResultWriter;

/**
 * An abstract class to implement the base functionality for both
 * SPARQLBooleanXMLWriter and SPARQLResultsXMLWriter.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 */
abstract class SPARQLXMLWriterBase<T extends QueryResultFormat> implements QueryResultWriter<T> {

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

	protected void endDocument()
		throws IOException
	{
		xmlWriter.endTag(ROOT_TAG);

		xmlWriter.endDocument();

		headerComplete = false;
		documentOpen = false;
	}

}
