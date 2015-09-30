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
import org.openrdf.query.algebra.Distinct;
import org.openrdf.query.algebra.Order;
import org.openrdf.query.algebra.OrderElem;
import org.openrdf.query.algebra.Projection;
import org.openrdf.query.algebra.ProjectionElem;
import org.openrdf.query.algebra.QueryModelNode;
import org.openrdf.query.algebra.Reduced;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.algebra.evaluation.QueryOptimizer;
import org.openrdf.query.algebra.helpers.AbstractQueryModelVisitor;

/**
 * Moves the Order node above the Projection when variables are projected.
 * 
 * @author James Leigh
 */
public class OrderLimitOptimizer implements QueryOptimizer {

	public void optimize(TupleExpr tupleExpr, Dataset dataset, BindingSet bindings) {
		tupleExpr.visit(new OrderOptimizer());
	}

	protected static class OrderOptimizer extends AbstractQueryModelVisitor<RuntimeException> {

		private boolean variablesProjected = true;

		private Projection projection;

		@Override
		public void meet(Projection node) {
			projection = node;
			node.getArg().visit(this);
			projection = null;
		}

		@Override
		public void meet(Order node) {
			for (OrderElem e : node.getElements()) {
				e.visit(this);
			}
			if (variablesProjected) {
				QueryModelNode parent = node.getParentNode();
				if (projection == parent) {
					node.replaceWith(node.getArg().clone());
					node.setArg(projection.clone());
					Order replacement = node.clone();
					projection.replaceWith(replacement);
					QueryModelNode distinct = replacement.getParentNode();
					if (distinct instanceof Distinct) {
						distinct.replaceWith(new Reduced(replacement.clone()));
					}
				}
			}
		}

		@Override
		public void meet(Var node) {
			if (projection != null) {
				boolean projected = false;
				for (ProjectionElem e : projection.getProjectionElemList().getElements()) {
					String source = e.getSourceName();
					String target = e.getTargetName();
					if (node.getName().equals(source) && node.getName().equals(target)) {
						projected = true;
						break;
					}
				}
				if (!projected) {
					variablesProjected = false;
				}
			}
		}

	}
}
