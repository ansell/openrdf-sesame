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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Comparator;
import java.util.UUID;

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
public class OrderComparator implements Comparator<BindingSet>, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7002730491398949902L;

	private final transient Logger logger = LoggerFactory.getLogger(OrderComparator.class);

	private transient EvaluationStrategy strategy;

	private final UUID strategyKey;
	
	private final Order order;

	private transient ValueComparator cmp;

	public OrderComparator(EvaluationStrategy strategy, Order order, ValueComparator vcmp) {
		this.strategy = strategy;
		this.strategyKey = EvaluationStrategies.register(strategy);
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
	
	 private void readObject(ObjectInputStream in) throws IOException,ClassNotFoundException {
		 in.defaultReadObject();
		 this.strategy = EvaluationStrategies.get(this.strategyKey);
		 this.cmp = new ValueComparator();
	 }
}
