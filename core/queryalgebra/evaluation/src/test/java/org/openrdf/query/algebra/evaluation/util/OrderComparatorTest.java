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
package org.openrdf.query.algebra.evaluation.util;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;

import info.aduna.iteration.CloseableIteration;

import org.openrdf.model.Value;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.Order;
import org.openrdf.query.algebra.OrderElem;
import org.openrdf.query.algebra.Service;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.ValueExpr;
import org.openrdf.query.algebra.evaluation.EvaluationStrategy;
import org.openrdf.query.algebra.evaluation.QueryBindingSet;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;
import org.openrdf.query.algebra.evaluation.federation.FederatedService;

/**
 * @author james
 */
public class OrderComparatorTest {

	class EvaluationStrategyStub implements EvaluationStrategy {

		public CloseableIteration<BindingSet, QueryEvaluationException> evaluate(Service expr,
				String serviceUri, CloseableIteration<BindingSet, QueryEvaluationException> bindings)
					throws QueryEvaluationException
		{
			throw new UnsupportedOperationException();
		}

		public CloseableIteration<BindingSet, QueryEvaluationException> evaluate(TupleExpr expr,
				BindingSet bindings)
					throws QueryEvaluationException
		{
			throw new UnsupportedOperationException();
		}

		public Value evaluate(ValueExpr expr, BindingSet bindings)
			throws ValueExprEvaluationException, QueryEvaluationException
		{
			return null;
		}

		public boolean isTrue(ValueExpr expr, BindingSet bindings)
			throws ValueExprEvaluationException, QueryEvaluationException
		{
			throw new UnsupportedOperationException();
		}

		public FederatedService getService(String serviceUrl)
			throws QueryEvaluationException
		{
			throw new UnsupportedOperationException();
		}
	}

	class ComparatorStub extends ValueComparator {

		Iterator<Integer> iter;

		public void setIterator(Iterator<Integer> iter) {
			this.iter = iter;
		}

		@Override
		public int compare(Value o1, Value o2) {
			return iter.next();
		}
	}

	private EvaluationStrategyStub strategy = new EvaluationStrategyStub();

	private Order order = new Order();

	private OrderElem asc = new OrderElem();

	private OrderElem desc = new OrderElem();

	private ComparatorStub cmp = new ComparatorStub();

	private int ZERO = 0;

	private int POS = 378;

	private int NEG = -7349;

	@Test
	public void testEquals()
		throws Exception
	{
		order.addElement(asc);
		cmp.setIterator(Arrays.asList(ZERO).iterator());
		OrderComparator sud = new OrderComparator(strategy, order, cmp);
		assertTrue(sud.compare(null, null) == 0);
	}

	@Test
	public void testZero()
		throws Exception
	{
		order.addElement(asc);
		order.addElement(asc);
		cmp.setIterator(Arrays.asList(ZERO, POS).iterator());
		OrderComparator sud = new OrderComparator(strategy, order, cmp);
		assertTrue(sud.compare(null, null) > 0);
	}

	@Test
	public void testTerm()
		throws Exception
	{
		order.addElement(asc);
		order.addElement(asc);
		cmp.setIterator(Arrays.asList(POS, NEG).iterator());
		OrderComparator sud = new OrderComparator(strategy, order, cmp);
		assertTrue(sud.compare(null, null) > 0);
	}

	@Test
	public void testAscLessThan()
		throws Exception
	{
		order.addElement(asc);
		cmp.setIterator(Arrays.asList(NEG).iterator());
		OrderComparator sud = new OrderComparator(strategy, order, cmp);
		assertTrue(sud.compare(null, null) < 0);
	}

	@Test
	public void testAscGreaterThan()
		throws Exception
	{
		order.addElement(asc);
		cmp.setIterator(Arrays.asList(POS).iterator());
		OrderComparator sud = new OrderComparator(strategy, order, cmp);
		assertTrue(sud.compare(null, null) > 0);
	}

	@Test
	public void testDescLessThan()
		throws Exception
	{
		order.addElement(desc);
		cmp.setIterator(Arrays.asList(NEG).iterator());
		OrderComparator sud = new OrderComparator(strategy, order, cmp);
		assertTrue(sud.compare(null, null) > 0);
	}

	@Test
	public void testDescGreaterThan()
		throws Exception
	{
		order.addElement(desc);
		cmp.setIterator(Arrays.asList(POS).iterator());
		OrderComparator sud = new OrderComparator(strategy, order, cmp);
		assertTrue(sud.compare(null, null) < 0);
	}

	@Test
	public void testDisjunctBindingNames()
		throws Exception
	{
		OrderComparator sud = new OrderComparator(strategy, order, cmp);
		QueryBindingSet a = new QueryBindingSet();
		QueryBindingSet b = new QueryBindingSet();
		a.addBinding("a", ValueFactoryImpl.getInstance().createLiteral("a"));
		b.addBinding("b", ValueFactoryImpl.getInstance().createLiteral("b"));
		assertTrue(sud.compare(a, b) != 0);
		assertTrue(sud.compare(a, b) != sud.compare(b, a));
	}
	
	@Test 
	public void testEqualBindingNamesUnequalValues() {
		OrderComparator sud = new OrderComparator(strategy, order, new ValueComparator());
		QueryBindingSet a = new QueryBindingSet();
		QueryBindingSet b = new QueryBindingSet();
		a.addBinding("a", ValueFactoryImpl.getInstance().createLiteral("ab"));
		a.addBinding("b", ValueFactoryImpl.getInstance().createLiteral("b"));
		b.addBinding("b", ValueFactoryImpl.getInstance().createLiteral("b"));
		b.addBinding("a", ValueFactoryImpl.getInstance().createLiteral("ac"));
		assertTrue(sud.compare(a, b) < 0);
		assertTrue(sud.compare(a, b) != sud.compare(b, a));
	}

	@Before
	public void setUp()
		throws Exception
	{
		asc.setAscending(true);
		desc.setAscending(false);
	}
}
