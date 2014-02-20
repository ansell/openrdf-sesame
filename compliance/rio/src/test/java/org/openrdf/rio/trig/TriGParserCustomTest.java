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
package org.openrdf.rio.trig;

import static org.junit.Assert.*;

import java.io.StringReader;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.rio.ParserConfig;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.openrdf.rio.helpers.BasicParserSettings;
import org.openrdf.rio.helpers.ParseErrorCollector;
import org.openrdf.rio.helpers.StatementCollector;

/**
 * Custom (non-manifest) tests for TriG parser.
 * 
 * @author Peter Ansell
 */
public class TriGParserCustomTest {

	@Rule
	public Timeout timeout = new Timeout(10000);

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
		Rio.parse(new StringReader("PREFIX : <http://example/>\n GRAPH [] { :s :p :o }"), "",
				RDFFormat.TRIG);
	}

	@Test
	public void testAnonymousGraph2()
		throws Exception
	{
		Rio.parse(new StringReader("PREFIX : <http://example/>\n [] { :s :p :o }"), "",
				RDFFormat.TRIG);
	}
	
	@Test
	public void testSupportedSettings()
		throws Exception
	{
		assertEquals(12, Rio.createParser(RDFFormat.TRIG).getSupportedSettings().size());
	}

}
