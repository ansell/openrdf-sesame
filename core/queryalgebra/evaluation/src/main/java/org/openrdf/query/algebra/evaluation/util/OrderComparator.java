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
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Comparator;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openrdf.model.Value;
import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.Order;
import org.openrdf.query.algebra.OrderElem;
import org.openrdf.query.algebra.ValueExpr;
import org.openrdf.query.algebra.evaluation.EvaluationStrategy;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;

/**
 * A {@link Comparator} on {@link BindingSet}s that imposes a total ordering by
 * examining supplied {@link Order} elements (i.e. the elements of an ORDER BY
 * clause), falling back on a custom predictable ordering for BindingSet
 * elements if no ordering is established on the basis of the Order elements.
 * 
 * @author James Leigh
 * @author Jeen Broekstra
 */
public class OrderComparator implements Comparator<BindingSet>, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7002730491398949902L;

	private final transient Logger logger = LoggerFactory.getLogger(OrderComparator.class);

	private transient EvaluationStrategy strategy;

	private UUID strategyKey;

	private final Order order;

	private transient ValueComparator cmp;

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

			// On the basis of the order clause elements the two binding sets are
			// unordered.
			// We now need to impose a total ordering (as per the
			// contract of java.util.Comparator). We order by
			// size first, then by binding names, then finally by values.

			// null check 
			if (o1 == null || o2 == null) {
				if (o1 == null) {
					return o2 == null ? 0 : 1;
				}
				if (o2 == null) {
					return o1 == null ? 0 : -1;
				}
			}
			
			if (o2.size() != o1.size()) {
				return o1.size() < o2.size() ? 1 : -1;
			}

			// sizes are equal. compare on binding names
			if (!o2.getBindingNames().equals(o1.getBindingNames())) {
				if (!o2.getBindingNames().containsAll(o1.getBindingNames())) {
					return -1;
				}
				if (!o1.getBindingNames().containsAll(o2.getBindingNames())) {
					return 1;
				}
			}

			// binding names equal. compare on all values
			for (Binding o1binding : o1) {
				final Value v1 = o1binding.getValue();
				final Value v2 = o2.getValue(o1binding.getName());

				final int compare = cmp.compare(v1, v2);
				if (compare != 0) {
					return compare;
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
	
	private void writeObject(ObjectOutputStream out) throws IOException {
		this.strategyKey = EvaluationStrategies.register(strategy);
		out.defaultWriteObject();
	}
	
	private void readObject(ObjectInputStream in)
		throws IOException, ClassNotFoundException
	{
		in.defaultReadObject();
		this.strategy = EvaluationStrategies.get(this.strategyKey);
		this.cmp = new ValueComparator();
	}
}
