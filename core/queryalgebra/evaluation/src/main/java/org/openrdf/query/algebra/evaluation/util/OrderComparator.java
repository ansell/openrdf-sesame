/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.util;

import java.util.Comparator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.Order;
import org.openrdf.query.algebra.OrderElem;
import org.openrdf.query.algebra.ValueExpr;
import org.openrdf.query.algebra.evaluation.EvaluationStrategy;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;

/**
 * @author james
 */
public class OrderComparator implements Comparator<BindingSet> {

	private final Logger logger = LoggerFactory.getLogger(OrderComparator.class);

	private final EvaluationStrategy strategy;

	private final Order order;

	private final ValueComparator cmp;

	public OrderComparator(EvaluationStrategy strategy, Order order, ValueComparator vcmp) {
		this.strategy = strategy;
		this.order = order;
		this.cmp = vcmp;
	}

	public int compare(BindingSet o1, BindingSet o2) {
		try {
			for (OrderElem element : order.getElements()) {
				Value v1 = evaluate(element.getExpr(), o1);
				Value v2 = evaluate(element.getExpr(), o2);

				int compare = cmp.compare(v1, v2);

				if (compare != 0) {
					return element.isAscending() ? compare : -compare;
				}
			}

			return 0;
		}
		catch (QueryEvaluationException e) {
			logger.debug(e.getMessage(), e);
			return 0;
		}
		catch (IllegalArgumentException e) {
			logger.debug(e.getMessage(), e);
			return 0;
		}
	}

	private Value evaluate(ValueExpr valueExpr, BindingSet o)
		throws QueryEvaluationException
	{
		try {
			return strategy.evaluate(valueExpr, o);
		}
		catch (ValueExprEvaluationException exc) {
			return null;
		}
	}
}
