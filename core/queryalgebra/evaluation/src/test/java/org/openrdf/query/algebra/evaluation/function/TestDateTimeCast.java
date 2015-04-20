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
package org.openrdf.query.algebra.evaluation.function;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.openrdf.model.Literal;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.datatypes.XMLDatatypeUtil;
import org.openrdf.model.impl.SimpleValueFactory;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;

/**
 * @author jeen
 */
public class TestDateTimeCast {

	private DateTimeCast dtCast;

	private ValueFactory f = new SimpleValueFactory();
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp()
		throws Exception
	{
		dtCast = new DateTimeCast();
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
	public void testCastPlainLiteral() {
		Literal plainLit = f.createLiteral("1999-09-09T00:00:01");
		try {
			Literal result = dtCast.evaluate(f, plainLit);
			assertNotNull(result);
			assertEquals(XMLSchema.DATETIME, result.getDatatype());
		}
		catch (ValueExprEvaluationException e) {
			fail(e.getMessage());
		}
	}
	
	@Test 
	public void testCastDateLiteral() {
		Literal dateLit = f.createLiteral("1999-09-09", XMLSchema.DATE);
		try {
			Literal result = dtCast.evaluate(f, dateLit);
			assertNotNull(result);
			assertEquals(XMLSchema.DATETIME, result.getDatatype());

		}
		catch (ValueExprEvaluationException e) {
			fail(e.getMessage());
		}
	}
	
	@Test 
	public void testCastDateTimeLiteral() {
		String lexVal = "2000-01-01T00:00:00";
		Literal dtLit = f.createLiteral(XMLDatatypeUtil.parseCalendar(lexVal));
		try {
			Literal result = dtCast.evaluate(f, dtLit);
			assertNotNull(result);
			assertEquals(XMLSchema.DATETIME, result.getDatatype());
			assertFalse(result.getLanguage().isPresent());
			assertEquals(lexVal, result.getLabel());
		}
		catch (ValueExprEvaluationException e) {
			fail(e.getMessage());
		}
	}

}
