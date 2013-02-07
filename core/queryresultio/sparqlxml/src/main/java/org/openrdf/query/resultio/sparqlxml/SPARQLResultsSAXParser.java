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
import static org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLConstants.LITERAL_DATATYPE_ATT;
import static org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLConstants.LITERAL_LANG_ATT;
import static org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLConstants.LITERAL_TAG;
import static org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLConstants.RESULT_SET_TAG;
import static org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLConstants.RESULT_TAG;
import static org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLConstants.URI_TAG;
import static org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLConstants.VAR_NAME_ATT;
import static org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLConstants.VAR_TAG;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.xml.sax.SAXException;

import info.aduna.xml.SimpleSAXAdapter;

import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.QueryResultHandler;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.query.impl.MapBindingSet;

class SPARQLResultsSAXParser extends SimpleSAXAdapter {

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * The variable names that are specified in the header.
	 */
	private List<String> bindingNames;

	/**
	 * The most recently parsed binding name.
	 */
	private String currentBindingName;

	/**
	 * The most recently parsed value.
	 */
	private Value currentValue;

	/**
	 * The bound variables for the current result.
	 */
	private MapBindingSet currentSolution;

	private ValueFactory valueFactory;

	private QueryResultHandler handler;

	public SPARQLResultsSAXParser(ValueFactory valueFactory, QueryResultHandler handler) {
		this.valueFactory = valueFactory;
		this.handler = handler;
	}

	/*---------*
	 * Methods *
	 *---------*/

	@Override
	public void startDocument()
		throws SAXException
	{
		bindingNames = new ArrayList<String>();
		currentValue = null;
	}

	@Override
	public void endDocument()
		throws SAXException
	{
		try {
			handler.endQueryResult();
		}
		catch (TupleQueryResultHandlerException e) {
			throw new SAXException(e);
		}
	}

	@Override
	public void startTag(String tagName, Map<String, String> atts, String text)
		throws SAXException
	{
		if (BINDING_TAG.equals(tagName)) {
			currentBindingName = atts.get(BINDING_NAME_ATT);

			if (currentBindingName == null) {
				throw new SAXException(BINDING_NAME_ATT + " attribute missing for " + BINDING_TAG + " element");
			}
		}
		else if (URI_TAG.equals(tagName)) {
			try {
				currentValue = valueFactory.createURI(text);
			}
			catch (IllegalArgumentException e) {
				// Malformed URI
				throw new SAXException(e.getMessage());
			}
		}
		else if (BNODE_TAG.equals(tagName)) {
			currentValue = valueFactory.createBNode(text);
		}
		else if (LITERAL_TAG.equals(tagName)) {
			String xmlLang = atts.get(LITERAL_LANG_ATT);
			String datatype = atts.get(LITERAL_DATATYPE_ATT);

			if (datatype != null) {
				try {
					currentValue = valueFactory.createLiteral(text, valueFactory.createURI(datatype));
				}
				catch (IllegalArgumentException e) {
					// Illegal datatype URI
					throw new SAXException(e.getMessage());
				}
			}
			else if (xmlLang != null) {
				currentValue = valueFactory.createLiteral(text, xmlLang);
			}
			else {
				currentValue = valueFactory.createLiteral(text);
			}
		}
		else if (RESULT_TAG.equals(tagName)) {
			currentSolution = new MapBindingSet(bindingNames.size());
		}
		else if (VAR_TAG.equals(tagName)) {
			String varName = atts.get(VAR_NAME_ATT);

			if (varName == null) {
				throw new SAXException(VAR_NAME_ATT + " missing for " + VAR_TAG + " element");
			}

			bindingNames.add(varName);
		}
		else if (RESULT_SET_TAG.equals(tagName)) {
			try {
				handler.startQueryResult(bindingNames);
			}
			catch (TupleQueryResultHandlerException e) {
				throw new SAXException(e);
			}
		}
	}

	@Override
	public void endTag(String tagName)
		throws SAXException
	{
		if (BINDING_TAG.equals(tagName)) {
			if (currentValue == null) {
				throw new SAXException("Value missing for " + BINDING_TAG + " element");
			}

			currentSolution.addBinding(currentBindingName, currentValue);

			currentBindingName = null;
			currentValue = null;
		}
		else if (RESULT_TAG.equals(tagName)) {
			try {
				handler.handleSolution(currentSolution);
				currentSolution = null;
			}
			catch (TupleQueryResultHandlerException e) {
				throw new SAXException(e);
			}
		}
	}
}