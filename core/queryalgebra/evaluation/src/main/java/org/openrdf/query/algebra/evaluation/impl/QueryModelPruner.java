/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.impl;

import org.openrdf.query.BindingSet;
import org.openrdf.query.algebra.Difference;
import org.openrdf.query.algebra.EmptySet;
import org.openrdf.query.algebra.Intersection;
import org.openrdf.query.algebra.Join;
import org.openrdf.query.algebra.LeftJoin;
import org.openrdf.query.algebra.QueryModel;
import org.openrdf.query.algebra.SingletonSet;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.Union;
import org.openrdf.query.algebra.ValueConstant;
import org.openrdf.query.algebra.ValueExpr;
import org.openrdf.query.algebra.evaluation.QueryOptimizer;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;
import org.openrdf.query.algebra.evaluation.util.QueryEvaluationUtil;
import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;

/**
 * A query optimizer that prunes query model trees by removing superfluous parts
 * and/or by reducing complex parts with simpler parts.
 * 
 * @author Arjohn Kampman
 */
public class QueryModelPruner implements QueryOptimizer {

	public QueryModelPruner() {
	}

	/**
	 * Applies generally applicable optimizations: path expressions are sorted
	 * from more to less specific.
	 * 
	 * @param query
	 */
	public void optimize(QueryModel query, BindingSet bindings) {
		query.visit(new TreeSanitizer());
	}

	protected class TreeSanitizer extends QueryModelVisitorBase<RuntimeException> {

		@Override
		public void meet(Join join) {
			super.meet(join);
			for (TupleExpr arg : join.getArgs()) {
				if (arg instanceof SingletonSet) {
					join.removeArg(arg);
				}
				else if (arg instanceof EmptySet) {
					join.replaceWith(new EmptySet());
					return;
				}
			}
			if (join.getNumberOfArguments() == 1) {
				join.replaceWith(join.getArg(0));
			}
		}

		@Override
		public void meet(LeftJoin leftJoin) {
			super.meet(leftJoin);

			TupleExpr leftArg = leftJoin.getLeftArg();
			TupleExpr rightArg = leftJoin.getRightArg();
			ValueExpr condition = leftJoin.getCondition();

			if (leftArg instanceof EmptySet) {
				leftJoin.replaceWith(leftArg);
			}
			else if (rightArg instanceof EmptySet) {
				leftJoin.replaceWith(leftArg);
			}
			else if (rightArg instanceof SingletonSet) {
				leftJoin.replaceWith(leftArg);
			}
			else if (condition instanceof ValueConstant) {
				boolean conditionValue;
				try {
					conditionValue = QueryEvaluationUtil.getEffectiveBooleanValue(((ValueConstant)condition).getValue());
				}
				catch (ValueExprEvaluationException e) {
					conditionValue = false;
				}

				if (conditionValue == false) {
					// Constraint is always false
					leftJoin.replaceWith(leftArg);
				}
				else {
					leftJoin.setCondition(null);
				}
			}
		}

		@Override
		public void meet(Union union) {
			super.meet(union);

			for (TupleExpr arg : union.getArgs()) {
				if (arg instanceof EmptySet) {
					union.removeArg(arg);
				}
			}
			for (TupleExpr arg : union.getArgs()) {
				if (!(arg instanceof SingletonSet))
					return;
			}
			union.replaceWith(new SingletonSet());
		}

		@Override
		public void meet(Difference difference) {
			super.meet(difference);

			TupleExpr leftArg = difference.getLeftArg();
			TupleExpr rightArg = difference.getRightArg();

			if (leftArg instanceof EmptySet) {
				difference.replaceWith(leftArg);
			}
			else if (rightArg instanceof EmptySet) {
				difference.replaceWith(leftArg);
			}
			else if (leftArg instanceof SingletonSet && rightArg instanceof SingletonSet) {
				difference.replaceWith(new EmptySet());
			}
		}

		@Override
		public void meet(Intersection intersection) {
			super.meet(intersection);

			TupleExpr leftArg = intersection.getLeftArg();
			TupleExpr rightArg = intersection.getRightArg();

			if (leftArg instanceof EmptySet || rightArg instanceof EmptySet) {
				intersection.replaceWith(leftArg);
			}
		}
	}
}