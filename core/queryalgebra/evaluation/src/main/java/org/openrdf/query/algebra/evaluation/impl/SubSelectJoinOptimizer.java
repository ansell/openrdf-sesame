/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2009.
 * Copyright James Leigh (c) 2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.algebra.Join;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.evaluation.QueryOptimizer;
import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;

/**
 * A query optimizer that reorders Joins involving subselects.
 * 
 * @author Jeen Broekstra
 * @author Ruslan Velkov
 */
public class SubSelectJoinOptimizer implements QueryOptimizer {

	/**
	 * Applies generally applicable optimizations: Joins involving subselects are sorted
	 * according to the number of overlapping binding variables.
	 * 
	 * @param tupleExpr
	 */
	public void optimize(TupleExpr tupleExpr, Dataset dataset, BindingSet bindings) {
		tupleExpr.visit(new JoinVisitor());
	}

	protected class JoinVisitor extends QueryModelVisitorBase<RuntimeException> {

		@Override
		public void meet(Join node) {
			
			if (!node.hasSubSelect()) {
				return;
			}

			// recursively get all join arguments.
			List<TupleExpr> joinArgs = getJoinArgs(node, new ArrayList<TupleExpr>());

			// reorder join arguments based on shared variables.
			List<TupleExpr> orderedJoinArgs = reorderJoinArgs(joinArgs);

			// build the reordered join hierarchy
			TupleExpr replacement = new Join(orderedJoinArgs.get(0), orderedJoinArgs.get(1));

			for (int i = 2; i < orderedJoinArgs.size(); i++) {
				replacement = new Join(replacement, orderedJoinArgs.get(i));
			}

			// replace the original node with the reordered tree
			node.replaceWith(replacement);
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
		 * Determines an optimal ordering of join arguments, based on variable
		 * bindings. An ordering is considered optimal if for each consecutive
		 * element it holds that first of all its shared variables with all
		 * previous elements is maximized, and second, the union of all its
		 * variables with all previous elements is maximized.
		 * <p>
		 * Example: reordering
		 * 
		 * <pre>
		 *   [f] [a b c] [e f] [a d] [b e]
		 * </pre>
		 * 
		 * should result in:
		 * 
		 * <pre>
		 *   [a b c] [a d] [b e] [e f] [f]
		 * </pre>
		 * 
		 * @param joinArgs
		 * @return
		 */
		protected List<TupleExpr> reorderJoinArgs(List<TupleExpr> joinArgs) {

			List<TupleExpr> result = new ArrayList<TupleExpr>();

			// Step 1: determine size of join for each pair of arguments
			HashMap<Integer, List<TupleExpr[]>> joinSizes = new HashMap<Integer, List<TupleExpr[]>>();

			int maxJoinSize = 0;
			for (int i = 0; i < joinArgs.size(); i++) {
				TupleExpr firstArg = joinArgs.get(i);
				for (int j = i + 1; j < joinArgs.size(); j++) {
					TupleExpr secondArg = joinArgs.get(j);

					Set<String> names = firstArg.getBindingNames();
					names.retainAll(secondArg.getBindingNames());

					int joinSize = names.size();
					if (joinSize > maxJoinSize) {
						maxJoinSize = joinSize;
					}

					List<TupleExpr[]> l = null;

					if (joinSizes.containsKey(joinSize)) {
						l = joinSizes.get(joinSize);
					}
					else {
						l = new ArrayList<TupleExpr[]>();
					}
					TupleExpr[] tupleTuple = new TupleExpr[] { firstArg, secondArg };
					l.add(tupleTuple);
					joinSizes.put(joinSize, l);
				}
			}

			// Step 2: find the first two elements for the ordered list by
			// selecting the pair with first of all,
			// the highest join size, and second, the highest union size.

			TupleExpr[] maxUnionTupleTuple = null;
			int currentUnionSize = -1;

			// get a list of all argument pairs with the maximum join size
			List<TupleExpr[]> list = joinSizes.get(maxJoinSize);

			// select the pair that has the highest union size.
			for (TupleExpr[] tupleTuple : list) {
				Set<String> names = tupleTuple[0].getBindingNames();
				names.addAll(tupleTuple[1].getBindingNames());
				int unionSize = names.size();

				if (unionSize > currentUnionSize) {
					maxUnionTupleTuple = tupleTuple;
					currentUnionSize = unionSize;
				}
			}

			// add the pair to the result list.
			result.add(maxUnionTupleTuple[0]);
			result.add(maxUnionTupleTuple[1]);

			// Step 3: sort the rest of the list by selecting and adding an element
			// at a time.
			while (result.size() < joinArgs.size()) {
				result.add(getNextElement(result, joinArgs));
			}

			return result;
		}

		private TupleExpr getNextElement(List<TupleExpr> currentList, List<TupleExpr> joinArgs) {

			// determine union of names of all elements currently in the list: this
			// corresponds to the projection resulting from joining all these
			// elements.
			Set<String> currentListNames = new HashSet<String>();
			for (TupleExpr expr : currentList) {
				currentListNames.addAll(expr.getBindingNames());
			}

			// select the next argument from the list, by checking that it has,
			// first, the highest join size with the current list, and second, the
			// highest union size.
			TupleExpr selected = null;
			int currentUnionSize = -1;
			int currentJoinSize = -1;
			for (TupleExpr candidate : joinArgs) {
				if (!currentList.contains(candidate)) {
					Set<String> names = candidate.getBindingNames();
					names.retainAll(currentListNames);
					int joinSize = names.size();

					names = candidate.getBindingNames();
					names.addAll(currentListNames);
					int unionSize = names.size();

					if (joinSize > currentJoinSize) {
						selected = candidate;
						currentJoinSize = joinSize;
						currentUnionSize = unionSize;
					}
					else if (joinSize == currentJoinSize) {
						if (unionSize > currentUnionSize) {
							selected = candidate;
							currentJoinSize = joinSize;
							currentUnionSize = unionSize;
						}
					}
				}
			}

			return selected;
		}
	}
}