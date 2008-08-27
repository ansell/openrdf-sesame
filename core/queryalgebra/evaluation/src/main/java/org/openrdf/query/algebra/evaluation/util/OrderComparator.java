/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.util;

import java.util.Comparator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openrdf.StoreException;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.algebra.Order;
import org.openrdf.query.algebra.OrderElem;
import org.openrdf.query.algebra.evaluation.EvaluationStrategy;

/**
 * 
 * @author james
 * 
 */
public class OrderComparator implements Comparator<BindingSet> {
	private Logger logger = LoggerFactory.getLogger(OrderComparator.class);

	private EvaluationStrategy strategy;

	private Order order;

	private ValueComparator cmp;

	public OrderComparator(EvaluationStrategy strategy, Order order,
			ValueComparator vcmp) {
		this.strategy = strategy;
		this.order = order;
		this.cmp = vcmp;
	}

	public int compare(BindingSet o1, BindingSet o2) {
		try {
			for (OrderElem element : order.getElements()) {
				Value v1 = strategy.evaluate(element.getExpr(), o1);
				Value v2 = strategy.evaluate(element.getExpr(), o2);
				int compare = cmp.compare(v1, v2);
				if (compare == 0)
					continue;
				if (element.isAscending())
					return compare;
				if (compare > 0)
					return -1;
				if (compare < 0)
					return 1;
			}
			return 0;
		} catch (StoreException e) {
			logger.error(e.getMessage(), e);
			return 0;
		} catch (IllegalArgumentException e) {
			logger.error(e.getMessage(), e);
			return 0;
		}
	}

}