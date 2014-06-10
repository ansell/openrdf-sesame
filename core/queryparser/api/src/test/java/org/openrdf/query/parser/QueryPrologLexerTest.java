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
package org.openrdf.query.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import org.openrdf.query.parser.QueryPrologLexer.Token;
import org.openrdf.query.parser.QueryPrologLexer.TokenType;

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
	public void testLexNoProlog1() {
		List<Token> tokens = QueryPrologLexer.lex("SELECT * WHERE {?s ?p ?o} ");
		assertNotNull(tokens);
		assertEquals(1, tokens.size());

		Token t = tokens.get(0);
		assertTrue(t.getType().equals(TokenType.REST_OF_QUERY));
	}
	
	@Test
	public void testLexWithComment() {
		List<Token> tokens = QueryPrologLexer.lex("# COMMENT \n SELECT * WHERE {?s ?p ?o} ");
		assertNotNull(tokens);

		Token t = tokens.get(tokens.size() - 1);
		assertTrue(t.getType().equals(TokenType.REST_OF_QUERY));
	}
	
	
	@Test
	public void testLexWithBaseAndComment() {
		List<Token> tokens = QueryPrologLexer.lex("BASE <foobar> # COMMENT \n SELECT * WHERE {?s ?p ?o} ");
		assertNotNull(tokens);

		for (Token t: tokens) {
			System.out.println(t);
		}
		Token t = tokens.get(tokens.size() - 1);
		assertTrue(t.getType().equals(TokenType.REST_OF_QUERY));
	}
	
	@Test
	public void testLexSyntaxError() {
		List<Token> tokens = QueryPrologLexer.lex("BASE <foobar # missing closing bracket \n SELECT * WHERE {?s ?p ?o} ");
		assertNotNull(tokens);
		// all that is guaranteed in queries with syntax errors is that the lexer returns. there are no guarantees that the
		// last token is the rest of the query in this case. Any syntax errors in the query are to be picked up by subsequent processing.
	}
}
