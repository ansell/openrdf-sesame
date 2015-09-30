/*******************************************************************************
 * Copyright (c) 2015, Eclipse Foundation, Inc. and its licensors.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * - Neither the name of the Eclipse Foundation, Inc. nor the names of its
 *   contributors may be used to endorse or promote products derived from this
 *   software without specific prior written permission. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package org.eclipse.rdf4j.query.algebra.evaluation.iterator;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.rdf4j.common.iteration.Iterations;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.ValueFactoryImpl;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.algebra.BindingSetAssignment;
import org.eclipse.rdf4j.query.algebra.Join;
import org.eclipse.rdf4j.query.algebra.evaluation.EvaluationStrategy;
import org.eclipse.rdf4j.query.algebra.evaluation.QueryBindingSet;
import org.eclipse.rdf4j.query.algebra.evaluation.impl.SimpleEvaluationStrategy;
import org.eclipse.rdf4j.query.algebra.evaluation.iterator.JoinIterator;
import org.eclipse.rdf4j.query.impl.EmptyBindingSet;
import org.junit.Test;

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
