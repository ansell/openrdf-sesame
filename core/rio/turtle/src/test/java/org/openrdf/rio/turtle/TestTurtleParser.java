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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import org.openrdf.model.Statement;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.helpers.ParseErrorCollector;
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

	@Ignore("TODO: Implement support for UTF8")
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
		assertEquals(2, statementCollector.getStatements().size());

		for (Statement st : statementCollector.getStatements()) {
			System.out.println(st);
		}
	}

	@Ignore("TODO: Implement support for UTF8")
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

	@Ignore("TODO: Implement support for UTF8")
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

}
