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
package org.openrdf.rio.rdfxml;

import junit.framework.Test;

/**
 * JUnit test for the RDF/XML parser that uses the test manifest that is
 * available <a
 * href="http://www.w3.org/2000/10/rdf-tests/rdfcore/Manifest.rdf">online</a>.
 */
public class RDFXMLParserTest extends RDFXMLParserTestCase {

	public static Test suite()
		throws Exception
	{
		return new RDFXMLParserTest().createTestSuite();
	}

	@Override
	protected RDFXMLParser createRDFParser() {
		RDFXMLParser rdfxmlParser = new RDFXMLParser();
		rdfxmlParser.setParseStandAloneDocuments(true);
		return rdfxmlParser;
	}
}
