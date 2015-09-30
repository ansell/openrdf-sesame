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
package org.eclipse.rdf4j.rio.rdfxml;

import static org.junit.Assert.*;

import java.io.StringReader;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.rio.ParserConfig;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.RDFParser.DatatypeHandling;
import org.eclipse.rdf4j.rio.helpers.StatementCollector;
import org.eclipse.rdf4j.rio.helpers.XMLParserSettings;
import org.junit.Ignore;
import org.junit.Test;

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
			// "The parser has encountered more than \"64,000\" entity expansions in this document; this is the limit imposed by the "));
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
			// "The parser has encountered more than \"64,000\" entity expansions in this document; this is the limit imposed by the "));
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
			// "The parser has encountered more than \"64,000\" entity expansions in this document; this is the limit imposed by the "));
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
			// "The parser has encountered more than \"64,000\" entity expansions in this document; this is the limit imposed by the"));
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
	public void testParseCommentAtStart()
		throws Exception
	{
		// Example from:
		// http://www.w3.org/TR/rdf-syntax-grammar/#section-Syntax-parsetype-Collection
		StringBuilder string = new StringBuilder();
		string.append("<!-- Test comment for parser to ignore -->\n");
		string.append("<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" ");
		string.append(" xmlns:ex=\"http://example.org/stuff/1.0/\"> \n");
		string.append("  <rdf:Description rdf:about=\"http://example.org/basket\"> \n");
		string.append("    <ex:hasFruit>\n");
		string.append("    	Mango\n");
		string.append("    </ex:hasFruit>\n");
		string.append("  </rdf:Description>\n");
		string.append("</rdf:RDF>");

		Model parse = Rio.parse(new StringReader(string.toString()), "", RDFFormat.RDFXML);
		Rio.write(parse, System.out, RDFFormat.NTRIPLES);
		assertEquals(1, parse.size());
	}

	@Test
	public void testSupportedSettings()
		throws Exception
	{
		assertEquals(21, Rio.createParser(RDFFormat.RDFXML).getSupportedSettings().size());
	}
}
