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
