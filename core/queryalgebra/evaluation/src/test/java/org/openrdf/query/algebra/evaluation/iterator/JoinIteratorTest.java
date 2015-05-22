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

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import info.aduna.iteration.Iterations;

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

/**
 *
 * @author MJAHale
 */
public class JoinIteratorTest {
	private final ValueFactory vf = ValueFactoryImpl.getInstance();
	private final EvaluationStrategy evaluator = new EvaluationStrategyImpl(null, null);

	/**
	 * Tests joins between two different BindingSetAssignments with the same BindingSets but ordered differently.
	 */
	@Test
	public void testBindingSetAssignmentJoin() throws QueryEvaluationException {
		BindingSetAssignment left = new BindingSetAssignment();
		{
			List<BindingSet> leftb = new ArrayList<BindingSet>();
			for(int i=0; i<5; i++)
			{
				QueryBindingSet b = new QueryBindingSet();
				b.addBinding("a", vf.createLiteral(i));
				leftb.add(b);
			}
			left.setBindingSets(leftb);
		}

		BindingSetAssignment right = new BindingSetAssignment();
		{
			List<BindingSet> rightb = new ArrayList<BindingSet>();
			for(int i=5; i>=0; i--)
			{
				QueryBindingSet b = new QueryBindingSet();
				b.addBinding("a", vf.createLiteral(i));
				rightb.add(b);
			}
			right.setBindingSets(rightb);
		}

		JoinIterator iter = new JoinIterator(evaluator, new Join(left, right), EmptyBindingSet.getInstance());
		List<BindingSet> lr = Iterations.asList(iter);
		iter = new JoinIterator(evaluator, new Join(right, left), EmptyBindingSet.getInstance());
		List<BindingSet> rl = Iterations.asList(iter);

		assertEquals(lr, rl);
	}
}
