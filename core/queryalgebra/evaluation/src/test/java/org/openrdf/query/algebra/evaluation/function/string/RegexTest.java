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
package org.openrdf.query.algebra.evaluation.function.string;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.model.Literal;
import org.openrdf.model.IRI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.SimpleValueFactory;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.Regex;
import org.openrdf.query.algebra.ValueExpr;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;
import org.openrdf.query.algebra.evaluation.federation.FederatedServiceResolverImpl;
import org.openrdf.query.algebra.evaluation.impl.EmptyTripleSource;
import org.openrdf.query.algebra.evaluation.impl.SimpleEvaluationStrategy;
import org.openrdf.query.impl.EmptyBindingSet;

/**
 * @author james
 */
public class RegexTest {
	private ValueFactory vf = SimpleValueFactory.getInstance();
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
		SimpleEvaluationStrategy strategy = new SimpleEvaluationStrategy(new EmptyTripleSource(vf), serviceResolver);
		ValueExpr expr = new Var("expr", args[0]);
		ValueExpr pattern = new Var("pattern", args[1]);
		ValueExpr flags = null;
		if (args.length > 2) {
			flags = new Var("flags", args[2]);
		}
		return (Literal)strategy.evaluate(new Regex(expr, pattern, flags), new EmptyBindingSet());
	}

}
