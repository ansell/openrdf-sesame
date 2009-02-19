/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
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
