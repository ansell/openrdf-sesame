/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.impl;

import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.algebra.Join;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.Union;
import org.openrdf.query.algebra.evaluation.QueryOptimizer;
import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;

/**
 * @author Arjohn Kampman
 */
public class IterativeEvaluationOptimizer implements QueryOptimizer {

	public IterativeEvaluationOptimizer() {
	}

	public void optimize(TupleExpr tupleExpr, Dataset dataset, BindingSet bindings) {
		tupleExpr.visit(new IEOVisitor());
	}

	protected class IEOVisitor extends QueryModelVisitorBase<RuntimeException> {

		@Override
		public void meet(Union union) {
			super.meet(union);

			TupleExpr leftArg = union.getLeftArg();
			TupleExpr rightArg = union.getRightArg();

			if (leftArg instanceof Join && rightArg instanceof Join) {
				Join leftJoinArg = (Join)leftArg;
				Join rightJoin = (Join)rightArg;

				if (leftJoinArg.getLeftArg().equals(rightJoin.getLeftArg())) {
					// factor out the left-most join argument
					Join newJoin = new Join();
					union.replaceWith(newJoin);
					newJoin.setLeftArg(leftJoinArg.getLeftArg());
					newJoin.setRightArg(union);
					union.setLeftArg(leftJoinArg.getRightArg());
					union.setRightArg(rightJoin.getRightArg());

					union.visit(this);
				}
			}
		}
	}
}