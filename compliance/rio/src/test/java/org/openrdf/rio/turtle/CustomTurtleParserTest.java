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

import static org.junit.Assert.*;

import java.io.StringReader;
import java.io.StringWriter;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import org.openrdf.model.Literal;
import org.openrdf.model.Model;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.util.ModelUtil;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.rio.ParserConfig;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.openrdf.rio.helpers.BasicParserSettings;
import org.openrdf.rio.helpers.ParseErrorCollector;
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

		ModelUtil.equals(statementCollector.getStatements(), Rio.parse(
				this.getClass().getResourceAsStream("/testcases/turtle/turtle-long-literals-test.nt"), "",
				RDFFormat.NTRIPLES));
	}
}
