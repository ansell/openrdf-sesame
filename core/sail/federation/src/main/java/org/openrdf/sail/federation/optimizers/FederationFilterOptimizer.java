/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.federation.optimizers;

import org.openrdf.query.algebra.Filter;
import org.openrdf.query.algebra.QueryModelNode;
import org.openrdf.query.algebra.evaluation.impl.FilterOptimizer;
import org.openrdf.sail.federation.algebra.OwnedTupleExpr;

/**
 * @author Arjohn Kampman
 */
public class FederationFilterOptimizer extends FilterOptimizer {

	protected FilterRelocator getFilterRelocator(Filter filter) {
		return new FederationFilterRelocator(filter);
	}

	protected class FederationFilterRelocator extends FilterRelocator {

		public FederationFilterRelocator(Filter filter) {
			super(filter);
		}

		@Override
		public void meetOther(QueryModelNode node) {
			if (node instanceof OwnedTupleExpr) {
				// Embed the filter in the owned tuple expression
				OwnedTupleExpr ownedTupleExpr = (OwnedTupleExpr)node;
				relocate(filter, ownedTupleExpr.getArg());
			}
		}
	}
}
