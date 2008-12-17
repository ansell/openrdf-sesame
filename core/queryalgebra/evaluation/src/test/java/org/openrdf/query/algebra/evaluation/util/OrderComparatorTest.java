/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.util;

import java.util.Arrays;
import java.util.Iterator;

import junit.framework.TestCase;

import org.openrdf.cursor.Cursor;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.EvaluationException;
import org.openrdf.query.algebra.Order;
import org.openrdf.query.algebra.OrderElem;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.ValueExpr;
import org.openrdf.query.algebra.evaluation.EvaluationStrategy;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;

/**
 * 
 * @author James Leigh
 * 
 */
public class OrderComparatorTest extends TestCase {
	class EvaluationStrategyStub implements EvaluationStrategy {
		public Cursor<BindingSet> evaluate(
				TupleExpr expr, BindingSet bindings)
				throws EvaluationException {
			throw new UnsupportedOperationException();
		}

		public Value evaluate(ValueExpr expr, BindingSet bindings)
				throws ValueExprEvaluationException, EvaluationException {
			return null;
		}

		public boolean isTrue(ValueExpr expr, BindingSet bindings)
				throws ValueExprEvaluationException, EvaluationException {
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

	public void testEquals() throws Exception {
		order.addElement(asc);
		cmp.setIterator(Arrays.asList(ZERO).iterator());
		OrderComparator sud = new OrderComparator(strategy, order, cmp);
		assertTrue(sud.compare(null, null) == 0);
	}

	public void testZero() throws Exception {
		order.addElement(asc);
		order.addElement(asc);
		cmp.setIterator(Arrays.asList(ZERO, POS).iterator());
		OrderComparator sud = new OrderComparator(strategy, order, cmp);
		assertTrue(sud.compare(null, null) > 0);
	}

	public void testTerm() throws Exception {
		order.addElement(asc);
		order.addElement(asc);
		cmp.setIterator(Arrays.asList(POS, NEG).iterator());
		OrderComparator sud = new OrderComparator(strategy, order, cmp);
		assertTrue(sud.compare(null, null) > 0);
	}

	public void testAscLessThan() throws Exception {
		order.addElement(asc);
		cmp.setIterator(Arrays.asList(NEG).iterator());
		OrderComparator sud = new OrderComparator(strategy, order, cmp);
		assertTrue(sud.compare(null, null) < 0);
	}

	public void testAscGreaterThan() throws Exception {
		order.addElement(asc);
		cmp.setIterator(Arrays.asList(POS).iterator());
		OrderComparator sud = new OrderComparator(strategy, order, cmp);
		assertTrue(sud.compare(null, null) > 0);
	}

	public void testDescLessThan() throws Exception {
		order.addElement(desc);
		cmp.setIterator(Arrays.asList(NEG).iterator());
		OrderComparator sud = new OrderComparator(strategy, order, cmp);
		assertTrue(sud.compare(null, null) > 0);
	}

	public void testDescGreaterThan() throws Exception {
		order.addElement(desc);
		cmp.setIterator(Arrays.asList(POS).iterator());
		OrderComparator sud = new OrderComparator(strategy, order, cmp);
		assertTrue(sud.compare(null, null) < 0);
	}

	@Override
	protected void setUp() throws Exception {
		asc.setAscending(true);
		desc.setAscending(false);
	}
}
