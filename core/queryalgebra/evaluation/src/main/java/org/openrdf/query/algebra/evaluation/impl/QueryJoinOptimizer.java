/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
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
import org.openrdf.query.Dataset;
import org.openrdf.query.algebra.Join;
import org.openrdf.query.algebra.LeftJoin;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.evaluation.QueryOptimizer;
import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;

/**
 * A query optimizer that re-orders nested Joins.
 * 
 * @author Arjohn Kampman
 * @author James Leigh
 */
public class QueryJoinOptimizer implements QueryOptimizer {

	protected final EvaluationStatistics statistics;

	public QueryJoinOptimizer() {
		this(new EvaluationStatistics());
	}

	public QueryJoinOptimizer(EvaluationStatistics statistics) {
		this.statistics = statistics;
	}

	/**
	 * Applies generally applicable optimizations: path expressions are sorted
	 * from more to less specific.
	 * 
	 * @param tupleExpr
	 */
	public void optimize(TupleExpr tupleExpr, Dataset dataset, BindingSet bindings) {
		tupleExpr.visit(new JoinVisitor());
	}

	protected class JoinVisitor extends QueryModelVisitorBase<RuntimeException> {

		Set<String> boundVars = new HashSet<String>();

		@Override
		public void meet(LeftJoin leftJoin) {
			leftJoin.getLeftArg().visit(this);

			Set<String> origBoundVars = boundVars;
			try {
				boundVars = new HashSet<String>(boundVars);
				boundVars.addAll(leftJoin.getLeftArg().getBindingNames());

				leftJoin.getRightArg().visit(this);
			}
			finally {
				boundVars = origBoundVars;
			}
		}

		@Override
		public void meet(Join node) {
			Set<String> origBoundVars = boundVars;
			try {
				boundVars = new HashSet<String>(boundVars);

				// Reorder the (recursive) join arguments to a more optimal sequence
				List<TupleExpr> joinArgs = getJoinArgs(node, new ArrayList<TupleExpr>());
				List<TupleExpr> orderedJoinArgs = new ArrayList<TupleExpr>(joinArgs.size());

				while (!joinArgs.isEmpty()) {
					TupleExpr tupleExpr = selectNextTupleExpr(joinArgs, boundVars);

					joinArgs.remove(tupleExpr);
					orderedJoinArgs.add(tupleExpr);

					// Recursively optimize join arguments
					tupleExpr.visit(this);

					boundVars.addAll(tupleExpr.getBindingNames());
				}

			// Build new join hierarchy
				TupleExpr replacement = orderedJoinArgs.get(0);
				for (int i = 1; i < orderedJoinArgs.size(); i++) {
					replacement = new Join(replacement, orderedJoinArgs.get(i));
			}

			// Replace old join hierarchy
			node.replaceWith(replacement);
		}
			finally {
				boundVars = origBoundVars;
			}
		}

		protected <L extends List<TupleExpr>> L getJoinArgs(TupleExpr tupleExpr, L joinArgs) {
			if (tupleExpr instanceof Join) {
				Join join = (Join)tupleExpr;
				getJoinArgs(join.getLeftArg(), joinArgs);
				getJoinArgs(join.getRightArg(), joinArgs);
			}
			else {
				joinArgs.add(tupleExpr);
			}

			return joinArgs;
		}

		/**
		 * Selects from a list of tuple expressions the next tuple expression that
		 * should be evaluated. This method selects the tuple expression with
		 * highest number of bound variables, preferring variables that have been
		 * bound in other tuple expressions over variables with a fixed value.
		 */
		protected TupleExpr selectNextTupleExpr(List<TupleExpr> expressions, Set<String> boundVars) {
			double lowestCardinality = Double.MAX_VALUE;
			TupleExpr result = null;

			for (TupleExpr tupleExpr : expressions) {
				// Calculate a score for this tuple expression
				double cardinality = statistics.getCardinality(tupleExpr, boundVars);

				if (cardinality < lowestCardinality) {
					// More specific path expression found
					lowestCardinality = cardinality;
					result = tupleExpr;
				}
			}

			return result;
		}
	}
}