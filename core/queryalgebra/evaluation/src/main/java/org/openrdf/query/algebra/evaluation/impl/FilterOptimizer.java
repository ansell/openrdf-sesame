/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 * Copyright James Leigh (c) 2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.impl;

import java.util.Set;

import org.openrdf.query.BindingSet;
import org.openrdf.query.algebra.Filter;
import org.openrdf.query.algebra.Group;
import org.openrdf.query.algebra.Join;
import org.openrdf.query.algebra.LeftJoin;
import org.openrdf.query.algebra.QueryModel;
import org.openrdf.query.algebra.QueryModelNode;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.TupleExpr;
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

	public void optimize(QueryModel query, BindingSet bindings) {
		query.visit(new FilterFinder(query));
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
		public void meet(Filter filter)
		{
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

		protected final Filter filter;

		protected final Set<String> filterVars;

		public FilterRelocator(Filter filter) {
			this.filter = filter;
			filterVars = VarNameCollector.process(filter.getCondition());
		}

		@Override
		protected void meetNode(QueryModelNode node)
		{
			// By default, do not visit child nodes
		}

		@Override
		public void meet(Join join)
		{
			for (TupleExpr arg : join.getArgs()) {
				if (arg.getBindingNames().containsAll(filterVars)) {
					// All required vars are bound by the expr
					arg.visit(this);
					return;
				}
			}
			relocate(filter, join);
		}

		@Override
		public void meet(LeftJoin leftJoin)
		{
			if (leftJoin.getLeftArg().getBindingNames().containsAll(filterVars)) {
				leftJoin.getLeftArg().visit(this);
			}
		}

		@Override
		public void meet(StatementPattern sp)
		{
			relocate(filter, sp);
		}

		@Override
		public void meet(Filter filter)
		{
			// Filters are commutative
			filter.getArg().visit(this);
		}

		@Override
		public void meet(Group group)
		{
			// Prefer evaluation of filters before grouping
			group.getArg().visit(this);
		}

		protected void relocate(Filter filter, TupleExpr newFilterArg) {
			if (filter.getArg() != newFilterArg) {
				// Remove filter from its original location
				filter.replaceWith(filter.getArg());

				// Insert filter at the new location
				newFilterArg.replaceWith(filter);
				filter.setArg(newFilterArg);
			}
		}
	}
}
