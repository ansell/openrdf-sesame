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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.Dataset;
import org.eclipse.rdf4j.query.algebra.And;
import org.eclipse.rdf4j.query.algebra.Filter;
import org.eclipse.rdf4j.query.algebra.LeftJoin;
import org.eclipse.rdf4j.query.algebra.TupleExpr;
import org.eclipse.rdf4j.query.algebra.ValueExpr;
import org.eclipse.rdf4j.query.algebra.evaluation.QueryOptimizer;
import org.eclipse.rdf4j.query.algebra.helpers.AbstractQueryModelVisitor;
import org.eclipse.rdf4j.query.algebra.helpers.VarNameCollector;

/**
 * Splits conjunctive constraints into seperate constraints.
 * 
 * @author Arjohn Kampman
 */
public class ConjunctiveConstraintSplitter implements QueryOptimizer {

	public void optimize(TupleExpr tupleExpr, Dataset dataset, BindingSet bindings) {
		tupleExpr.visit(new ConstraintVisitor(tupleExpr));
	}

	protected static class ConstraintVisitor extends AbstractQueryModelVisitor<RuntimeException> {

		protected final TupleExpr tupleExpr;

		public ConstraintVisitor(TupleExpr tupleExpr) {
			this.tupleExpr = tupleExpr;
		}

		@Override
		public void meet(Filter filter)
		{
			super.meet(filter);

			List<ValueExpr> conjunctiveConstraints = new ArrayList<ValueExpr>(16);
			getConjunctiveConstraints(filter.getCondition(), conjunctiveConstraints);

			TupleExpr filterArg = filter.getArg();

			for (int i = conjunctiveConstraints.size() - 1; i >= 1; i--) {
				Filter newFilter = new Filter(filterArg, conjunctiveConstraints.get(i));
				filterArg = newFilter;
			}

			filter.setCondition(conjunctiveConstraints.get(0));
			filter.setArg(filterArg);
		}

		@Override
		public void meet(LeftJoin node) {
			super.meet(node);

			if (node.getCondition() != null) {
				List<ValueExpr> conjunctiveConstraints = new ArrayList<ValueExpr>(16);
				getConjunctiveConstraints(node.getCondition(), conjunctiveConstraints);
	
				TupleExpr arg = node.getRightArg();
				ValueExpr condition = null;
	
				for (ValueExpr constraint : conjunctiveConstraints) {
					if (isWithinBindingScope(constraint, arg)) {
						arg = new Filter(arg, constraint);
					} else if (condition == null) {
						condition = constraint;
					} else {
						condition = new And(condition, constraint);
					}
				}
	
				node.setCondition(condition);
				node.setRightArg(arg);
			}
		}

		protected void getConjunctiveConstraints(ValueExpr valueExpr, List<ValueExpr> conjunctiveConstraints) {
			if (valueExpr instanceof And) {
				And and = (And)valueExpr;
				getConjunctiveConstraints(and.getLeftArg(), conjunctiveConstraints);
				getConjunctiveConstraints(and.getRightArg(), conjunctiveConstraints);
			}
			else {
				conjunctiveConstraints.add(valueExpr);
			}
		}

		private boolean isWithinBindingScope(ValueExpr condition, TupleExpr node) {
			return node.getBindingNames().containsAll(VarNameCollector.process(condition));
		}
	}
}
