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
import info.aduna.iteration.Iterations;

import java.util.Set;

import org.junit.Test;
import org.openrdf.OpenRDFException;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.algebra.QueryRoot;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.evaluation.EvaluationStrategy;
import org.openrdf.query.algebra.evaluation.QueryBindingSet;
import org.openrdf.query.impl.EmptyBindingSet;
import org.openrdf.query.parser.ParsedQuery;
import org.openrdf.query.parser.QueryParserUtil;


/**
 */
public class ConstantOptimizerTest {
	@Test
	public void testAndOptimization() throws OpenRDFException {
		String query = "prefix ex: <ex:>"
				+ "select ?a ?b ?c\n"
				+ "where {\n"
				+ " bind((?a && ?b) as ?c) \n"
				+ "}";

		QueryBindingSet bs = new QueryBindingSet();
		bs.addBinding("a", ValueFactoryImpl.getInstance().createLiteral(true));
		bs.addBinding("b", ValueFactoryImpl.getInstance().createLiteral(true));

		testOptimizer(query, bs);
	}

	@Test
	public void testBoundOptimization() throws OpenRDFException {
		String query = "prefix ex: <ex:>"
				+ "select ?a ?c\n"
				+ "where {\n"
				+ " bind(bound(?a) as ?c) \n"
				+ "}";

		QueryBindingSet bs = new QueryBindingSet();
		bs.addBinding("a", ValueFactoryImpl.getInstance().createLiteral("foo"));

		testOptimizer(query, bs);
	}

	@Test
	public void testFunctionOptimization() throws OpenRDFException {
		String query = "prefix ex: <ex:>"
				+ "construct {\n"
				+ "ex:a rdfs:label ?a .\n"
				+ "ex:b rdfs:label ?b .\n"
				+ "ex:c rdfs:label ?c .\n"
				+ "} where {\n"
				+ " bind(concat(?a, ?b) as ?c) \n"
				+ "}";

		QueryBindingSet bs = new QueryBindingSet();
		bs.addBinding("a", ValueFactoryImpl.getInstance().createLiteral("foo"));
		bs.addBinding("b", ValueFactoryImpl.getInstance().createLiteral("bah"));

		testOptimizer(query, bs);
	}

	private void testOptimizer(String query, BindingSet bs)
		throws OpenRDFException
	{
		ParsedQuery pq = QueryParserUtil.parseQuery(QueryLanguage.SPARQL, query, null);
		EvaluationStrategy strategy = new SimpleEvaluationStrategy(new EmptyTripleSource(), null);
		TupleExpr opt = optimize(pq.getTupleExpr().clone(), bs, strategy);
		Set<BindingSet> expected = Iterations.asSet(strategy.evaluate(pq.getTupleExpr(), bs));
		Set<BindingSet> actual = Iterations.asSet(strategy.evaluate(opt, EmptyBindingSet.getInstance()));
		assertEquals(expected, actual);
	}

	private TupleExpr optimize(TupleExpr expr, BindingSet bs, EvaluationStrategy strategy)
	{
		QueryRoot optRoot = new QueryRoot(expr);
		new BindingAssigner().optimize(optRoot, null, bs);
		new ConstantOptimizer(strategy).optimize(optRoot, null, bs);
		return optRoot;
	}
}
