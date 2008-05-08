/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 * Copyright James Leigh (c) 2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.impl;

import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.algebra.Filter;
import org.openrdf.query.algebra.Or;
import org.openrdf.query.algebra.SameTerm;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.Union;
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

	public void optimize(TupleExpr tupleExpr, Dataset dataset, BindingSet bindings) {
		tupleExpr.visit(new OrSameTermOptimizer());
	}

	protected class OrSameTermOptimizer extends QueryModelVisitorBase<RuntimeException> {

		@Override
		public void meet(Or orNode) {
			boolean top = orNode.getParentNode() instanceof Filter;
			boolean leftIsSameTerm = orNode.getLeftArg() instanceof SameTerm;
			boolean rightIsSameTerm = orNode.getRightArg() instanceof SameTerm;

			if (top && (leftIsSameTerm || rightIsSameTerm)) {
				Filter filter = (Filter)orNode.getParentNode();

				Filter left = filter.clone();
				Filter right = filter.clone();

				left.setCondition(orNode.getLeftArg().clone());
				right.setCondition(orNode.getRightArg().clone());

				Union union = new Union(left, right);
				filter.replaceWith(union);

				meet(union);
			}
			else {
				super.meet(orNode);
			}
		}
	}
}
