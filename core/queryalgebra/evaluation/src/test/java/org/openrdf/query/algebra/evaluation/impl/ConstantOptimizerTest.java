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
