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
package org.openrdf.query.algebra.evaluation.iterator;

import java.util.Arrays;

import org.junit.Test;

import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.BindingSetAssignment;
import org.openrdf.query.algebra.Join;
import org.openrdf.query.algebra.evaluation.EvaluationStrategy;
import org.openrdf.query.algebra.evaluation.QueryBindingSet;
import org.openrdf.query.algebra.evaluation.impl.EvaluationStrategyImpl;
import org.openrdf.query.impl.EmptyBindingSet;

import static org.junit.Assert.*;

/**
 *
 * @author MJAHale
 */
public class HashJoinIterationTest {
	private final ValueFactory vf = ValueFactoryImpl.getInstance();
	private final EvaluationStrategy evaluator = new EvaluationStrategyImpl(null, null);

	@Test
	public void testCartesianJoin() throws QueryEvaluationException {
		BindingSetAssignment left = new BindingSetAssignment();
		{
			QueryBindingSet leftb = new QueryBindingSet();
			leftb.addBinding("a", vf.createLiteral("1"));
			left.setBindingSets(Arrays.<BindingSet>asList(leftb));
		}

		BindingSetAssignment right = new BindingSetAssignment();
		{
			QueryBindingSet rightb = new QueryBindingSet();
			rightb.addBinding("b", vf.createLiteral("2"));
			right.setBindingSets(Arrays.<BindingSet>asList(rightb));
		}

		HashJoinIteration iter = new HashJoinIteration(evaluator, new Join(left, right), EmptyBindingSet.getInstance());
		BindingSet actual = iter.next();
		
		assertEquals("1", actual.getValue("a").stringValue());
		assertEquals("2", actual.getValue("b").stringValue());
	}

	@Test
	public void testInnerJoin() throws QueryEvaluationException {
		BindingSetAssignment left = new BindingSetAssignment();
		{
			QueryBindingSet leftb = new QueryBindingSet();
			leftb.addBinding("a", vf.createLiteral("1"));
			leftb.addBinding("i", vf.createLiteral("x"));
			left.setBindingSets(Arrays.<BindingSet>asList(leftb));
		}

		BindingSetAssignment right = new BindingSetAssignment();
		{
			QueryBindingSet rightb = new QueryBindingSet();
			rightb.addBinding("b", vf.createLiteral("2"));
			rightb.addBinding("i", vf.createLiteral("x"));
			right.setBindingSets(Arrays.<BindingSet>asList(rightb));
		}

		HashJoinIteration iter = new HashJoinIteration(evaluator, new Join(left, right), EmptyBindingSet.getInstance());
		BindingSet actual = iter.next();
		
		assertEquals("1", actual.getValue("a").stringValue());
		assertEquals("2", actual.getValue("b").stringValue());
		assertEquals("x", actual.getValue("i").stringValue());
	}

	@Test
	public void testLeftJoin() throws QueryEvaluationException {
		BindingSetAssignment left = new BindingSetAssignment();
		{
			QueryBindingSet leftb = new QueryBindingSet();
			leftb.addBinding("a", vf.createLiteral("1"));
			leftb.addBinding("i", vf.createLiteral("x"));
			left.setBindingSets(Arrays.<BindingSet>asList(leftb));
		}

		BindingSetAssignment right = new BindingSetAssignment();
		{
			QueryBindingSet rightb = new QueryBindingSet();
			rightb.addBinding("b", vf.createLiteral("2"));
			rightb.addBinding("i", vf.createLiteral("y"));
			right.setBindingSets(Arrays.<BindingSet>asList(rightb));
		}

		HashJoinIteration iter = new HashJoinIteration(evaluator, new Join(left, right), EmptyBindingSet.getInstance(), true);
		BindingSet actual = iter.next();
		
		assertEquals("1", actual.getValue("a").stringValue());
		assertEquals("x", actual.getValue("i").stringValue());
		assertFalse(actual.hasBinding("b"));
	}
}
