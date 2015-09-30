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

import java.util.Set;

import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.algebra.Difference;
import org.openrdf.query.algebra.Distinct;
import org.openrdf.query.algebra.EmptySet;
import org.openrdf.query.algebra.Extension;
import org.openrdf.query.algebra.Filter;
import org.openrdf.query.algebra.Intersection;
import org.openrdf.query.algebra.Join;
import org.openrdf.query.algebra.LeftJoin;
import org.openrdf.query.algebra.Order;
import org.openrdf.query.algebra.QueryModelNode;
import org.openrdf.query.algebra.QueryRoot;
import org.openrdf.query.algebra.Reduced;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.Union;
import org.openrdf.query.algebra.evaluation.QueryOptimizer;
import org.openrdf.query.algebra.helpers.AbstractQueryModelVisitor;
import org.openrdf.query.algebra.helpers.VarNameCollector;

/**
 * Optimizes a query model by pushing {@link Filter}s as far down in the model
 * tree as possible.
 * 
 * @author Arjohn Kampman
 */
public class FilterOptimizer implements QueryOptimizer {

	public void optimize(TupleExpr tupleExpr, Dataset dataset, BindingSet bindings) {
		tupleExpr.visit(new FilterFinder(tupleExpr));
	}

	/*--------------------------*
	 * Inner class FilterFinder *
	 *--------------------------*/

	protected static class FilterFinder extends AbstractQueryModelVisitor<RuntimeException> {

		protected final TupleExpr tupleExpr;

		public FilterFinder(TupleExpr tupleExpr) {
			this.tupleExpr = tupleExpr;
		}

		@Override
		public void meet(Filter filter) {
			super.meet(filter);
			FilterRelocator.relocate(filter);
		}
	}

	/*-----------------------------*
	 * Inner class FilterRelocator *
	 *-----------------------------*/

	protected static class FilterRelocator extends AbstractQueryModelVisitor<RuntimeException> {

		public static void relocate(Filter filter) {
			filter.visit(new FilterRelocator(filter));
		}

		protected final Filter filter;

		protected final Set<String> filterVars;

		public FilterRelocator(Filter filter) {
			this.filter = filter;
			filterVars = VarNameCollector.process(filter.getCondition());
		}

		@Override
		protected void meetNode(QueryModelNode node) {
			// By default, do not traverse
			assert node instanceof TupleExpr;
			relocate(filter, (TupleExpr) node);
		}

		@Override
		public void meet(Join join) {
			if (join.getLeftArg().getBindingNames().containsAll(filterVars)) {
				// All required vars are bound by the left expr
				join.getLeftArg().visit(this);
			}
			else if (join.getRightArg().getBindingNames().containsAll(filterVars)) {
				// All required vars are bound by the right expr
				join.getRightArg().visit(this);
			}
			else {
				relocate(filter, join);
			}
		}

		@Override
		public void meet(LeftJoin leftJoin) {
			if (leftJoin.getLeftArg().getBindingNames().containsAll(filterVars)) {
				leftJoin.getLeftArg().visit(this);
			}
			else {
				relocate(filter, leftJoin);
			}
		}

		@Override
		public void meet(Union union) {
			Filter clone = new Filter();
			clone.setCondition(filter.getCondition().clone());

			relocate(filter, union.getLeftArg());
			relocate(clone, union.getRightArg());

			FilterRelocator.relocate(filter);
			FilterRelocator.relocate(clone);
		}

		@Override
		public void meet(Difference node) {
			Filter clone = new Filter();
			clone.setCondition(filter.getCondition().clone());
		
			relocate(filter, node.getLeftArg());
			relocate(clone, node.getRightArg());
		
			FilterRelocator.relocate(filter);
			FilterRelocator.relocate(clone);
		}

		@Override
		public void meet(Intersection node) {
			Filter clone = new Filter();
			clone.setCondition(filter.getCondition().clone());
		
			relocate(filter, node.getLeftArg());
			relocate(clone, node.getRightArg());
		
			FilterRelocator.relocate(filter);
			FilterRelocator.relocate(clone);
		}

		@Override
		public void meet(Extension node) {
			if (node.getArg().getBindingNames().containsAll(filterVars)) {
				node.getArg().visit(this);
			}
			else {
				relocate(filter, node);
			}
		}

		@Override
		public void meet(EmptySet node) {
			if (filter.getParentNode() != null) {
				// Remove filter from its original location
				filter.replaceWith(filter.getArg());
			}
		}

		@Override
		public void meet(Filter filter) {
			// Filters are commutative
			filter.getArg().visit(this);
		}

		@Override
		public void meet(Distinct node) {
			node.getArg().visit(this);
		}

		@Override
		public void meet(Order node) {
			node.getArg().visit(this);
		}

		@Override
		public void meet(QueryRoot node) {
			node.getArg().visit(this);
		}

		@Override
		public void meet(Reduced node) {
			node.getArg().visit(this);
		}

		protected void relocate(Filter filter, TupleExpr newFilterArg) {
			if (filter.getArg() != newFilterArg) {
				if (filter.getParentNode() != null) {
					// Remove filter from its original location
					filter.replaceWith(filter.getArg());
				}

				// Insert filter at the new location
				newFilterArg.replaceWith(filter);
				filter.setArg(newFilterArg);
			}
		}
	}
}
