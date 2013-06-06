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
package org.openrdf.query.algebra.evaluation.function.datetime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
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
public class TzTest {

	private Tz tz;

	private ValueFactory f = new ValueFactoryImpl();

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp()
		throws Exception
	{
		tz = new Tz();
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

			Literal result = tz.evaluate(f, f.createLiteral("2011-01-10T14:45:13.815-05:00", XMLSchema.DATETIME));

			assertNotNull(result);
			assertEquals(XMLSchema.STRING, result.getDatatype());

			assertEquals("-05:00", result.getLabel());

		}
		catch (ValueExprEvaluationException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testEvaluate2() {
		try {

			Literal result = tz.evaluate(f, f.createLiteral("2011-01-10T14:45:13.815Z", XMLSchema.DATETIME));

			assertNotNull(result);
			assertEquals(XMLSchema.STRING, result.getDatatype());

			assertEquals("Z", result.getLabel());

		}
		catch (ValueExprEvaluationException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	
	@Test
	public void testEvaluate3() {
		try {

			Literal result = tz.evaluate(f, f.createLiteral("2011-01-10T14:45:13.815", XMLSchema.DATETIME));

			assertNotNull(result);
			assertEquals(XMLSchema.STRING, result.getDatatype());

			assertEquals("", result.getLabel());

		}
		catch (ValueExprEvaluationException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
}
