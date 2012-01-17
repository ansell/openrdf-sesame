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
import org.openrdf.query.algebra.Intersection;
import org.openrdf.query.algebra.SPARQLIntersection;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.evaluation.QueryOptimizer;
import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;

/**
 * A query optimizer that reorders SPARQLIntersections.
 * 
 * @author Jeen Broekstra
 * @author Ruslan Velkov
 */
public class SPARQLIntersectionOptimizer implements QueryOptimizer {

	/**
	 * Applies generally applicable optimizations: SPARQLIntersections are sorted
	 * according to the number of overlapping binding variables.
	 * 
	 * @param tupleExpr
	 */
	public void optimize(TupleExpr tupleExpr, Dataset dataset, BindingSet bindings) {
		tupleExpr.visit(new SPARQLIntersectionVisitor());
	}

	protected class SPARQLIntersectionVisitor extends QueryModelVisitorBase<RuntimeException> {

		@Override
		public void meet(Intersection node) {
			if (node instanceof SPARQLIntersection) {
				this.meet((SPARQLIntersection)node);
			}
			else {
				super.meet(node);
			}
		}

		@Override
		public void meet(SPARQLIntersection node) {

			// recursively get all intersection arguments.
			List<TupleExpr> intersectionArgs = getIntersectionArgs(node, new ArrayList<TupleExpr>());

			// reorder intersection arguments based on shared variables.
			List<TupleExpr> orderedIntersectionArgs = reorderIntersectionArgs(intersectionArgs);

			// build the reordered intersection hierarchy
			TupleExpr replacement = new SPARQLIntersection(orderedIntersectionArgs.get(0),
					orderedIntersectionArgs.get(1));

			for (int i = 2; i < orderedIntersectionArgs.size(); i++) {
				replacement = new SPARQLIntersection(replacement, orderedIntersectionArgs.get(i));
			}

			// replace the original node with the reordered tree
			node.replaceWith(replacement);
		}

		protected <L extends List<TupleExpr>> L getIntersectionArgs(TupleExpr tupleExpr, L intersectionArgs) {
			if (tupleExpr instanceof SPARQLIntersection) {
				SPARQLIntersection sparqlIntersection = (SPARQLIntersection)tupleExpr;
				getIntersectionArgs(sparqlIntersection.getLeftArg(), intersectionArgs);
				getIntersectionArgs(sparqlIntersection.getRightArg(), intersectionArgs);
			}
			else {
				intersectionArgs.add(tupleExpr);
			}

			return intersectionArgs;
		}

		/**
		 * Determines an optimal ordering of intersection arguments, based on
		 * variable bindings. An ordering is considered optimal if for each
		 * consecutive element it holds that first of all its shared variables
		 * with all previous elements is maximized, and second, the union of all
		 * its variables with all previous elements is maximized.
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
		 * @param intersectionArgs
		 * @return
		 */
		protected List<TupleExpr> reorderIntersectionArgs(List<TupleExpr> intersectionArgs) {

			List<TupleExpr> result = new ArrayList<TupleExpr>();

			// Step 1: determine size of intersection for each pair of arguments
			HashMap<Integer, List<TupleExpr[]>> intersectionSizes = new HashMap<Integer, List<TupleExpr[]>>();

			int maxIntersectionSize = 0;
			for (int i = 0; i < intersectionArgs.size(); i++) {
				TupleExpr firstArg = intersectionArgs.get(i);
				for (int j = i + 1; j < intersectionArgs.size(); j++) {
					TupleExpr secondArg = intersectionArgs.get(j);

					Set<String> names = firstArg.getBindingNames();
					names.retainAll(secondArg.getBindingNames());

					int interSectionSize = names.size();
					if (interSectionSize > maxIntersectionSize) {
						maxIntersectionSize = interSectionSize;
					}

					List<TupleExpr[]> l = null;

					if (intersectionSizes.containsKey(interSectionSize)) {
						l = intersectionSizes.get(interSectionSize);
					}
					else {
						l = new ArrayList<TupleExpr[]>();
					}
					TupleExpr[] tupleTuple = new TupleExpr[] { firstArg, secondArg };
					l.add(tupleTuple);
					intersectionSizes.put(interSectionSize, l);
				}
			}

			// Step 2: find the first two elements for the ordered list by
			// selecting the pair with first of all,
			// the highest intersection size, and second, the highest union size.

			TupleExpr[] maxUnionTupleTuple = null;
			int currentUnionSize = -1;

			// get a list of all argument pairs with the maximum intersection size
			List<TupleExpr[]> list = intersectionSizes.get(maxIntersectionSize);

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
			while (result.size() < intersectionArgs.size()) {
				result.add(getNextElement(result, intersectionArgs));
			}

			return result;
		}

		private TupleExpr getNextElement(List<TupleExpr> currentList, List<TupleExpr> intersectionArgs) {

			// determine union of names of all elements currently in the list: this
			// corresponds to the projection
			// resulting from intersecting all these elements.
			Set<String> currentListNames = new HashSet<String>();
			for (TupleExpr expr : currentList) {
				currentListNames.addAll(expr.getBindingNames());
			}

			// select the next argument from the list, by checking that it has,
			// first, the highest intersection size
			// with the current list, and second, the highest union size.
			TupleExpr selected = null;
			int currentUnionSize = -1;
			int currentIntersectionSize = -1;
			for (TupleExpr candidate : intersectionArgs) {
				if (!currentList.contains(candidate)) {
					Set<String> names = candidate.getBindingNames();
					names.retainAll(currentListNames);
					int intersectionSize = names.size();

					names = candidate.getBindingNames();
					names.addAll(currentListNames);
					int unionSize = names.size();

					if (intersectionSize > currentIntersectionSize) {
						selected = candidate;
						currentIntersectionSize = intersectionSize;
						currentUnionSize = unionSize;
					}
					else if (intersectionSize == currentIntersectionSize) {
						if (unionSize > currentUnionSize) {
							selected = candidate;
							currentIntersectionSize = intersectionSize;
							currentUnionSize = unionSize;
						}
					}
				}
			}

			return selected;
		}
	}
}