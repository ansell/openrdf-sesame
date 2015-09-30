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
package org.openrdf.sail.lucene;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.openrdf.model.vocabulary.RDF.TYPE;
import static org.openrdf.sail.lucene.LuceneSailSchema.LUCENE_QUERY;
import static org.openrdf.sail.lucene.LuceneSailSchema.MATCHES;
import static org.openrdf.sail.lucene.LuceneSailSchema.QUERY;
import static org.openrdf.sail.lucene.LuceneSailSchema.SCORE;
import static org.openrdf.sail.lucene.LuceneSailSchema.SNIPPET;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.openrdf.model.Literal;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.evaluation.QueryBindingSet;
import org.openrdf.query.parser.ParsedQuery;
import org.openrdf.query.parser.serql.SeRQLParser;
import org.openrdf.sail.SailException;

public class QuerySpecBuilderTest {

	private QuerySpecBuilder interpreter;

	private SeRQLParser parser;

	@Before
	public void setUp()
		throws Exception
	{
		interpreter = new QuerySpecBuilder(true);
		parser = new SeRQLParser();
	}

	@Test
	public void testQueryInterpretation()
		throws Exception
	{
		StringBuilder buffer = new StringBuilder();
		buffer.append("SELECT Subject, Score, Snippet ");
		buffer.append("FROM {Subject} <" + MATCHES + "> {} ");
		buffer.append("<" + TYPE + "> {<" + LUCENE_QUERY + ">}; ");
		buffer.append("<" + QUERY + "> {\"my Lucene query\"}; ");
		buffer.append("<" + SCORE + "> {Score}; ");
		buffer.append("<" + SNIPPET + "> {Snippet} ");
		ParsedQuery query = parser.parseQuery(buffer.toString(), null);
		TupleExpr tupleExpr = query.getTupleExpr();
		System.out.print(buffer.toString());
		Collection<SearchQueryEvaluator> queries = process(interpreter, tupleExpr);
		assertEquals(1, queries.size());

		QuerySpec querySpec = (QuerySpec)queries.iterator().next();
		assertEquals("Subject", querySpec.getMatchesPattern().getSubjectVar().getName());
		assertEquals("my Lucene query",
				((Literal)querySpec.getQueryPattern().getObjectVar().getValue()).getLabel());
		assertEquals("Score", querySpec.getScorePattern().getObjectVar().getName());
		assertEquals("Snippet", querySpec.getSnippetPattern().getObjectVar().getName());
		assertEquals(LUCENE_QUERY, querySpec.getTypePattern().getObjectVar().getValue());
		assertEquals("my Lucene query", querySpec.getQueryString());
		assertNull(querySpec.getSubject());
	}

	@Test
	public void testMultipleQueriesInterpretation()
		throws Exception
	{
		StringBuilder buffer = new StringBuilder();
		buffer.append("SELECT sub1, score1, snippet1, sub2, score2, snippet2, x, p1, p2 ");
		buffer.append("FROM {sub1} <" + MATCHES + "> {} ");
		buffer.append("<" + TYPE + "> {<" + LUCENE_QUERY + ">}; ");
		buffer.append("<" + QUERY + "> {\"my Lucene query\"}; ");
		buffer.append("<" + SCORE + "> {score1}; ");
		buffer.append("<" + SNIPPET + "> {snippet1}, ");
		buffer.append("{sub2} <" + MATCHES + "> {} ");
		buffer.append("<" + TYPE + "> {<" + LUCENE_QUERY + ">}; ");
		buffer.append("<" + QUERY + "> {\"second lucene query\"}; ");
		buffer.append("<" + SCORE + "> {score2}; ");
		buffer.append("<" + SNIPPET + "> {snippet2}, ");
		// and connect them both via any X in between, just as salt to make the
		// parser do something
		buffer.append("{sub1} p1 {x}, {x} p2 {sub2} ");
		ParsedQuery query = parser.parseQuery(buffer.toString(), null);
		TupleExpr tupleExpr = query.getTupleExpr();

		Collection<SearchQueryEvaluator> queries = process(interpreter, tupleExpr);
		assertEquals(2, queries.size());
		Iterator<SearchQueryEvaluator> i = queries.iterator();
		boolean matched1 = false;
		boolean matched2 = false;
		while (i.hasNext()) {
			QuerySpec querySpec = (QuerySpec)i.next();
			if ("sub1".equals(querySpec.getMatchesVariableName())) {
				// Matched the first
				assertEquals("sub1", querySpec.getMatchesPattern().getSubjectVar().getName());
				assertEquals("my Lucene query",
						((Literal)querySpec.getQueryPattern().getObjectVar().getValue()).getLabel());
				assertEquals("score1", querySpec.getScorePattern().getObjectVar().getName());
				assertEquals("snippet1", querySpec.getSnippetPattern().getObjectVar().getName());
				assertEquals(LUCENE_QUERY, querySpec.getTypePattern().getObjectVar().getValue());
				assertEquals("my Lucene query", querySpec.getQueryString());
				assertNull(querySpec.getSubject());
				matched1 = true;
			}
			else if ("sub2".equals(querySpec.getMatchesVariableName())) {
				// and the second
				assertEquals("sub2", querySpec.getMatchesPattern().getSubjectVar().getName());
				assertEquals("second lucene query",
						((Literal)querySpec.getQueryPattern().getObjectVar().getValue()).getLabel());
				assertEquals("score2", querySpec.getScorePattern().getObjectVar().getName());
				assertEquals("snippet2", querySpec.getSnippetPattern().getObjectVar().getName());
				assertEquals(LUCENE_QUERY, querySpec.getTypePattern().getObjectVar().getValue());
				assertEquals("second lucene query", querySpec.getQueryString());
				assertNull(querySpec.getSubject());
				matched2 = true;
			}
			else
				fail("Found unexpected query spec: " + querySpec.toString());
		}
		if (!matched1)
			fail("did not find query patter sub1");
		if (!matched2)
			fail("did not find query patter sub2");
	}

	/**
	 * Incomplete queries should fail, if configured
	 *
	 * @throws Exception
	 */
	@Test
	public void testIncompleteFail()
		throws Exception
	{
		// default works
		String queryString = "SELECT sub1, score1, snippet1 FROM " + "{sub1} <" + MATCHES + "> {} " + "<"
				+ TYPE + "> {<" + LUCENE_QUERY + ">}; " + "<" + QUERY + "> {\"my Lucene query\"}; " + "<" + SCORE
				+ "> {score1}; " + "<" + SNIPPET + "> {snippet1}";
		checkQuery(queryString);

		// minimal works
		queryString = "SELECT sub1 FROM " + "{sub1} <" + MATCHES + "> {} " + "<" + TYPE + "> {<" + LUCENE_QUERY
				+ ">}; " + "<" + QUERY + "> {\"my Lucene query\"} ";
		checkQuery(queryString);

		// matches missing
		queryString = "SELECT sub1, score1, snippet1 FROM "
				// + "{sub1} <" + MATCHES + "> {} "
				+ "<" + TYPE + "> {<" + LUCENE_QUERY + ">}; " + "<" + QUERY + "> {\"my Lucene query\"}; " + "<"
				+ SCORE + "> {score1}; " + "<" + SNIPPET + "> {snippet1}";
		try {
			checkQuery(queryString);
			fail("invalid query ignored");
		}
		catch (Exception x) {
			// excellent
		}

		// type missing
		queryString = "SELECT sub1, score1, snippet1 FROM " + "{sub1} <" + MATCHES + "> {} "
				// +"<" + TYPE + "> {<" + LUCENE_QUERY + ">}; "
				+ "<" + QUERY + "> {\"my Lucene query\"}; " + "<" + SCORE + "> {score1}; " + "<" + SNIPPET
				+ "> {snippet1}";
		try {
			checkQuery(queryString);
			// excellent
		}
		catch (Exception x) {
			fail("missing type is ok, should not throw an exception");
		}

		// query missing
		queryString = "SELECT sub1, score1, snippet1 FROM " + "{sub1} <" + MATCHES + "> {} " + "<" + TYPE
				+ "> {<" + LUCENE_QUERY + ">}; "
				// +"<" + QUERY + "> {\"my Lucene query\"}; "
				+ "<" + SCORE + "> {score1}; " + "<" + SNIPPET + "> {snippet1}";
		try {
			checkQuery(queryString);
			fail("invalid missing query not detected");
		}
		catch (Exception x) {
			// excellent
		}
	}

	/**
	 * Checks if the querystring contains exactly one lucene query throws
	 * exceptions if not or if the query is incomplete
	 *
	 * @param queryString
	 */
	private void checkQuery(String queryString)
		throws Exception
	{
		ParsedQuery query = parser.parseQuery(queryString, null);
		TupleExpr tupleExpr = query.getTupleExpr();
		Collection<SearchQueryEvaluator> queries = process(interpreter, tupleExpr);
		assertEquals("expect one query", 1, queries.size());
	}

	private Collection<SearchQueryEvaluator> process(SearchQueryInterpreter interpreter, TupleExpr tupleExpr)
		throws SailException
	{
		List<SearchQueryEvaluator> queries = new ArrayList<SearchQueryEvaluator>();
		interpreter.process(tupleExpr, new QueryBindingSet(), queries);
		return queries;
	}
}
