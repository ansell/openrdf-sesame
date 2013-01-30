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
package org.openrdf.query.algebra.evaluation.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import org.openrdf.model.Literal;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.XMLSchema;


/**
 *
 * @author Jeen Broekstra
 */
public class QueryEvaluationUtilTest {

	private ValueFactory f = ValueFactoryImpl.getInstance();
	
	@Test
	public void testCompatibleArguments() throws Exception {
		
		Literal arg1simple = f.createLiteral("abc");
		Literal arg2simple = f.createLiteral("b");
		
		Literal arg1en = f.createLiteral("abc", "en");
		Literal arg2en = f.createLiteral("b", "en");
		
		Literal arg1cy = f.createLiteral("abc", "cy");
		Literal arg2cy = f.createLiteral("b", "cy");
		
		Literal arg1string = f.createLiteral("abc", XMLSchema.STRING);
		Literal arg2string = f.createLiteral("b", XMLSchema.STRING);
		
		Literal arg1int = f.createLiteral(10);
		Literal arg2int = f.createLiteral(1);
		
		assertTrue(QueryEvaluationUtil.compatibleArguments(arg1simple, arg2simple));
		assertFalse(QueryEvaluationUtil.compatibleArguments(arg1simple, arg2en));
		assertFalse(QueryEvaluationUtil.compatibleArguments(arg1simple, arg2cy));
		assertTrue(QueryEvaluationUtil.compatibleArguments(arg1simple, arg2string));
		assertFalse(QueryEvaluationUtil.compatibleArguments(arg1simple, arg2int));
		
		assertTrue(QueryEvaluationUtil.compatibleArguments(arg1en, arg2simple));
		assertTrue(QueryEvaluationUtil.compatibleArguments(arg1en, arg2en));
		assertFalse(QueryEvaluationUtil.compatibleArguments(arg2en, arg2cy));
		assertFalse(QueryEvaluationUtil.compatibleArguments(arg1en, arg2cy));
		assertTrue(QueryEvaluationUtil.compatibleArguments(arg1en, arg2string));
		assertFalse(QueryEvaluationUtil.compatibleArguments(arg1en, arg2int));
		
		assertTrue(QueryEvaluationUtil.compatibleArguments(arg1cy, arg2simple));
		assertFalse(QueryEvaluationUtil.compatibleArguments(arg1cy, arg2en));
		assertFalse(QueryEvaluationUtil.compatibleArguments(arg2cy, arg2en));
		assertTrue(QueryEvaluationUtil.compatibleArguments(arg1cy, arg2cy));
		assertTrue(QueryEvaluationUtil.compatibleArguments(arg1cy, arg2string));
		assertFalse(QueryEvaluationUtil.compatibleArguments(arg1cy, arg2int));
		
		assertTrue(QueryEvaluationUtil.compatibleArguments(arg1string, arg2simple));
		assertFalse(QueryEvaluationUtil.compatibleArguments(arg1string, arg2en));
		assertFalse(QueryEvaluationUtil.compatibleArguments(arg1string, arg2cy));
		assertTrue(QueryEvaluationUtil.compatibleArguments(arg1string, arg2string));
		assertFalse(QueryEvaluationUtil.compatibleArguments(arg1string, arg2int));
		
		assertFalse(QueryEvaluationUtil.compatibleArguments(arg1int, arg2simple));
		assertFalse(QueryEvaluationUtil.compatibleArguments(arg1int, arg2en));
		assertFalse(QueryEvaluationUtil.compatibleArguments(arg1int, arg2cy));
		assertFalse(QueryEvaluationUtil.compatibleArguments(arg1int, arg2string));
		assertFalse(QueryEvaluationUtil.compatibleArguments(arg1int, arg2int));
		
	}
	
}
