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
import java.util.Set;

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
import org.openrdf.query.algebra.evaluation.impl.SimpleEvaluationStrategy;
import org.openrdf.query.impl.EmptyBindingSet;

/**
 *
 * @author MJAHale
 */
public class JoinIteratorTest {
	private final ValueFactory vf = ValueFactoryImpl.getInstance();
	private final EvaluationStrategy evaluator = new SimpleEvaluationStrategy(null, null);

	/**
	 * Tests joins between two different BindingSetAssignments with the same BindingSets but ordered differently.
	 */
	@Test
	public void testBindingSetAssignmentJoin() throws QueryEvaluationException {
		testBindingSetAssignmentJoin(5, 5, EmptyBindingSet.getInstance());

		{
			QueryBindingSet b = new QueryBindingSet();
			b.addBinding("a", vf.createLiteral(2));
			testBindingSetAssignmentJoin(1, 5, b);
		}

		{
			QueryBindingSet b = new QueryBindingSet();
			b.addBinding("x", vf.createLiteral("foo"));
			testBindingSetAssignmentJoin(5, 5, b);
		}
	}

	private void testBindingSetAssignmentJoin(int expectedSize, int n, BindingSet bindings) throws QueryEvaluationException {
		BindingSetAssignment left = new BindingSetAssignment();
		{
			List<BindingSet> leftb = new ArrayList<BindingSet>();
			for(int i=0; i<n; i++)
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
			for(int i=n; i>=0; i--)
			{
				QueryBindingSet b = new QueryBindingSet();
				b.addBinding("a", vf.createLiteral(i));
				rightb.add(b);
			}
			right.setBindingSets(rightb);
		}

		JoinIterator lrIter = new JoinIterator(evaluator, new Join(left, right), bindings);
		Set<BindingSet> lr = Iterations.asSet(lrIter);
		assertEquals(expectedSize, lr.size());

		JoinIterator rlIter = new JoinIterator(evaluator, new Join(right, left), bindings);
		Set<BindingSet> rl = Iterations.asSet(rlIter);
		assertEquals(expectedSize, rl.size());

		assertEquals(lr, rl);

		// check bindings
		for(BindingSet b : lr) {
			for(String name : bindings.getBindingNames()) {
				assertEquals(bindings.getValue(name), b.getValue(name));
			}
		}
	}
}
