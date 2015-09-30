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

import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.Dataset;
import org.eclipse.rdf4j.query.algebra.Compare;
import org.eclipse.rdf4j.query.algebra.SameTerm;
import org.eclipse.rdf4j.query.algebra.TupleExpr;
import org.eclipse.rdf4j.query.algebra.ValueConstant;
import org.eclipse.rdf4j.query.algebra.ValueExpr;
import org.eclipse.rdf4j.query.algebra.Var;
import org.eclipse.rdf4j.query.algebra.Compare.CompareOp;
import org.eclipse.rdf4j.query.algebra.evaluation.QueryOptimizer;
import org.eclipse.rdf4j.query.algebra.helpers.AbstractQueryModelVisitor;

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
	 */
	public void optimize(TupleExpr tupleExpr, Dataset dataset, BindingSet bindings) {
		tupleExpr.visit(new CompareVisitor());
	}

	protected static class CompareVisitor extends AbstractQueryModelVisitor<RuntimeException> {

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
				return value instanceof Resource;
			}

			if (valueExpr instanceof Var) {
				Value value = ((Var)valueExpr).getValue();
				return value instanceof Resource;
			}

			return false;
		}
	}
}
