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
import java.util.Map;
import java.util.Set;

import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.algebra.Intersection;
import org.openrdf.query.algebra.Join;
import org.openrdf.query.algebra.LeftJoin;
import org.openrdf.query.algebra.SPARQLIntersection;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.algebra.evaluation.QueryOptimizer;
import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;
import org.openrdf.query.algebra.helpers.StatementPatternCollector;

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

		protected List<TupleExpr> reorderIntersectionArgs(List<TupleExpr> intersectionArgs) {

			List<TupleExpr> result = new ArrayList<TupleExpr>();

			HashMap<Integer, List<TupleExpr[]>> map = new HashMap<Integer, List<TupleExpr[]>>();

			int maxSize = 0;
			for (int i = 0; i < intersectionArgs.size(); i++) {
				TupleExpr firstArg = intersectionArgs.get(i);
				for (int j = 0; j < intersectionArgs.size(); j++) {
					if (i == j) {
						continue;
					}
					TupleExpr secondArg = intersectionArgs.get(j);

					Set<String> names = firstArg.getBindingNames();
					names.retainAll(secondArg.getBindingNames());
					int size = names.size();
					if (size > maxSize) {
						maxSize = size;
					}
					List<TupleExpr[]> l = null;

					if (map.containsKey(size)) {
						l = map.get(size);
					}
					else {
						l = new ArrayList<TupleExpr[]>();
					}
					l.add(new TupleExpr[] { firstArg, secondArg });
					map.put(size, l);
				}
			}

			for (int i = maxSize; i >= 0; i--) {
				if (map.containsKey(i)) {
					List<TupleExpr[]> list = map.get(i);
					for (TupleExpr[] array: list) {
						if (!result.contains(array[0])) {
							result.add(array[0]);
						}
						if (!result.contains(array[1])) {
							result.add(array[1]);
						}						
					}
				}
			}

			return result;
		}

	}
}