/*******************************************************************************
 * Copyright (c) 2015, Eclipse Foundation, Inc. and its licensors.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * - Neither the name of the Eclipse Foundation, Inc. nor the names of its
 *   contributors may be used to endorse or promote products derived from this
 *   software without specific prior written permission. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package org.eclipse.rdf4j.query.algebra.evaluation.impl;

import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.Dataset;
import org.eclipse.rdf4j.query.algebra.Difference;
import org.eclipse.rdf4j.query.algebra.EmptySet;
import org.eclipse.rdf4j.query.algebra.Intersection;
import org.eclipse.rdf4j.query.algebra.Join;
import org.eclipse.rdf4j.query.algebra.LeftJoin;
import org.eclipse.rdf4j.query.algebra.SingletonSet;
import org.eclipse.rdf4j.query.algebra.TupleExpr;
import org.eclipse.rdf4j.query.algebra.Union;
import org.eclipse.rdf4j.query.algebra.ValueConstant;
import org.eclipse.rdf4j.query.algebra.ValueExpr;
import org.eclipse.rdf4j.query.algebra.evaluation.QueryOptimizer;
import org.eclipse.rdf4j.query.algebra.evaluation.ValueExprEvaluationException;
import org.eclipse.rdf4j.query.algebra.evaluation.util.QueryEvaluationUtil;
import org.eclipse.rdf4j.query.algebra.helpers.AbstractQueryModelVisitor;

/**
 * A query optimizer that prunes query model trees by removing superfluous parts
 * and/or by reducing complex parts with simpler parts.
 * 
 * @author Arjohn Kampman
 * @deprecated Replaced by {@link QueryModelNormalizer}.
 */
@Deprecated
public class QueryModelPruner implements QueryOptimizer {

	public QueryModelPruner() {
	}

	/**
	 * Applies generally applicable optimizations: path expressions are sorted
	 * from more to less specific.
	 * 
	 * @param tupleExpr
	 */
	public void optimize(TupleExpr tupleExpr, Dataset dataset, BindingSet bindings) {
		tupleExpr.visit(new TreeSanitizer());
	}

	protected static class TreeSanitizer extends AbstractQueryModelVisitor<RuntimeException> {

		@Override
		public void meet(Join join) {
			super.meet(join);

			TupleExpr leftArg = join.getLeftArg();
			TupleExpr rightArg = join.getRightArg();

			if (leftArg instanceof EmptySet || rightArg instanceof EmptySet) {
				join.replaceWith(new EmptySet());
			}
			else if (leftArg instanceof SingletonSet) {
				join.replaceWith(rightArg);
			}
			else if (rightArg instanceof SingletonSet) {
				join.replaceWith(leftArg);
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

			TupleExpr leftArg = union.getLeftArg();
			TupleExpr rightArg = union.getRightArg();

			if (leftArg instanceof EmptySet) {
				union.replaceWith(rightArg);
			}
			else if (rightArg instanceof EmptySet) {
				union.replaceWith(leftArg);
			}
			else if (leftArg instanceof SingletonSet && rightArg instanceof SingletonSet) {
				union.replaceWith(leftArg);
			}
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
				intersection.replaceWith(new EmptySet());
			}
		}
	}
}
