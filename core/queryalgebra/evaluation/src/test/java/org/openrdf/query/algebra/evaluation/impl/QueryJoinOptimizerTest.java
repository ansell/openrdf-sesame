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
package org.openrdf.query.algebra.evaluation.impl;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import org.openrdf.OpenRDFException;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.UnsupportedQueryLanguageException;
import org.openrdf.query.algebra.QueryModelNode;
import org.openrdf.query.algebra.QueryRoot;
import org.openrdf.query.parser.ParsedQuery;
import org.openrdf.query.parser.QueryParserUtil;


/**
 * Tests to monitor QueryJoinOptimizer behaviour.
 * @author Mark
 */
public class QueryJoinOptimizerTest {
	@Test
	public void testBindingSetAssignmentOptimization() throws OpenRDFException {
		String query = "prefix ex: <ex:>"
				+ "select ?s ?p ?o ?x where {"
				+ " ex:s1 ex:pred ?v. "
				+ " ex:s2 ex:pred 'bah'. {"
				+ "  ?s ?p ?o. "
				+ "  optional {"
				+ "   values ?x {ex:a ex:b ex:c ex:d ex:e ex:f ex:g}. "
				+ "  }"
				+ " }"
				+ "}";
		// optimal order should be existence check of first statement
		// followed by left join evaluation
		String expectedQuery = "prefix ex: <ex:>"
				+ "select ?s ?p ?o ?x where {"
				+ " ex:s2 ex:pred 'bah'. {"
				+ "  ex:s1 ex:pred ?v. {"
				+ "   ?s ?p ?o. "
				+ "   optional {"
				+ "    values ?x {ex:a ex:b ex:c ex:d ex:e ex:f ex:g}. "
				+ "   }"
				+ "  }"
				+ " }"
				+ "}";

		testOptimizer(expectedQuery, query);
	}

	@Test(expected=AssertionError.class)
	public void testContextOptimization()
		throws OpenRDFException
	{
		String query = "prefix ex: <ex:>"
				+ "select ?x ?y ?z ?g ?p ?o where {"
				+ " graph ?g {"
				+ "  ex:s ?sp ?so. "
				+ "  ?ps ex:p ?po. "
				+ "  ?os ?op 'ex:o'. "
				+ " }"
				+ " ?x ?y ?z. "
				+ "}";
		// optimal order should be ?g graph first
		// as it is all statements about a subject in all graphs
		// rather than all subjects in the default graph:
		// card(?g) << card(?x)
		// and assuming named graph has same access cost as default graph
		String expectedQuery = "prefix ex: <ex:>"
				+ "select ?x ?y ?z ?g ?p ?o where {"
				+ " graph ?g {"
				+ "  ex:s ?sp ?so. "
				+ "  ?ps ex:p ?po. "
				+ "  ?os ?op 'ex:o'. "
				+ " }"
				+ " ?x ?y ?z. "
				+ "}";

		testOptimizer(expectedQuery, query);
	}

	private void testOptimizer(String expectedQuery, String actualQuery)
		throws MalformedQueryException, UnsupportedQueryLanguageException
	{
		ParsedQuery pq = QueryParserUtil.parseQuery(QueryLanguage.SPARQL, actualQuery, null);
		QueryJoinOptimizer opt = new QueryJoinOptimizer();
		QueryRoot optRoot = new QueryRoot(pq.getTupleExpr());
		opt.optimize(optRoot, null, null);

		ParsedQuery expectedParsedQuery = QueryParserUtil.parseQuery(QueryLanguage.SPARQL, expectedQuery, null);
		QueryRoot root = new QueryRoot(expectedParsedQuery.getTupleExpr());
		assertQueryModelTrees(root, optRoot);
	}

	private void assertQueryModelTrees(QueryModelNode expected, QueryModelNode actual) {
		assertEquals(expected, actual);
	}
}
