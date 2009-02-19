/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 * Copyright James Leigh (c) 2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.impl;

import org.openrdf.model.Resource;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.EvaluationException;
import org.openrdf.query.algebra.Compare;
import org.openrdf.query.algebra.QueryModel;
import org.openrdf.query.algebra.SameTerm;
import org.openrdf.query.algebra.ValueConstant;
import org.openrdf.query.algebra.ValueExpr;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.algebra.Compare.CompareOp;
import org.openrdf.query.algebra.evaluation.QueryOptimizer;
import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;

/**
 * A query optimizer that replaces {@link Compare} operators with
 * {@link SameTerm}s, if possible.
 * 
 * @author Arjohn Kampman
 */
public class CompareOptimizer implements QueryOptimizer {

	/**
	 * Applies generally applicable optimizations to the supplied query: variable
	 * assignments are inlined.
	 * 
	 * @param query
	 * @return optimized TupleExpr
	 * @throws EvaluationException
	 */
	public void optimize(QueryModel query, BindingSet bindings) {
		query.visit(new CompareVisitor());
	}

	protected class CompareVisitor extends QueryModelVisitorBase<RuntimeException> {

		@Override
		public void meet(Compare compare) {
			super.meet(compare);

			if (compare.getOperator() == CompareOp.EQ) {
				ValueExpr leftArg = compare.getLeftArg();
				ValueExpr rightArg = compare.getRightArg();

				boolean leftIsVar = isVar(leftArg);
				boolean rightIsVar = isVar(rightArg);
				boolean leftIsResource = isResource(leftArg);
				boolean rightIsResource = isResource(rightArg);

				if (leftIsVar && rightIsResource || leftIsResource && rightIsVar || leftIsResource
						&& rightIsResource)
				{
					SameTerm sameTerm = new SameTerm(leftArg, rightArg);
					compare.replaceWith(sameTerm);
				}
			}
		}

		protected boolean isVar(ValueExpr valueExpr) {
			if (valueExpr instanceof Var) {
				return true;
			}

			return false;
		}

		protected boolean isResource(ValueExpr valueExpr) {
			if (valueExpr instanceof ValueConstant) {
				Value value = ((ValueConstant)valueExpr).getValue();

				if (value instanceof Resource) {
					return true;
				}
			}

			return false;
		}
	}
}
