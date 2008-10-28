/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
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
import org.openrdf.query.algebra.Or;
import org.openrdf.query.algebra.QueryModelNode;
import org.openrdf.query.algebra.SameTerm;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.Union;
import org.openrdf.query.algebra.ValueExpr;
import org.openrdf.query.algebra.evaluation.QueryOptimizer;
import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;

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

	protected class OrSameTermOptimizer extends QueryModelVisitorBase<RuntimeException> {

		@Override
		public void meet(Or or) {
			boolean top = or.getParentNode() instanceof Filter;
			if (or.getNumberOfArguments() == 0) {
				return;
			}
			else if (or.getNumberOfArguments() == 1) {
				or.replaceWith(or.getArg(0));
				or.getParentNode().visit(this);
			}
			else if (top && containsSameTerm(or)) {
				Filter filter = (Filter)or.getParentNode();
				QueryModelNode parentNode = filter.getParentNode();

				// Find SameTerm(s)
				List<ValueExpr> args = new ArrayList<ValueExpr>();
				for (ValueExpr arg : or.getArgs()) {
					if (containsSameTerm(arg)) {
						args.add(arg);
						or.removeArg(arg);
					}
				}

				// Add the rest
				if (or.getNumberOfArguments() == 1) {
					args.add(or.getArg(0));
				}
				else if (or.getNumberOfArguments() > 1) {
					args.add(or);
				}

				// remove filter
				filter.replaceWith(filter.getArg());

				// Push UNION down below other filters to avoid cloning them
				TupleExpr node = findNotFilter(filter.getArg());

				List<Filter> filters = new ArrayList<Filter>(args.size());
				for (ValueExpr arg : args) {
					filters.add(new Filter(node.clone(), arg));
				}
				node.replaceWith(new Union(filters));

				parentNode.visit(this);
			}
			else {
				super.meet(or);
			}
		}

		private TupleExpr findNotFilter(TupleExpr node) {
			if (node instanceof Filter)
				return findNotFilter(((Filter)node).getArg());
			return node;
		}

		private boolean containsSameTerm(ValueExpr node) {
			if (node instanceof SameTerm)
				return true;
			if (node instanceof Or) {
				Or or = (Or)node;
				boolean contains = false;
				for (ValueExpr arg : or.getArgs()) {
					contains |= containsSameTerm(arg);
				}
				return contains;
			}
			if (node instanceof And) {
				And and = (And)node;
				boolean contains = false;
				for (ValueExpr arg : and.getArgs()) {
					contains |= containsSameTerm(arg);
				}
				return contains;
			}
			return false;
		}
	}
}
