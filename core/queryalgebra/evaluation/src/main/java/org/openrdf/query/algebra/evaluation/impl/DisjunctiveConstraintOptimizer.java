/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008-2009.
 * Copyright James Leigh (c) 2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.impl;

import java.util.ArrayList;
import java.util.List;

import org.openrdf.query.BindingSet;
import org.openrdf.query.algebra.And;
import org.openrdf.query.algebra.Filter;
import org.openrdf.query.algebra.NaryValueOperator;
import org.openrdf.query.algebra.Or;
import org.openrdf.query.algebra.QueryModel;
import org.openrdf.query.algebra.SameTerm;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.Union;
import org.openrdf.query.algebra.ValueExpr;
import org.openrdf.query.algebra.evaluation.QueryOptimizer;
import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;

/**
 * A query optimizer that optimize disjunctive constraints on tuple expressions.
 * Currently, this optimizer {@link Union unions} a clone of the underlying
 * tuple expression with the original expression for each {@link SameTerm}
 * operator, moving the SameTerm to the cloned tuple expression.
 * 
 * @author Arjohn Kampman
 * @author James Leigh
 */
public class DisjunctiveConstraintOptimizer implements QueryOptimizer {

	public void optimize(QueryModel query, BindingSet bindings) {
		query.visit(new OrSameTermOptimizer());
	}

	protected class OrSameTermOptimizer extends QueryModelVisitorBase<RuntimeException> {

		@Override
		public void meet(Filter filter) {
			if (filter.getCondition() instanceof Or) {
				Or orNode = (Or)filter.getCondition();

				// Search constraints that contain SameTerm's
				List<ValueExpr> constraints = new ArrayList<ValueExpr>();

				for (ValueExpr arg : orNode.getArgs()) {
					if (containsSameTerm(arg)) {
						constraints.add(arg);
						orNode.removeArg(arg);
					}
				}

				// Check there were any constraints with SameTerm's
				if (!constraints.isEmpty()) {
					// Add the rest of the constraint to the args list
					if (orNode.getNumberOfArguments() == 1) {
						constraints.add(orNode.getArg(0));
					}
					else if (orNode.getNumberOfArguments() > 1) {
						constraints.add(orNode);
					}

					// remove the existing Filter
					TupleExpr filterArg = filter.getArg();
					filter.replaceWith(filterArg);

					// Insert a Union over the individual constraints, pushing it
					// down below other filters to avoid cloning them
					TupleExpr node = findNotFilter(filterArg);

					Union union = new Union();
					for (ValueExpr arg : constraints) {
						union.addArg(new Filter(node.clone(), arg));
					}

					node.replaceWith(union);

					// Enter recursion
					filter.getParentNode().visit(this);

					return;
				}
			}

			super.meet(filter);
		}

		private TupleExpr findNotFilter(TupleExpr node) {
			if (node instanceof Filter) {
				return findNotFilter(((Filter)node).getArg());
			}
			return node;
		}

		private boolean containsSameTerm(ValueExpr node) {
			boolean result = false;

			if (node instanceof SameTerm) {
				result = true;
			}
			else if (node instanceof Or || node instanceof And) {
				for (ValueExpr arg : ((NaryValueOperator)node).getArgs()) {
					result |= containsSameTerm(arg);
				}
			}

			return result;
		}
	}
}
