/*
 * Copyright 3 Round Stones Inc. (c) 2012.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.function.string;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.EmptyIteration;

import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.Regex;
import org.openrdf.query.algebra.ValueExpr;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.algebra.evaluation.TripleSource;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;
import org.openrdf.query.algebra.evaluation.impl.EvaluationStrategyImpl;
import org.openrdf.query.impl.EmptyBindingSet;

/**
 * @author james
 */
public class RegexTest {
	private ValueFactory vf = new ValueFactoryImpl();

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
		EvaluationStrategyImpl strategy = new EvaluationStrategyImpl(new TripleSource() {
			public ValueFactory getValueFactory() {
				return vf;
			}
			
			public CloseableIteration<? extends Statement, QueryEvaluationException> getStatements(Resource subj,
					URI pred, Value obj, Resource... contexts)
				throws QueryEvaluationException
			{
				return new EmptyIteration<Statement, QueryEvaluationException>();
			}
		});
		ValueExpr expr = new Var("expr", args[0]);
		ValueExpr pattern = new Var("pattern", args[1]);
		ValueExpr flags = null;
		if (args.length > 2) {
			flags = new Var("flags", args[2]);
		}
		return (Literal)strategy.evaluate(new Regex(expr, pattern, flags), new EmptyBindingSet());
	}

}
