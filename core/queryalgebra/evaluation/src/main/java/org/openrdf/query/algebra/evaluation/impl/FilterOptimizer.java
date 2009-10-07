/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 * Copyright James Leigh (c) 2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.impl;

import java.util.Set;

import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.algebra.Filter;
import org.openrdf.query.algebra.Group;
import org.openrdf.query.algebra.Join;
import org.openrdf.query.algebra.LeftJoin;
import org.openrdf.query.algebra.QueryModelNode;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.Union;
import org.openrdf.query.algebra.ValueExpr;
import org.openrdf.query.algebra.evaluation.QueryOptimizer;
import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;
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

	protected class FilterFinder extends QueryModelVisitorBase<RuntimeException> {

		protected final TupleExpr tupleExpr;

		public FilterFinder(TupleExpr tupleExpr) {
			this.tupleExpr = tupleExpr;
		}

		@Override
		public void meet(Filter filter) {
			super.meet(filter);
			filter.getArg().visit(getFilterRelocator(filter));
		}

		protected FilterRelocator getFilterRelocator(Filter filter) {
			return new FilterRelocator(filter);
		}
	}

	/*-----------------------------*
	 * Inner class FilterRelocator *
	 *-----------------------------*/

	protected class FilterRelocator extends QueryModelVisitorBase<RuntimeException> {

		protected Filter filter;

		protected final Set<String> filterVars;

		public FilterRelocator(Filter filter) {
			this.filter = filter;
			filterVars = VarNameCollector.process(filter.getCondition());
		}

		@Override
		protected void meetNode(QueryModelNode node) {
			// By default, do not visit child nodes
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
			// apply the filter to both arguments
			union.getLeftArg().visit(this);

			ValueExpr conditionClone = filter.getCondition().clone();
			filter = new Filter();
			filter.setCondition(conditionClone);

			union.getRightArg().visit(this);
		}

		@Override
		public void meet(StatementPattern sp) {
			relocate(filter, sp);
		}

		@Override
		public void meet(Filter filter) {
			// Filters are commutative
			filter.getArg().visit(this);
		}

		@Override
		public void meet(Group group) {
			// Prefer evaluation of filters before grouping
			group.getArg().visit(this);
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
