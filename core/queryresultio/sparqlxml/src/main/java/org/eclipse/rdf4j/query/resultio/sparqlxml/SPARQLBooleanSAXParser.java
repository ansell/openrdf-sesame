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
package org.eclipse.rdf4j.query.resultio.sparqlxml;

import static org.eclipse.rdf4j.query.resultio.sparqlxml.SPARQLResultsXMLConstants.BOOLEAN_FALSE;
import static org.eclipse.rdf4j.query.resultio.sparqlxml.SPARQLResultsXMLConstants.BOOLEAN_TAG;
import static org.eclipse.rdf4j.query.resultio.sparqlxml.SPARQLResultsXMLConstants.BOOLEAN_TRUE;

import java.util.Map;

import org.eclipse.rdf4j.common.xml.SimpleSAXAdapter;
import org.xml.sax.SAXException;

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