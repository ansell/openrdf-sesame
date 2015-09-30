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
import org.openrdf.query.algebra.helpers.AbstractQueryModelVisitor;

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

	protected static class OrSameTermOptimizer extends AbstractQueryModelVisitor<RuntimeException> {

		@Override
		public void meet(Filter filter) {
			if (filter.getCondition() instanceof Or && containsSameTerm(filter.getCondition())) {
				Or orNode = (Or)filter.getCondition();
				TupleExpr filterArg = filter.getArg();

				ValueExpr leftConstraint = orNode.getLeftArg();
				ValueExpr rightConstraint = orNode.getRightArg();

				// remove filter
				filter.replaceWith(filterArg);

				// Push UNION down below other filters to avoid cloning them
				TupleExpr node = findNotFilter(filterArg);

				Filter leftFilter = new Filter(node.clone(), leftConstraint);
				Filter rightFilter = new Filter(node.clone(), rightConstraint);
				Union union = new Union(leftFilter, rightFilter);
				node.replaceWith(union);

				filter.getParentNode().visit(this);
			}
			else {
				super.meet(filter);
			}
		}

		private TupleExpr findNotFilter(TupleExpr node) {
			if (node instanceof Filter) {
				return findNotFilter(((Filter)node).getArg());
			}
			return node;
		}

		private boolean containsSameTerm(ValueExpr node) {
			if (node instanceof SameTerm) {
				return true;
			}
			if (node instanceof Or) {
				Or or = (Or)node;
				boolean left = containsSameTerm(or.getLeftArg());
				return left || containsSameTerm(or.getRightArg());
			}
			if (node instanceof And) {
				And and = (And)node;
				boolean left = containsSameTerm(and.getLeftArg());
				return left || containsSameTerm(and.getRightArg());
			}
			return false;
		}
	}
}
