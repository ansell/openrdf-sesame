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
package org.openrdf.rio.turtle;

import static org.junit.Assert.*;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collections;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import org.openrdf.model.Literal;
import org.openrdf.model.Model;
import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.impl.NamespaceImpl;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.util.Models;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.SKOS;
import org.openrdf.rio.ParserConfig;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.openrdf.rio.helpers.BasicParserSettings;
import org.openrdf.rio.helpers.ParseErrorCollector;
import org.openrdf.rio.helpers.ParseErrorLogger;
import org.openrdf.rio.helpers.StatementCollector;

/**
 * Custom tests for Turtle Parser
 * 
 * @author Peter Ansell
 */
public class CustomTurtleParserTest {

	@Rule
	public Timeout timeout = new Timeout(1000000);

	private ValueFactory vf;

	private ParserConfig settingsNoVerifyLangTag;

	private ParseErrorCollector errors;

	private RDFParser parser;

	private StatementCollector statementCollector;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp()
		throws Exception
	{
		vf = ValueFactoryImpl.getInstance();
		settingsNoVerifyLangTag = new ParserConfig();
		settingsNoVerifyLangTag.set(BasicParserSettings.VERIFY_LANGUAGE_TAGS, false);
		errors = new ParseErrorCollector();
		parser = Rio.createParser(RDFFormat.TURTLE);
		statementCollector = new StatementCollector(new LinkedHashModel());
		parser.setRDFHandler(statementCollector);
	}

	@Test
	public void testSES1887NoLangTagFailure()
		throws Exception
	{
		try {
			Rio.parse(new StringReader("<urn:a> <http://www.example.net/test> \"Foo\"@."), "", RDFFormat.TURTLE);
			fail("Did not receive an exception");
		}
		catch (RDFParseException e) {
			assertTrue(e.getMessage().contains("Expected a letter, found '.'"));
		}
	}

	@Test
	public void testSES1887NoLangTagFailure2()
		throws Exception
	{
		try {
			// NOTE: Bad things may happen when VERIFY_LANGUAGE_TAGS is turned off
			// on a file of this structure
			Rio.parse(new StringReader("<urn:a> <http://www.example.net/test> \"Foo\"@."), "", RDFFormat.TURTLE,
					settingsNoVerifyLangTag, vf, errors);
			fail("Did not receive an exception");
		}
		catch (RDFParseException e) {
			assertTrue(e.getMessage().contains("Unexpected end of file"));
		}
	}

	@Test
	public void testSES1887Whitespace()
		throws Exception
	{
		Model model = Rio.parse(new StringReader("<urn:a> <http://www.example.net/test> \"Foo\"@fr-FR ."), "",
				RDFFormat.TURTLE);

		assertEquals(1, model.size());
		assertTrue(model.contains(null, null, vf.createLiteral("Foo", "fr-FR")));
	}

	@Test
	public void testSES1887Period()
		throws Exception
	{
		Model model = Rio.parse(new StringReader("<urn:a> <http://www.example.net/test> \"Foo\"@fr-FR."), "",
				RDFFormat.TURTLE);

		assertEquals(1, model.size());
		assertTrue(model.contains(null, null, vf.createLiteral("Foo", "fr-FR")));
	}

	@Test
	public void testSES1887Semicolon()
		throws Exception
	{
		Model model = Rio.parse(new StringReader(
				"<urn:a> <http://www.example.net/test> \"Foo\"@fr-FR;<http://other.example.org>\"Blah\"@en-AU."),
				"", RDFFormat.TURTLE);

		assertEquals(2, model.size());
		assertTrue(model.contains(null, null, vf.createLiteral("Foo", "fr-FR")));
		assertTrue(model.contains(null, null, vf.createLiteral("Blah", "en-AU")));
	}

	@Test
	public void testSES1887Comma()
		throws Exception
	{
		Model model = Rio.parse(new StringReader(
				"<urn:a> <http://www.example.net/test> \"Foo\"@fr-FR,\"Blah\"@en-AU."), "", RDFFormat.TURTLE);

		assertEquals(2, model.size());
		assertTrue(model.contains(null, null, vf.createLiteral("Foo", "fr-FR")));
		assertTrue(model.contains(null, null, vf.createLiteral("Blah", "en-AU")));
	}

	@Test
	public void testSES1887CloseParentheses()
		throws Exception
	{
		Model model = Rio.parse(new StringReader("<urn:a> <http://www.example.net/test> (\"Foo\"@fr-FR)."), "",
				RDFFormat.TURTLE);

		assertEquals(3, model.size());
		assertTrue(model.contains(null, null, vf.createLiteral("Foo", "fr-FR")));
	}

	@Test
	public void testSES1887CloseSquareBracket()
		throws Exception
	{
		Model model = Rio.parse(new StringReader("[<http://www.example.net/test> \"Foo\"@fr-FR]."), "",
				RDFFormat.TURTLE);

		assertEquals(1, model.size());
		assertTrue(model.contains(null, null, vf.createLiteral("Foo", "fr-FR")));
	}

	@Test
	public void testLiteralWithNewlines()
		throws Exception
	{
		String namespace = "http://www.foo.com/bar#";
		String okLiteralString = "Literal \n without \n new line at the beginning. \n ";
		String errLiteralString = "\n Literal \n with \n new line at the beginning. \n ";

		URI mySubject = vf.createURI(namespace, "Subject");
		URI myPredicate = vf.createURI(namespace, "Predicate");
		Literal myOkObject = vf.createLiteral(okLiteralString);
		Literal myErrObject = vf.createLiteral(errLiteralString);

		StringWriter out = new StringWriter();
		Model model = new LinkedHashModel();
		model.add(mySubject, myPredicate, myOkObject);
		model.add(mySubject, myPredicate, myErrObject);
		Rio.write(model, out, RDFFormat.TURTLE);

		String str = out.toString();

		System.err.println(str);

		assertTrue("okLiteralString not found", str.contains(okLiteralString));
		assertTrue("errLiteralString not found", str.contains(errLiteralString));
	}

	@Test
	public void testSupportedSettings()
		throws Exception
	{
		assertEquals(12, parser.getSupportedSettings().size());
	}

	@Test
	public void testSES1988BlankNodePeriodEOF()
		throws Exception
	{
		Model model = Rio.parse(new StringReader("<urn:a> <urn:b> _:blank."), "", RDFFormat.TURTLE);

		assertEquals(1, model.size());
	}

	@Test
	public void testSES1988BlankNodePeriodSpace()
		throws Exception
	{
		Model model = Rio.parse(new StringReader("<urn:a> <urn:b> _:blank. "), "", RDFFormat.TURTLE);

		assertEquals(1, model.size());
	}

	@Test
	public void testSES1988BlankNodePeriodTab()
		throws Exception
	{
		Model model = Rio.parse(new StringReader("<urn:a> <urn:b> _:blank.\t"), "", RDFFormat.TURTLE);

		assertEquals(1, model.size());
	}

	@Test
	public void testSES1988BlankNodePeriodNewLine()
		throws Exception
	{
		Model model = Rio.parse(new StringReader("<urn:a> <urn:b> _:blank.\n"), "", RDFFormat.TURTLE);

		assertEquals(1, model.size());
	}

	@Test
	public void testSES1988BlankNodePeriodCarriageReturn()
		throws Exception
	{
		Model model = Rio.parse(new StringReader("<urn:a> <urn:b> _:blank.\r"), "", RDFFormat.TURTLE);

		assertEquals(1, model.size());
	}

	@Test
	public void testSES1988BlankNodePeriodURI()
		throws Exception
	{
		Model model = Rio.parse(new StringReader("<urn:a> <urn:b> _:blank.<urn:c> <urn:d> <urn:e>."), "",
				RDFFormat.TURTLE);

		assertEquals(2, model.size());
	}

	@Test
	public void testSES1988BlankNodePeriodBNode()
		throws Exception
	{
		Model model = Rio.parse(new StringReader("<urn:a> <urn:b> _:blank._:blank <urn:d> <urn:e>."), "",
				RDFFormat.TURTLE);

		assertEquals(2, model.size());
	}

	@Test
	public void testSES2013BlankNodeSemiColonBNodeSpaceA()
		throws Exception
	{
		Model model = Rio.parse(new StringReader("<urn:a> a _:c2; a <urn:b> ."), "", RDFFormat.TURTLE);

		assertEquals(2, model.size());
		assertTrue(model.contains(vf.createURI("urn:a"), RDF.TYPE, vf.createURI("urn:b")));
	}

	@Test
	public void testSES2013BlankNodeSemiColonBNodeA()
		throws Exception
	{
		Model model = Rio.parse(new StringReader("<urn:a> a _:c2;a <urn:b> ."), "", RDFFormat.TURTLE);

		assertEquals(2, model.size());
		assertTrue(model.contains(vf.createURI("urn:a"), RDF.TYPE, vf.createURI("urn:b")));
	}

	@Test
	public void testSES2013BlankNodeSemiColonBNodeSpaceURI()
		throws Exception
	{
		Model model = Rio.parse(new StringReader("<urn:a> a _:c2; <urn:b> <urn:c> ."), "", RDFFormat.TURTLE);

		assertEquals(2, model.size());
		assertTrue(model.contains(vf.createURI("urn:a"), vf.createURI("urn:b"), vf.createURI("urn:c")));
	}

	@Test
	public void testSES2013BlankNodeSemiColonBNodeURI()
		throws Exception
	{
		Model model = Rio.parse(new StringReader("<urn:a> a _:c2;<urn:b> <urn:c> ."), "", RDFFormat.TURTLE);

		assertEquals(2, model.size());
		assertTrue(model.contains(vf.createURI("urn:a"), vf.createURI("urn:b"), vf.createURI("urn:c")));
	}

	@Test
	public void testSES2019ParseLongLiterals()
		throws Exception
	{
		parser.parse(this.getClass().getResourceAsStream("/testcases/turtle/turtle-long-literals-test.ttl"), "");

		assertTrue(errors.getWarnings().isEmpty());
		assertTrue(errors.getErrors().isEmpty());
		assertTrue(errors.getFatalErrors().isEmpty());

		assertFalse(statementCollector.getStatements().isEmpty());
		assertEquals(5, statementCollector.getStatements().size());

		Models.isomorphic(statementCollector.getStatements(), Rio.parse(
				this.getClass().getResourceAsStream("/testcases/turtle/turtle-long-literals-test.nt"), "",
				RDFFormat.NTRIPLES));
	}

	@Test
	public void testSES2086PeriodEndingLocalNamesFailure1()
		throws Exception
	{
		try {
			Rio.parse(new StringReader(
					"@prefix : <http://example.org> .\n <urn:a> <http://www.example.net/test> :test. ."), "",
					RDFFormat.TURTLE);
			fail("Did not receive an exception");
		}
		catch (RDFParseException e) {
			System.out.println(e.getMessage());
			assertTrue(e.getMessage().contains("Object for statement missing"));
		}
	}

	@Test
	public void testSES2086PeriodEndingLocalNamesFailure2()
		throws Exception
	{
		try {
			Rio.parse(
					new StringReader(
							"@prefix ns: <http://example.org/data/> . ns:uriWithDot. a ns:Product ; ns:title \"An example subject ending with a dot.\" . "),
					"", RDFFormat.TURTLE);
			fail("Did not receive an exception");
		}
		catch (RDFParseException e) {
			System.out.println(e.getMessage());
			assertTrue(e.getMessage().contains(
					"Illegal predicate value: \"\"^^<http://www.w3.org/2001/XMLSchema#integer>"));
		}
	}

	@Test
	public void testSES2086PeriodEndingLocalNamesFailure3()
		throws Exception
	{
		try {
			Rio.parse(
					new StringReader(
							"@prefix ns: <http://example.org/data/> . ns:1 a ns:Product ; ns:affects ns:4 , ns:16 , ns:uriWithDot. ; ns:title \"An example entity with uriWithDot as an object\" . "),
					"", RDFFormat.TURTLE);
			fail("Did not receive an exception");
		}
		catch (RDFParseException e) {
			System.out.println(e.getMessage());
			assertTrue(e.getMessage().contains("Expected an RDF value here, found ';'"));
		}
	}

	@Test
	public void testSES2086PeriodEndingLocalNamesFailure4()
		throws Exception
	{
		try {
			Rio.parse(
					new StringReader(
							"@prefix ns: <http://example.org/data/> . ns:1 a ns:uriWithDot. ; ns:title \"An example entity with uriWithDot as an object\" . "),
					"", RDFFormat.TURTLE);
			fail("Did not receive an exception");
		}
		catch (RDFParseException e) {
			System.out.println(e.getMessage());
			assertTrue(e.getMessage().contains("Expected an RDF value here, found ';'"));
		}
	}

	@Test
	public void testSES2165LiteralSpaceDatatypeNewline()
		throws Exception
	{
		Model model = Rio.parse(new StringReader("<urn:a> <urn:b> \"testliteral\"^^\n<urn:datatype> ."), "",
				RDFFormat.TURTLE);

		assertEquals(1, model.size());
		assertTrue(model.contains(vf.createURI("urn:a"), vf.createURI("urn:b"),
				vf.createLiteral("testliteral", vf.createURI("urn:datatype"))));
	}

	@Test
	public void testSES2165LiteralSpaceDatatypeTab()
		throws Exception
	{
		Model model = Rio.parse(new StringReader("<urn:a> <urn:b> \"testliteral\"^^\t<urn:datatype> ."), "",
				RDFFormat.TURTLE);

		assertEquals(1, model.size());
		assertTrue(model.contains(vf.createURI("urn:a"), vf.createURI("urn:b"),
				vf.createLiteral("testliteral", vf.createURI("urn:datatype"))));
	}

	@Test
	public void testSES2165LiteralSpaceDatatypeCarriageReturn()
		throws Exception
	{
		Model model = Rio.parse(new StringReader("<urn:a> <urn:b> \"testliteral\"^^\r<urn:datatype> ."), "",
				RDFFormat.TURTLE);

		assertEquals(1, model.size());
		assertTrue(model.contains(vf.createURI("urn:a"), vf.createURI("urn:b"),
				vf.createLiteral("testliteral", vf.createURI("urn:datatype"))));
	}

	@Test
	public void testSES2165LiteralSpaceDatatypeSpace()
		throws Exception
	{
		Model model = Rio.parse(new StringReader("<urn:a> <urn:b> \"testliteral\"^^ <urn:datatype> ."), "",
				RDFFormat.TURTLE);

		assertEquals(1, model.size());
		assertTrue(model.contains(vf.createURI("urn:a"), vf.createURI("urn:b"),
				vf.createLiteral("testliteral", vf.createURI("urn:datatype"))));
	}
	
	@Test
	public void testSES2165LiteralSpaceDatatypeComment()
		throws Exception
	{
		Model model = Rio.parse(new StringReader("<urn:a> <urn:b> \"testliteral\"^^#comment\n<urn:datatype> ."), "",
				RDFFormat.TURTLE);

		assertEquals(1, model.size());
		assertTrue(model.contains(vf.createURI("urn:a"), vf.createURI("urn:b"),
				vf.createLiteral("testliteral", vf.createURI("urn:datatype"))));
	}

	@Test
	public void testParsingDefaultNamespaces() throws Exception {
		Model model = Rio.parse(new StringReader("<urn:a> skos:broader <urn:b>."), "",
		                        RDFFormat.TURTLE);

		assertEquals(1, model.size());
		assertTrue(model.contains(vf.createURI("urn:a"), SKOS.BROADER, vf.createURI("urn:b")));
	}

	@Test
	public void testParsingNamespacesWithOption() throws Exception {
		ParserConfig aConfig = new ParserConfig();

		aConfig.set(BasicParserSettings.NAMESPACES, Collections.<Namespace>singleton(new NamespaceImpl("foo", SKOS.NAMESPACE)));

		Model model = Rio.parse(new StringReader("<urn:a> foo:broader <urn:b>."), "", RDFFormat.TURTLE, aConfig, vf, new ParseErrorLogger());

		assertEquals(1, model.size());
		assertTrue(model.contains(vf.createURI("urn:a"), SKOS.BROADER, vf.createURI("urn:b")));
	}

	@Test
	public void testParsingNamespacesWithOverride() throws Exception {
		ParserConfig aConfig = new ParserConfig();

		aConfig.set(BasicParserSettings.NAMESPACES, Collections.<Namespace>singleton(new NamespaceImpl("foo", SKOS.NAMESPACE)));

		Model model = Rio.parse(new StringReader("@prefix skos : <urn:not_skos:> ." +
		                                         "<urn:a> skos:broader <urn:b>."), "",
		                        RDFFormat.TURTLE, aConfig, vf, new ParseErrorLogger());

		assertEquals(1, model.size());
		assertTrue(model.contains(vf.createURI("urn:a"), vf.createURI("urn:not_skos:broader"), vf.createURI("urn:b")));
	}
}
