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

import static org.junit.Assert.*;

import java.io.StringReader;

import org.junit.Ignore;
import org.junit.Test;

import org.openrdf.model.Model;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.rio.ParserConfig;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.openrdf.rio.RDFParser.DatatypeHandling;
import org.openrdf.rio.helpers.XMLParserSettings;
import org.openrdf.rio.helpers.StatementCollector;

/**
 * Custom tests for RDFXML Parser.
 * 
 * @author Michael Grove
 */
public class RDFXMLParserCustomTest {

	/**
	 * Test with the default ParserConfig settings. Ie, setParserConfig is not
	 * called.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testEntityExpansionDefaultSettings()
		throws Exception
	{
		final Model aGraph = new LinkedHashModel();
		RDFParser aParser = Rio.createParser(RDFFormat.RDFXML);
		aParser.setRDFHandler(new StatementCollector(aGraph));

		try {
			// this should trigger a SAX parse exception that will blow up at the
			// 64k
			// entity limit rather than OOMing
			aParser.parse(
					this.getClass().getResourceAsStream("/testcases/rdfxml/openrdf/bad-entity-expansion-limit.rdf"),
					"http://example.org");
			fail("Parser did not throw an exception");
		}
		catch (RDFParseException e) {
			// assertTrue(e.getMessage().contains(
			// "The parser has encountered more than \"64,000\" entity expansions in this document; this is the limit imposed by the application."));
		}
	}

	/**
	 * Test with unrelated ParserConfig settings
	 * 
	 * @throws Exception
	 */
	@Test
	public void testEntityExpansionUnrelatedSettings()
		throws Exception
	{
		final Model aGraph = new LinkedHashModel();
		RDFParser aParser = Rio.createParser(RDFFormat.RDFXML);
		aParser.setRDFHandler(new StatementCollector(aGraph));

		ParserConfig config = new ParserConfig();
		aParser.setParserConfig(config);

		try {
			// this should trigger a SAX parse exception that will blow up at the
			// 64k entity limit rather than OOMing
			aParser.parse(
					this.getClass().getResourceAsStream("/testcases/rdfxml/openrdf/bad-entity-expansion-limit.rdf"),
					"http://example.org");
			fail("Parser did not throw an exception");
		}
		catch (RDFParseException e) {
			// assertTrue(e.getMessage().contains(
			// "The parser has encountered more than \"64,000\" entity expansions in this document; this is the limit imposed by the application."));
		}
	}

	/**
	 * Test with Secure processing setting on.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testEntityExpansionSecureProcessing()
		throws Exception
	{
		final Model aGraph = new LinkedHashModel();
		RDFParser aParser = Rio.createParser(RDFFormat.RDFXML);
		aParser.setRDFHandler(new StatementCollector(aGraph));

		ParserConfig config = new ParserConfig();
		config.set(XMLParserSettings.SECURE_PROCESSING, true);
		aParser.setParserConfig(config);

		try {
			// this should trigger a SAX parse exception that will blow up at the
			// 64k entity limit rather than OOMing
			aParser.parse(
					this.getClass().getResourceAsStream("/testcases/rdfxml/openrdf/bad-entity-expansion-limit.rdf"),
					"http://example.org");
			fail("Parser did not throw an exception");
		}
		catch (RDFParseException e) {
			// assertTrue(e.getMessage().contains(
			// "The parser has encountered more than \"64,000\" entity expansions in this document; this is the limit imposed by the application."));
		}
	}

	/**
	 * Test with Secure processing setting off.
	 * <p>
	 * IMPORTANT: Only turn this on to verify it is still working, as there is no
	 * way to safely perform this test.
	 * <p>
	 * WARNING: This test will cause an OutOfMemoryException when it eventually
	 * fails, as it will eventually fail.
	 * 
	 * @throws Exception
	 */
	@Ignore
	@Test(timeout = 10000)
	public void testEntityExpansionNoSecureProcessing()
		throws Exception
	{
		final Model aGraph = new LinkedHashModel();
		RDFParser aParser = Rio.createParser(RDFFormat.RDFXML);
		aParser.setRDFHandler(new StatementCollector(aGraph));

		ParserConfig config = new ParserConfig();
		config.set(XMLParserSettings.SECURE_PROCESSING, false);
		aParser.setParserConfig(config);

		try {
			// IMPORTANT: This will not use the entity limit
			aParser.parse(
					this.getClass().getResourceAsStream("/testcases/rdfxml/openrdf/bad-entity-expansion-limit.rdf"),
					"http://example.org");
			fail("Parser did not throw an exception");
		}
		catch (RDFParseException e) {
			// assertTrue(e.getMessage().contains(
			// "The parser has encountered more than \"64,000\" entity expansions in this document; this is the limit imposed by the application."));
		}
	}

	@Test
	public void testParseCollection()
		throws Exception
	{
		// Example from:
		// http://www.w3.org/TR/rdf-syntax-grammar/#section-Syntax-parsetype-Collection
		StringBuilder string = new StringBuilder();
		string.append("<?xml version=\"1.0\"?>\n");
		string.append("<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" ");
		string.append(" xmlns:ex=\"http://example.org/stuff/1.0/\"> \n");
		string.append("  <rdf:Description rdf:about=\"http://example.org/basket\"> \n");
		string.append("    <ex:hasFruit rdf:parseType=\"Collection\">\n");
		string.append("      <rdf:Description rdf:about=\"http://example.org/banana\"/>\n");
		string.append("      <rdf:Description rdf:about=\"http://example.org/apple\"/>\n");
		string.append("      <rdf:Description rdf:about=\"http://example.org/pear\"/>\n");
		string.append("    </ex:hasFruit>\n");
		string.append("  </rdf:Description>\n");
		string.append("</rdf:RDF>");

		Model parse = Rio.parse(new StringReader(string.toString()), "", RDFFormat.RDFXML);
		Rio.write(parse, System.out, RDFFormat.NTRIPLES);
		assertEquals(7, parse.size());
		assertEquals(3, parse.filter(null, RDF.FIRST, null).size());
		assertEquals(3, parse.filter(null, RDF.REST, null).size());
		assertEquals(1, parse.filter(null, null, RDF.NIL).size());
	}

	@Test
	public void testSupportedSettings()
		throws Exception
	{
		assertEquals(21, Rio.createParser(RDFFormat.RDFXML).getSupportedSettings().size());
	}
}
