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
package org.eclipse.rdf4j.rio.trig;

import static org.junit.Assert.*;

import java.io.StringReader;

import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.rio.ParserConfig;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.helpers.BasicParserSettings;
import org.eclipse.rdf4j.rio.helpers.ParseErrorCollector;
import org.eclipse.rdf4j.rio.helpers.StatementCollector;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

/**
 * Custom (non-manifest) tests for TriG parser.
 * 
 * @author Peter Ansell
 */
public class TriGParserCustomTest {

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
		vf = SimpleValueFactory.getInstance();
		settingsNoVerifyLangTag = new ParserConfig();
		settingsNoVerifyLangTag.set(BasicParserSettings.VERIFY_LANGUAGE_TAGS, false);
		errors = new ParseErrorCollector();
		parser = Rio.createParser(RDFFormat.TRIG);
		statementCollector = new StatementCollector(new LinkedHashModel());
		parser.setRDFHandler(statementCollector);
	}

	@Test
	public void testSPARQLGraphKeyword()
		throws Exception
	{
		Rio.parse(new StringReader("GRAPH <urn:a> { [] <http://www.example.net/test> \"Foo\" }"), "",
				RDFFormat.TRIG);
	}

	@Test
	public void testTrailingSemicolon()
		throws Exception
	{
		Rio.parse(new StringReader("{<http://example/s> <http://example/p> <http://example/o> ;}"), "",
				RDFFormat.TRIG);
	}

	@Test
	public void testAnonymousGraph1()
		throws Exception
	{
		Rio.parse(new StringReader("PREFIX : <http://example/>\n GRAPH [] { :s :p :o }"), "", RDFFormat.TRIG);
	}

	@Test
	public void testAnonymousGraph2()
		throws Exception
	{
		Rio.parse(new StringReader("PREFIX : <http://example/>\n [] { :s :p :o }"), "", RDFFormat.TRIG);
	}

	@Test
	public void testTurtle()
		throws Exception
	{
		Rio.parse(new StringReader("<urn:a> <urn:b> <urn:c>"), "", RDFFormat.TRIG);
	}

	@Test
	public void testMinimalWhitespace()
		throws Exception
	{
		Rio.parse(
				this.getClass().getResourceAsStream("/testcases/trig/trig-syntax-minimal-whitespace-01.trig"),
				"", RDFFormat.TRIG);
	}

	@Test
	public void testMinimalWhitespaceLine12()
		throws Exception
	{
		Rio.parse(new StringReader("@prefix : <http://example/c/> . {_:s:p :o ._:s:p\"Alice\". _:s:p _:o .}"),
				"", RDFFormat.TRIG);
	}

	@Test
	public void testBadPname02()
		throws Exception
	{
		try {
			Rio.parse(new StringReader("@prefix : <http://example/> . {:a%2 :p :o .}"), "", RDFFormat.TRIG);
			fail("Did not receive expected exception");
		}
		catch (RDFParseException e) {

		}
	}

	@Test
	public void testSupportedSettings()
		throws Exception
	{
		assertEquals(12, Rio.createParser(RDFFormat.TRIG).getSupportedSettings().size());
	}

}
