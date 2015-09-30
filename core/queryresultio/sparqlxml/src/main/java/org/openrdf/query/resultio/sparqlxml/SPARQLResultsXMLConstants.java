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
package org.openrdf.query.resultio.sparqlxml;

/**
 * Interface defining tags and attribute names that are used in SPARQL Results
 * Documents. See <a href="http://www.w3.org/TR/rdf-sparql-XMLres/">SPARQL Query
 * Results XML Format</a> for the definition of this format.
 * 
 * @author Arjohn Kampman
 */
interface SPARQLResultsXMLConstants {

	public static final String NAMESPACE = "http://www.w3.org/2005/sparql-results#";

	public static final String ROOT_TAG = "sparql";

	public static final String HEAD_TAG = "head";

	public static final String LINK_TAG = "link";

	public static final String VAR_TAG = "variable";

	public static final String VAR_NAME_ATT = "name";

	public static final String HREF_ATT = "href";

	public static final String BOOLEAN_TAG = "boolean";

	public static final String BOOLEAN_TRUE = "true";

	public static final String BOOLEAN_FALSE = "false";

	public static final String RESULT_SET_TAG = "results";

	public static final String RESULT_TAG = "result";

	public static final String BINDING_TAG = "binding";

	public static final String BINDING_NAME_ATT = "name";

	public static final String URI_TAG = "uri";

	public static final String BNODE_TAG = "bnode";

	public static final String LITERAL_TAG = "literal";

	public static final String LITERAL_LANG_ATT = "xml:lang";

	public static final String LITERAL_DATATYPE_ATT = "datatype";

	public static final String UNBOUND_TAG = "unbound";
	
	public static final String QNAME = "q:qname";
}
