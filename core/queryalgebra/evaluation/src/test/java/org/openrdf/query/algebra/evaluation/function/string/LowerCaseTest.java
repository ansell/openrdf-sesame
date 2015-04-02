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

import org.openrdf.model.Literal;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.SimpleValueFactory;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;

/**
 * @author jeen
 */
public class LowerCaseTest {

	private LowerCase lcaseFunc;

	private ValueFactory f = new SimpleValueFactory();

	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp()
		throws Exception
	{
		lcaseFunc = new LowerCase();
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
		
		Literal pattern = f.createLiteral("foobar");
		
		try {
			Literal result = lcaseFunc.evaluate(f, pattern);
			
			assertTrue(result.getLabel().equals("foobar"));
		}
		catch (ValueExprEvaluationException e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testEvaluate2() {
		
		Literal pattern = f.createLiteral("FooBar");
		
		try {
			Literal result = lcaseFunc.evaluate(f, pattern);
			
			assertTrue(result.getLabel().equals("foobar"));
		}
		catch (ValueExprEvaluationException e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testEvaluate3() {
		
		Literal pattern = f.createLiteral("FooBar");
		Literal startIndex = f.createLiteral(4);
		
		try {
			lcaseFunc.evaluate(f, pattern, startIndex);
			fail("illegal number of parameters");
		}
		catch (ValueExprEvaluationException e) {
			// do nothing, expected
		}
	}

}
