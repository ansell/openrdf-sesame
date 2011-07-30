/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2011.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.function.datetime;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.openrdf.model.Literal;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;

/**
 * @author jeen
 */
public class TimezoneTest {

	private Timezone timezone;

	private ValueFactory f = new ValueFactoryImpl();

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp()
		throws Exception
	{
		timezone = new Timezone();
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
	public void testEvaluate1() {
		try {

			Literal result = timezone.evaluate(f,
					f.createLiteral("2011-01-10T14:45:13.815-05:00", XMLSchema.DATETIME));

			assertNotNull(result);
			assertEquals(XMLSchema.DAYTIMEDURATION, result.getDatatype());

			assertEquals("-PT5H", result.getLabel());

		}
		catch (ValueExprEvaluationException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test
	public void testEvaluate2() {
		try {

			Literal result = timezone.evaluate(f,
					f.createLiteral("2011-01-10T14:45:13.815Z", XMLSchema.DATETIME));

			assertNotNull(result);
			assertEquals(XMLSchema.DAYTIMEDURATION, result.getDatatype());

			assertEquals("PT0S", result.getLabel());

		}
		catch (ValueExprEvaluationException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test
	public void testEvaluate3() {
		try {

			timezone.evaluate(f, f.createLiteral("2011-01-10T14:45:13.815", XMLSchema.DATETIME));

			fail("should have resulted in a type error");

		}
		catch (ValueExprEvaluationException e) {
			// do nothing, expected
		}
	}

}
