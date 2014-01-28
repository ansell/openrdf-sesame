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
package org.openrdf.rio;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collection;

import org.junit.Ignore;
import org.junit.Test;

import junit.framework.TestCase;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Model;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.rio.helpers.BasicParserSettings;
import org.openrdf.rio.helpers.StatementCollector;

/**
 * @author Arjohn Kampman
 */
public abstract class RDFWriterTest {

	protected RDFWriterFactory rdfWriterFactory;

	protected RDFParserFactory rdfParserFactory;

	protected RDFWriterTest(RDFWriterFactory writerF, RDFParserFactory parserF) {
		rdfWriterFactory = writerF;
		rdfParserFactory = parserF;
	}

	@Test
	public void testRoundTrip()
		throws RDFHandlerException, IOException, RDFParseException
	{
		String ex = "http://example.org/";

		ValueFactory vf = new ValueFactoryImpl();
		BNode bnode = vf.createBNode("anon");
		BNode bnodeNumeric = vf.createBNode("123");
		BNode bnodeDashes = vf.createBNode("a-b");
		URI uri1 = vf.createURI(ex, "uri1");
		URI uri2 = vf.createURI(ex, "uri2");
		Literal plainLit = vf.createLiteral("plain");
		Literal dtLit = vf.createLiteral(1);
		Literal langLit = vf.createLiteral("test", "en");
		Literal litWithNewlineAtEnd = vf.createLiteral("literal with newline at end\n");
		Literal litWithNewlineAtStart = vf.createLiteral("\nliteral with newline at start");
		Literal litWithMultipleNewlines = vf.createLiteral("\nliteral \nwith newline at start\n");
		Literal litWithSingleQuotes = vf.createLiteral("'''some single quote text''' - abc");
		Literal litWithDoubleQuotes = vf.createLiteral("\"\"\"some double quote text\"\"\" - abc");

		Statement st1 = vf.createStatement(bnode, uri1, plainLit);
		Statement st2 = vf.createStatement(bnodeNumeric, uri1, plainLit);
		Statement st3 = vf.createStatement(bnodeDashes, uri1, plainLit);
		Statement st4 = vf.createStatement(uri2, uri1, bnode);
		Statement st5 = vf.createStatement(uri2, uri1, bnodeNumeric);
		Statement st6 = vf.createStatement(uri2, uri1, bnodeDashes);
		Statement st7 = vf.createStatement(uri1, uri2, langLit, uri2);
		Statement st8 = vf.createStatement(uri1, uri2, dtLit);
		Statement st9 = vf.createStatement(uri1, uri2, litWithNewlineAtEnd);
		Statement st10 = vf.createStatement(uri1, uri2, litWithNewlineAtStart);
		Statement st11 = vf.createStatement(uri1, uri2, litWithMultipleNewlines);
		Statement st12 = vf.createStatement(uri1, uri2, litWithSingleQuotes);
		Statement st13 = vf.createStatement(uri1, uri2, litWithDoubleQuotes);

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		RDFWriter rdfWriter = rdfWriterFactory.getWriter(out);
		rdfWriter.handleNamespace("ex", ex);
		rdfWriter.startRDF();
		rdfWriter.handleStatement(st1);
		rdfWriter.handleStatement(st2);
		rdfWriter.handleStatement(st3);
		rdfWriter.handleStatement(st4);
		rdfWriter.handleStatement(st5);
		rdfWriter.handleStatement(st6);
		rdfWriter.handleStatement(st7);
		rdfWriter.handleStatement(st8);
		rdfWriter.handleStatement(st9);
		rdfWriter.handleStatement(st10);
		rdfWriter.handleStatement(st11);
		rdfWriter.handleStatement(st12);
		rdfWriter.handleStatement(st13);
		rdfWriter.endRDF();

		ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
		RDFParser rdfParser = rdfParserFactory.getParser();
		ParserConfig config = new ParserConfig();
		config.set(BasicParserSettings.FAIL_ON_UNKNOWN_DATATYPES, true);
		config.set(BasicParserSettings.FAIL_ON_UNKNOWN_LANGUAGES, true);
		rdfParser.setParserConfig(config);
		rdfParser.setValueFactory(vf);
		Model model = new LinkedHashModel();
		rdfParser.setRDFHandler(new StatementCollector(model));

		rdfParser.parse(in, "foo:bar");

		assertEquals("Unexpected number of statements", 13, model.size());
		// Test for three unique statements for blank nodes in subject position
		assertEquals(3, model.filter(null, uri1, plainLit).size());
		// Test for three unique statements for blank nodes in object position
		assertEquals(3, model.filter(uri2, uri1, null).size());
		if (rdfParser.getRDFFormat().supportsContexts()) {
			assertTrue(model.contains(st7));
		}
		else {
			assertTrue(model.contains(vf.createStatement(uri1, uri2, langLit)));
		}
		assertTrue(model.contains(st8));
		if (rdfParser.getRDFFormat().equals(RDFFormat.RDFXML)) {
			System.out.println("FIXME: SES-879: RDFXML Parser does not preserve literals starting or ending in newline character");
		}
		else {
			assertTrue("missing statement with literal ending with newline", model.contains(st9));
			assertTrue("missing statement with literal starting with newline", model.contains(st10));
			assertTrue("missing statement with literal containing multiple newlines", model.contains(st11));
		}
		assertTrue("missing statement with single quotes", model.contains(st12));
		assertTrue("missing statement with double quotes", model.contains(st13));
	}

	@Test
	public void testPrefixRedefinition()
		throws RDFHandlerException, RDFParseException, IOException
	{
		String ns1 = "a:";
		String ns2 = "b:";
		String ns3 = "c:";

		ValueFactory vf = new ValueFactoryImpl();
		URI uri1 = vf.createURI(ns1, "r1");
		URI uri2 = vf.createURI(ns2, "r2");
		URI uri3 = vf.createURI(ns3, "r3");
		Statement st = vf.createStatement(uri1, uri2, uri3);

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		RDFWriter rdfWriter = rdfWriterFactory.getWriter(out);
		rdfWriter.handleNamespace("", ns1);
		rdfWriter.handleNamespace("", ns2);
		rdfWriter.handleNamespace("", ns3);
		rdfWriter.startRDF();
		rdfWriter.handleStatement(st);
		rdfWriter.endRDF();

		ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
		RDFParser rdfParser = rdfParserFactory.getParser();
		rdfParser.setValueFactory(vf);
		StatementCollector stCollector = new StatementCollector();
		rdfParser.setRDFHandler(stCollector);

		rdfParser.parse(in, "foo:bar");

		Collection<Statement> statements = stCollector.getStatements();
		assertEquals("Unexpected number of statements", 1, statements.size());

		Statement parsedSt = statements.iterator().next();
		assertEquals("Written and parsed statements are not equal", st, parsedSt);
	}

	@Test
	public void testIllegalPrefix()
		throws RDFHandlerException, RDFParseException, IOException
	{
		String ns1 = "a:";
		String ns2 = "b:";
		String ns3 = "c:";

		ValueFactory vf = new ValueFactoryImpl();
		URI uri1 = vf.createURI(ns1, "r1");
		URI uri2 = vf.createURI(ns2, "r2");
		URI uri3 = vf.createURI(ns3, "r3");
		Statement st = vf.createStatement(uri1, uri2, uri3);

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		RDFWriter rdfWriter = rdfWriterFactory.getWriter(out);
		rdfWriter.handleNamespace("1", ns1);
		rdfWriter.handleNamespace("_", ns2);
		rdfWriter.handleNamespace("a%", ns3);
		rdfWriter.startRDF();
		rdfWriter.handleStatement(st);
		rdfWriter.endRDF();

		ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
		RDFParser rdfParser = rdfParserFactory.getParser();
		rdfParser.setValueFactory(vf);
		StatementCollector stCollector = new StatementCollector();
		rdfParser.setRDFHandler(stCollector);

		rdfParser.parse(in, "foo:bar");

		Collection<Statement> statements = stCollector.getStatements();
		assertEquals("Unexpected number of statements", 1, statements.size());

		Statement parsedSt = statements.iterator().next();
		assertEquals("Written and parsed statements are not equal", st, parsedSt);
	}

	@Test
	public void testDefaultNamespace()
		throws Exception
	{
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		RDFWriter rdfWriter = rdfWriterFactory.getWriter(out);
		rdfWriter.handleNamespace("", RDF.NAMESPACE);
		rdfWriter.handleNamespace("rdf", RDF.NAMESPACE);
		rdfWriter.startRDF();
		rdfWriter.handleStatement(new StatementImpl(new URIImpl(RDF.NAMESPACE), RDF.TYPE, OWL.ONTOLOGY));
		rdfWriter.endRDF();
	}
}
