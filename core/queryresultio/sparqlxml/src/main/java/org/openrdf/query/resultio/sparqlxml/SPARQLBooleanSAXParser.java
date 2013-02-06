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

import java.util.Map;

import org.xml.sax.SAXException;

import info.aduna.xml.SimpleSAXAdapter;

class SPARQLBooleanSAXParser extends SimpleSAXAdapter {

	/*-----------*
	 * Variables *
	 *-----------*/

	private Boolean value;

	/*---------*
	 * Methods *
	 *---------*/

	@Override
	public void startTag(String tagName, Map<String, String> atts, String text)
		throws SAXException
	{
		if (BOOLEAN_TAG.equals(tagName)) {
			if (BOOLEAN_TRUE.equals(text)) {
				value = true;
			}
			else if (BOOLEAN_FALSE.equals(text)) {
				value = false;
			}
			else {
				throw new SAXException("Illegal value for element " + BOOLEAN_TAG + ": " + text);
			}
		}
	}

	@Override
	public void endDocument()
		throws SAXException
	{
		if (value == null) {
			throw new SAXException("Malformed document, " + BOOLEAN_TAG + " element not found");
		}
	}

	public boolean getValue() {
		return value != null && value;
	}
}