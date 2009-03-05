/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007-2008.
 * Copyright James Leigh (c) 2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openrdf.query.BindingSet;
import org.openrdf.query.algebra.And;
import org.openrdf.query.algebra.Filter;
import org.openrdf.query.algebra.LeftJoin;
import org.openrdf.query.algebra.QueryModel;
import org.openrdf.query.algebra.QueryModelNode;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.ValueExpr;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.algebra.evaluation.QueryOptimizer;
import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;

/**
 * Splits conjunctive constraints into separate constraints. Treats LeftJoin
 * condition as a filter if only references variables from the right side.
 * 
 * @author Arjohn Kampman
 */
public class ConjunctiveConstraintSplitter implements QueryOptimizer {

	public void optimize(QueryModel query, BindingSet bindings) {
		query.visit(new ConstraintVisitor(query));
	}

	protected class ConstraintVisitor extends QueryModelVisitorBase<RuntimeException> {

		protected final TupleExpr tupleExpr;

		public ConstraintVisitor(TupleExpr tupleExpr) {
			this.tupleExpr = tupleExpr;
		}

		/**
		 * If any conjunctive constraints only reference the right side, split
		 * them out of the condition into the right side.
		 */
		@Override
		public void meet(LeftJoin node) {
			super.meet(node);
			ValueExpr condition = node.getCondition();
			if (condition != null) {
				And and = new And();
				List<ValueExpr> constraints = new ArrayList<ValueExpr>(16);
				getConjunctiveConstraints(condition, constraints);

				for (int i = constraints.size() - 1; i >= 0; i--) {
					ValueExpr constraint = constraints.get(i);
					TupleExpr right = node.getRightArg();
					Set<String> filterVars = new VarFinder(constraint).getVars();
					if (right.getBindingNames().containsAll(filterVars)) {
						node.setRightArg(new Filter(right.clone(), constraint.clone()));
					}
					else {
						and.addArg(constraint);
					}
				}
				if (and.getNumberOfArguments() > 1) {
					node.setCondition(and);
				}
				else if (and.getNumberOfArguments() == 1) {
					node.setCondition(and.getArg(0));
				}
				else {
					node.setCondition(null);
				}
			}
		}

		@Override
		public void meet(Filter filter) {
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

		class VarFinder extends QueryModelVisitorBase<RuntimeException> {

			private QueryModelNode node;

			private Set<String> vars;

			public VarFinder(QueryModelNode node) {
				this.node = node;
			}

			public Set<String> getVars() {
				vars = new HashSet<String>();
				node.visit(this);
				return vars;
			}

			@Override
			public void meet(Var var) {
				vars.add(var.getName());
			}
		}
	}
}
