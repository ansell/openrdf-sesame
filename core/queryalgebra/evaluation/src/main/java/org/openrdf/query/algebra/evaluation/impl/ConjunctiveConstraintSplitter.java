/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007-2008.
 * Copyright James Leigh (c) 2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.impl;

import java.util.ArrayList;
import java.util.List;

import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.algebra.And;
import org.openrdf.query.algebra.Filter;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.ValueExpr;
import org.openrdf.query.algebra.evaluation.QueryOptimizer;
import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;

/**
 * Splits conjunctive constraints into seperate constraints.
 * 
 * @author Arjohn Kampman
 */
public class ConjunctiveConstraintSplitter implements QueryOptimizer {

	public void optimize(TupleExpr tupleExpr, Dataset dataset, BindingSet bindings) {
		tupleExpr.visit(new ConstraintVisitor(tupleExpr));
	}

	protected class ConstraintVisitor extends QueryModelVisitorBase<RuntimeException> {

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

		protected void getConjunctiveConstraints(ValueExpr valueExpr, List<ValueExpr> conjunctiveConstraints) {
			if (valueExpr instanceof And) {
				And and = (And)valueExpr;
				for (ValueExpr arg : and.getArgs()) {
					getConjunctiveConstraints(arg, conjunctiveConstraints);
				}
			}
			else {
				conjunctiveConstraints.add(valueExpr);
			}
		}
	}
}
