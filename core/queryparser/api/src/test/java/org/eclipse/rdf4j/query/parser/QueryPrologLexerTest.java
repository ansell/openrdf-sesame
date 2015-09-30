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
package org.eclipse.rdf4j.query.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.eclipse.rdf4j.query.parser.QueryPrologLexer;
import org.eclipse.rdf4j.query.parser.QueryPrologLexer.Token;
import org.eclipse.rdf4j.query.parser.QueryPrologLexer.TokenType;
import org.junit.Test;

/**
 * @author jeen
 */
public class QueryPrologLexerTest {

	@Test
	public void testLexEmptyString() {
		List<Token> tokens = QueryPrologLexer.lex("");
		assertNotNull(tokens);
		assertEquals(0, tokens.size());

	}

	@Test
	public void testFinalTokenEmptyString() {
		try {
			Token t = QueryPrologLexer.getRestOfQueryToken("");
		}
		catch (Exception e) {
			fail("lexer should not throw exception on malformed input");
		}
	}

	@Test
	public void testLexNoProlog1() {
		List<Token> tokens = QueryPrologLexer.lex("SELECT * WHERE {?s ?p ?o} ");
		assertNotNull(tokens);
		assertEquals(1, tokens.size());

		Token t = tokens.get(0);
		assertTrue(t.getType().equals(TokenType.REST_OF_QUERY));
	}

	@Test
	public void testFinalTokenNoProlog1() {
		Token t = QueryPrologLexer.getRestOfQueryToken("SELECT * WHERE {?s ?p ?o} ");
		assertNotNull(t);
		assertTrue(t.getType().equals(TokenType.REST_OF_QUERY));
	}

	@Test
	public void testLexWithComment() {
		List<Token> tokens = QueryPrologLexer.lex("# COMMENT \n SELECT * WHERE {?s ?p ?o} ");
		assertNotNull(tokens);
		assertEquals(3, tokens.size());
		assertEquals(" COMMENT \n", tokens.get(1).s);
		
		Token t = tokens.get(tokens.size() - 1);
		assertTrue(t.getType().equals(TokenType.REST_OF_QUERY));
		assertEquals("SELECT * WHERE {?s ?p ?o} ", t.s);
	}
	
	@Test
	public void testLexWithComment_WindowsLinebreak() {
		List<Token> tokens = QueryPrologLexer.lex("# COMMENT \r\n SELECT * WHERE {?s ?p ?o} ");
		assertNotNull(tokens);

		Token t = tokens.get(tokens.size() - 1);
		assertTrue(t.getType().equals(TokenType.REST_OF_QUERY));
		assertEquals("SELECT * WHERE {?s ?p ?o} ", t.s);
	}
	
	@Test
	public void testLexWithComment_NoSpaceBeforeQuery() {
		List<Token> tokens = QueryPrologLexer.lex("# COMMENT \nSELECT * WHERE {?s ?p ?o} ");
		assertNotNull(tokens);

		Token t = tokens.get(tokens.size() - 1);
		assertTrue(t.getType().equals(TokenType.REST_OF_QUERY));
		assertEquals("SELECT * WHERE {?s ?p ?o} ", t.s);
	}

	@Test
	public void testFinalTokenWithComment() {
		Token t = QueryPrologLexer.getRestOfQueryToken("# COMMENT \n  SELECT * WHERE {?s ?p ?o} ");
		assertNotNull(t);
		assertTrue(t.getType().equals(TokenType.REST_OF_QUERY));
		assertEquals("SELECT * WHERE {?s ?p ?o} ", t.s);
	}

	@Test
	public void testFinalTokenWithMultilineComment() {
		Token t = QueryPrologLexer.getRestOfQueryToken("# COMMENT \n # COMMENT (continued) \n SELECT * WHERE {?s ?p ?o} ");
		assertNotNull(t);
		assertTrue(t.getType().equals(TokenType.REST_OF_QUERY));
		assertEquals("SELECT * WHERE {?s ?p ?o} ", t.s);
	}
	
	@Test
	public void testLexWithBaseAndComment() {
		List<Token> tokens = QueryPrologLexer.lex("BASE <foobar> # COMMENT \n SELECT * WHERE {?s ?p ?o} ");
		assertNotNull(tokens);
		
		Token t = tokens.get(tokens.size() - 1);
		assertTrue(t.getType().equals(TokenType.REST_OF_QUERY));
		assertEquals("SELECT * WHERE {?s ?p ?o} ", t.s);
	}

	@Test
	public void testFinalTokenWithBaseAndComment() {
		Token t = QueryPrologLexer.getRestOfQueryToken("BASE <foobar> # COMMENT \n SELECT * WHERE {?s ?p ?o} ");
		assertNotNull(t);
		assertTrue(t.getType().equals(TokenType.REST_OF_QUERY));
	}

	@Test
	public void testLexSyntaxError() {
		// all that is guaranteed in queries with syntax errors is that the lexer
		// returns. there are no guarantees that the
		// last token is the rest of the query in this case. Any syntax errors in
		// the query are to be picked up by subsequent processing.

		try {
			List<Token> tokens = QueryPrologLexer.lex("BASE <foobar # missing closing bracket \n SELECT * WHERE {?s ?p ?o} ");
		}
		catch (Exception e) {
			fail("malformed query should not make lexer fail");

		}
	}

	@Test
	public void testFinalTokenSyntaxError() {
		// all that is guaranteed in queries with syntax errors is that the lexer
		// returns. there are no guarantees that the
		// token returned is the rest of the query in this case. Any syntax errors
		// in the query are to be picked up by subsequent processing.

		try {
			Token t = QueryPrologLexer.getRestOfQueryToken("BASE <foobar # missing closing bracket \n SELECT * WHERE {?s ?p ?o} ");
		}
		catch (Exception e) {
			fail("malformed query should not make lexer fail");
		}
	}
}
