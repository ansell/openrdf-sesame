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
package org.openrdf.query.algebra.evaluation.function.string;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.EmptyIteration;

import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.IRI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.SimpleValueFactory;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.Regex;
import org.openrdf.query.algebra.ValueExpr;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.algebra.evaluation.TripleSource;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;
import org.openrdf.query.algebra.evaluation.federation.FederatedServiceResolverImpl;
import org.openrdf.query.algebra.evaluation.impl.SimpleEvaluationStrategy;
import org.openrdf.query.impl.EmptyBindingSet;

/**
 * @author james
 */
public class RegexTest {
	private ValueFactory vf = new SimpleValueFactory();
	private FederatedServiceResolverImpl serviceResolver;

	@Before
	public void setUp() {
		serviceResolver = new FederatedServiceResolverImpl();
	}

	@After
	public void tearDown() {
		serviceResolver.shutDown();
	}

	@Test
	public void testEvaluate1() throws QueryEvaluationException {

		Literal expr = vf.createLiteral("foobar");
		Literal pattern = vf.createLiteral("foobar");
		
		try {
			Literal result = evaluate(expr, pattern);
			
			assertTrue(result.booleanValue());
		}
		catch (ValueExprEvaluationException e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testEvaluate2() throws QueryEvaluationException {

		Literal expr = vf.createLiteral("foobar");
		Literal pattern = vf.createLiteral("FooBar");
		Literal flags = vf.createLiteral("i");
		
		try {
			Literal result = evaluate(expr, pattern, flags);
			
			assertTrue(result.booleanValue());
		}
		catch (ValueExprEvaluationException e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testEvaluate3() throws QueryEvaluationException {
		
		Literal pattern = vf.createLiteral("FooBar");
		Literal startIndex = vf.createLiteral(4);
		
		try {
			evaluate(pattern, startIndex, startIndex, startIndex);
			fail("illegal number of parameters");
		}
		catch (ValueExprEvaluationException e) {
			// do nothing, expected
		}
	}
	
	@Test
	public void testEvaluate4() throws QueryEvaluationException {

		Literal expr = vf.createLiteral("foobar", "en");
		Literal pattern = vf.createLiteral("FooBar");
		Literal flags = vf.createLiteral("i");
		
		try {
			Literal result = evaluate(expr, pattern, flags);
			
			assertTrue(result.booleanValue());
		}
		catch (ValueExprEvaluationException e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testEvaluate5() throws QueryEvaluationException {

		Literal expr = vf.createLiteral("foobar", XMLSchema.STRING);
		Literal pattern = vf.createLiteral("FooBar");
		Literal flags = vf.createLiteral("i");
		
		try {
			Literal result = evaluate(expr, pattern, flags);
			
			assertTrue(result.booleanValue());
		}
		catch (ValueExprEvaluationException e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testEvaluate6() throws QueryEvaluationException {

		Literal expr = vf.createLiteral("foobar", XMLSchema.TOKEN);
		Literal pattern = vf.createLiteral("FooBar");
		Literal flags = vf.createLiteral("i");
		
		try {
			evaluate(expr, pattern, flags);
			fail("Regex should not process typed literals");
		}
		catch (ValueExprEvaluationException e) {
			// do nothing, expected
		}
	}

	private Literal evaluate(Value... args) throws ValueExprEvaluationException, QueryEvaluationException {
		SimpleEvaluationStrategy strategy = new SimpleEvaluationStrategy(new TripleSource() {
			public ValueFactory getValueFactory() {
				return vf;
			}
			
			public CloseableIteration<? extends Statement, QueryEvaluationException> getStatements(Resource subj,
					IRI pred, Value obj, Resource... contexts)
				throws QueryEvaluationException
			{
				return new EmptyIteration<Statement, QueryEvaluationException>();
			}
		}, serviceResolver);
		ValueExpr expr = new Var("expr", args[0]);
		ValueExpr pattern = new Var("pattern", args[1]);
		ValueExpr flags = null;
		if (args.length > 2) {
			flags = new Var("flags", args[2]);
		}
		return (Literal)strategy.evaluate(new Regex(expr, pattern, flags), new EmptyBindingSet());
	}

}
