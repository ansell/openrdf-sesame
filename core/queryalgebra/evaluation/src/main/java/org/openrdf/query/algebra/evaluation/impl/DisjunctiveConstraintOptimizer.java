/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 * Copyright James Leigh (c) 2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.impl;

import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.algebra.And;
import org.openrdf.query.algebra.Filter;
import org.openrdf.query.algebra.Or;
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

	public void optimize(TupleExpr tupleExpr, Dataset dataset, BindingSet bindings) {
		tupleExpr.visit(new OrSameTermOptimizer());
	}

	protected class OrSameTermOptimizer extends QueryModelVisitorBase<RuntimeException> {

		@Override
		public void meet(Or orNode) {
			boolean top = orNode.getParentNode() instanceof Filter;
			if (top && containsSameTerm(orNode)) {
				Filter filter = (Filter)orNode.getParentNode();
				ValueExpr left = orNode.getLeftArg().clone();
				ValueExpr right = orNode.getRightArg().clone();

				// remove filter
				filter.replaceWith(filter.getArg());

				// Push UNION down below other filters to avoid cloning them
				TupleExpr node = findNotFilter(filter.getArg());

				Filter leftFilter = new Filter(node.clone(), left);
				Filter rightFilter = new Filter(node.clone(), right);
				Union union = new Union(leftFilter, rightFilter);
				node.replaceWith(union);

				filter.getParentNode().visit(this);
			}
			else {
				super.meet(orNode);
			}
		}

		private TupleExpr findNotFilter(TupleExpr node) {
			if (node instanceof Filter)
				return findNotFilter(((Filter) node).getArg());
			return node;
		}

		private boolean containsSameTerm(ValueExpr node) {
			if (node instanceof SameTerm)
				return true;
			if (node instanceof Or) {
				Or or = (Or) node;
				boolean left = containsSameTerm(or.getLeftArg());
				return left || containsSameTerm(or.getRightArg());
			}
			if (node instanceof And) {
				And and = (And) node;
				boolean contains = false;
				for (ValueExpr arg : and.getArgs()) {
					contains |= containsSameTerm(arg);
				}
				return contains;
			}
			return false;
		}
	}
}
