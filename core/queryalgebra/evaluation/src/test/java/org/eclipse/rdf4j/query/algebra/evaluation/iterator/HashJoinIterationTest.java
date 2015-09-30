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

import java.util.Arrays;

import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.ValueFactoryImpl;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.algebra.BindingSetAssignment;
import org.eclipse.rdf4j.query.algebra.Join;
import org.eclipse.rdf4j.query.algebra.evaluation.EvaluationStrategy;
import org.eclipse.rdf4j.query.algebra.evaluation.QueryBindingSet;
import org.eclipse.rdf4j.query.algebra.evaluation.impl.SimpleEvaluationStrategy;
import org.eclipse.rdf4j.query.algebra.evaluation.iterator.HashJoinIteration;
import org.eclipse.rdf4j.query.impl.EmptyBindingSet;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 * @author MJAHale
 */
public class HashJoinIterationTest {
	private final ValueFactory vf = ValueFactoryImpl.getInstance();
	private final EvaluationStrategy evaluator = new SimpleEvaluationStrategy(null, null);

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

		HashJoinIteration iter = new HashJoinIteration(evaluator, left, right, EmptyBindingSet.getInstance(), false);
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

		HashJoinIteration iter = new HashJoinIteration(evaluator, left, right, EmptyBindingSet.getInstance(), false);
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

		HashJoinIteration iter = new HashJoinIteration(evaluator, left, right, EmptyBindingSet.getInstance(), true);
		BindingSet actual = iter.next();
		
		assertEquals("1", actual.getValue("a").stringValue());
		assertEquals("x", actual.getValue("i").stringValue());
		assertFalse(actual.hasBinding("b"));
	}
}
