/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 * Copyright James Leigh (c) 2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.algebra.Join;
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

		@Override
		public void meet(Join node)
		{
			List<TupleExpr> joinArgs = new LinkedList<TupleExpr>();
			getJoinArgs(node, joinArgs);

			// Process rest of query model before reordering the joins
			for (TupleExpr joinArg : joinArgs) {
				joinArg.visit(this);
			}

			joinArgs = sortExpressions(joinArgs, new HashSet<String>());

			// Build new join hierarchy
			TupleExpr replacement = joinArgs.get(0);
			for (int i = 1; i < joinArgs.size(); i++) {
				replacement = new Join(replacement, joinArgs.get(i));
			}

			// Replace old join hierarchy
			node.replaceWith(replacement);
		}

		protected void getJoinArgs(TupleExpr tupleExpr, List<TupleExpr> joinArgs) {
			if (tupleExpr instanceof Join) {
				Join join = (Join)tupleExpr;
				getJoinArgs(join.getLeftArg(), joinArgs);
				getJoinArgs(join.getRightArg(), joinArgs);
			}
			else {
				joinArgs.add(tupleExpr);
			}
		}

		/**
		 * Merges the boolean constraints and the path expressions in one single
		 * list. Path expressions are heuristically reordered to minimize query
		 * evaluation time and boolean constraints are inserted between them. The
		 * separate boolean constraints are moved to the start of the list as much
		 * as possible, under the condition that all variables that are used in
		 * the constraint are instantiated by the path expressions that are
		 * earlier in the list. An example combined list might be:
		 * <tt>[(A,B,C), A != foo:bar, (B,E,F), C != F, (F,G,H)]</tt>.
		 */
		protected List<TupleExpr> sortExpressions(List<TupleExpr> expressions, Set<String> boundVars) {
			List<TupleExpr> orderedExpressions = new ArrayList<TupleExpr>(expressions.size());

			while (!expressions.isEmpty()) {
				TupleExpr tupleExpr = selectNextTupleExpr(expressions, boundVars);

				expressions.remove(tupleExpr);
				orderedExpressions.add(tupleExpr);

				boundVars.addAll(tupleExpr.getBindingNames());
			}

			return orderedExpressions;
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