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
package org.openrdf.rio.turtle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;

import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.DC;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.helpers.ParseErrorCollector;
import org.openrdf.rio.helpers.ParseErrorLogger;
import org.openrdf.rio.helpers.StatementCollector;

/**
 * @author jeen
 */
public class TestTurtleParser {

	private TurtleParser parser;

	private final ParseErrorCollector errorCollector = new ParseErrorCollector();

	private final StatementCollector statementCollector = new StatementCollector();

	private final String prefixes = "@prefix ex: <http://example.org/ex/> . \n@prefix : <http://example.org/> . \n";

	private final String baseURI = "http://example.org/";

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp()
		throws Exception
	{
		parser = new TurtleParser();
		parser.setParseErrorListener(errorCollector);
		parser.setRDFHandler(statementCollector);
	}

	@Test
	public void testParseDots()
		throws Exception
	{

		String data = prefixes + " ex:foo.bar ex:\\~foo.bar ex:foobar. ";

		parser.parse(new StringReader(data), baseURI);

		assertTrue(errorCollector.getWarnings().isEmpty());
		assertTrue(errorCollector.getErrors().isEmpty());
		assertTrue(errorCollector.getFatalErrors().isEmpty());

		assertFalse(statementCollector.getStatements().isEmpty());
		assertEquals(1, statementCollector.getStatements().size());

		for (Statement st : statementCollector.getStatements()) {
			System.out.println(st);
		}
	}

	@Test
	public void testParseBNodes()
		throws Exception
	{
		String data = prefixes + " [ :p  :o1,:2 ] . ";

		parser.parse(new StringReader(data), baseURI);

		assertTrue(errorCollector.getWarnings().isEmpty());
		assertTrue(errorCollector.getErrors().isEmpty());
		assertTrue(errorCollector.getFatalErrors().isEmpty());

		assertFalse(statementCollector.getStatements().isEmpty());
		assertEquals(2, statementCollector.getStatements().size());

		for (Statement st : statementCollector.getStatements()) {
			System.out.println(st);
		}
	}

	@Test
	public void testParsePrefixUTF8()
		throws Exception
	{
		URL url = new URL(
				"http://www.w3.org/2013/TurtleTests/prefix_with_PN_CHARS_BASE_character_boundaries.ttl");

		parser.parse(url.openStream(), baseURI);

		assertTrue(errorCollector.getWarnings().isEmpty());
		assertTrue(errorCollector.getErrors().isEmpty());
		assertTrue(errorCollector.getFatalErrors().isEmpty());

		assertFalse(statementCollector.getStatements().isEmpty());
		assertEquals(1, statementCollector.getStatements().size());

		for (Statement st : statementCollector.getStatements()) {
			System.out.println(st);
		}
	}

	@Test
	public void testW3C1()
		throws Exception
	{
		URL url = new URL("http://www.w3.org/2013/TurtleTests/localName_with_assigned_nfc_PN_CHARS_BASE_character_boundaries.ttl");
		URL nturl = new URL("http://www.w3.org/2013/TurtleTests/localName_with_assigned_nfc_PN_CHARS_BASE_character_boundaries.nt");

		parser.parse(url.openStream(), baseURI);

		assertTrue(errorCollector.getWarnings().isEmpty());
		assertTrue(errorCollector.getErrors().isEmpty());
		assertTrue(errorCollector.getFatalErrors().isEmpty());

		assertFalse(statementCollector.getStatements().isEmpty());
		assertEquals(1, statementCollector.getStatements().size());
		for (Statement st : statementCollector.getStatements()) {
			System.out.println(st);
		}
	}
	
	@Test
	public void testParseTurtleLiteralUTF8()
		throws Exception
	{
		URL url = new URL("http://www.w3.org/2013/TurtleTests/LITERAL2_WITH_UTF8_boundaries.ttl");

		parser.parse(url.openStream(), baseURI);

		assertTrue(errorCollector.getWarnings().isEmpty());
		assertTrue(errorCollector.getErrors().isEmpty());
		assertTrue(errorCollector.getFatalErrors().isEmpty());

		assertFalse(statementCollector.getStatements().isEmpty());
		assertEquals(1, statementCollector.getStatements().size());

		for (Statement st : statementCollector.getStatements()) {
			System.out.println(st);
		}
	}
	
	@Test
	public void testParseTurtleLiteralCarriageReturn()
		throws Exception
	{
		URL url = new URL("http://www.w3.org/2013/TurtleTests/literal_with_CARRIAGE_RETURN.ttl");

		parser.parse(url.openStream(), baseURI);

		assertTrue(errorCollector.getWarnings().isEmpty());
		assertTrue(errorCollector.getErrors().isEmpty());
		assertTrue(errorCollector.getFatalErrors().isEmpty());

		assertFalse(statementCollector.getStatements().isEmpty());
		assertEquals(1, statementCollector.getStatements().size());

		for (Statement st : statementCollector.getStatements()) {
			System.out.println(st);
		}
	}

	@Test
	public void testLineNumberReporting()
		throws Exception
	{

		InputStream in = this.getClass().getResourceAsStream("/test-newlines.ttl");
		try {
			parser.parse(in, baseURI);
			fail("expected to fail parsing input file");
		}
		catch (RDFParseException e) {
			// expected
			assertFalse(errorCollector.getFatalErrors().isEmpty());
			final String error = errorCollector.getFatalErrors().get(0);
			// expected to fail at line 9.
			assertTrue(error.contains("(9,"));
		}
	}
	
	@Test
	public void testParseBooleanLiteralComma() throws Exception{
		String data = "<urn:a> <urn:b> true, false .";
		Reader r = new StringReader(data);
		
		try {
			parser.parse(r, baseURI);
			assertTrue(statementCollector.getStatements().size() == 2);
		}
		catch (RDFParseException e) {
			fail("parse error on correct data: " + e.getMessage());
		}
	}

	@Test
	public void testParseBooleanLiteralWhitespaceComma() throws Exception{
		String data = "<urn:a> <urn:b> true , false .";
		Reader r = new StringReader(data);
		
		try {
			parser.parse(r, baseURI);
			assertTrue(statementCollector.getStatements().size() == 2);
		}
		catch (RDFParseException e) {
			fail("parse error on correct data: " + e.getMessage());
		}
	}
	
	@Test
	public void testParseBooleanLiteralSemicolumn() throws Exception{
		String data = "<urn:a> <urn:b> true; <urn:c> false .";
		Reader r = new StringReader(data);
		
		try {
			parser.parse(r, baseURI);
			assertTrue(statementCollector.getStatements().size() == 2);
		}
		catch (RDFParseException e) {
			fail("parse error on correct data: " + e.getMessage());
		}
	}
	
	@Test
	public void testParseBooleanLiteralWhitespaceSemicolumn() throws Exception{
		String data = "<urn:a> <urn:b> true ; <urn:c> false .";
		Reader r = new StringReader(data);
		
		try {
			parser.parse(r, baseURI);
			assertTrue(statementCollector.getStatements().size() == 2);
		}
		catch (RDFParseException e) {
			fail("parse error on correct data: " + e.getMessage());
		}
	}
	
	@Test
	public void testParseNTriplesLiteralUTF8()
		throws Exception
	{
		URL url = new URL("http://www.w3.org/2013/TurtleTests/LITERAL_WITH_UTF8_boundaries.nt");

		parser.parse(url.openStream(), baseURI);

		assertTrue(errorCollector.getWarnings().isEmpty());
		assertTrue(errorCollector.getErrors().isEmpty());
		assertTrue(errorCollector.getFatalErrors().isEmpty());

		assertFalse(statementCollector.getStatements().isEmpty());
		assertEquals(1, statementCollector.getStatements().size());

		for (Statement st : statementCollector.getStatements()) {
			System.out.println(st);
		}
	}

	@Test
	public void rdfXmlLoadedFromInsideAJarResolvesRelativeUris() throws Exception {
		URL zipfileUrl = TestTurtleParser.class.getResource("sample-with-turtle-data.zip");

		assertNotNull("The sample-with-turtle-data.zip file must be present for this test", zipfileUrl);

		String url = "jar:" + zipfileUrl + "!/index.ttl";

		RDFParser parser = new TurtleParser();

		StatementCollector sc = new StatementCollector();
		parser.setRDFHandler(sc);

		InputStream in = new URL(url).openStream();
		parser.parse(in, url);
		in.close();

		Collection<Statement> stmts = sc.getStatements();

		assertThat(stmts, Matchers.<Statement>iterableWithSize(2));

		Iterator<Statement> iter = stmts.iterator();

		Statement stmt1 = iter.next(),
				stmt2 = iter.next();

		assertEquals(new URIImpl("http://www.example.com/#"), stmt1.getSubject());
		assertEquals(new URIImpl("http://www.example.com/ns/#document-about"), stmt1.getPredicate());

		Resource res = (Resource) stmt1.getObject();

		String resourceUrl = res.stringValue();

		assertThat(resourceUrl, CoreMatchers.startsWith("jar:" + zipfileUrl + "!"));

		URL javaUrl = new URL(resourceUrl);
		assertEquals("jar", javaUrl.getProtocol());

		InputStream uc = javaUrl.openStream();
		assertEquals("The resource stream should be empty", -1, uc.read());
		uc.close();

		assertEquals(res, stmt2.getSubject());
		assertEquals(DC.TITLE, stmt2.getPredicate());
		assertEquals(new LiteralImpl("Empty File"), stmt2.getObject());
	}
}
