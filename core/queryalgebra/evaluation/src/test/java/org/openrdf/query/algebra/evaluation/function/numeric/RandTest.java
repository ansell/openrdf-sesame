/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2011.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.function.numeric;


import java.math.BigDecimal;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.openrdf.model.Literal;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;


/**
 *
 * @author jeen
 */
public class RandTest {

	private Rand rand;
	
	private ValueFactory f = new ValueFactoryImpl();
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp()
		throws Exception
	{
		rand = new Rand();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown()
		throws Exception
	{
	}

	@Test
	public void testEvaluate() {
		try {
			Literal random = rand.evaluate(f);
			
			Assert.assertNotNull(random);
			Assert.assertEquals(XMLSchema.DOUBLE, random.getDatatype());
			
			double randomValue = random.doubleValue();
			
			Assert.assertTrue(randomValue >= 0.0d);
			Assert.assertTrue(randomValue < 1.0d);
		}
		catch (ValueExprEvaluationException e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}

}
