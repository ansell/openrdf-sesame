/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.federation.optimizers;

import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.algebra.QueryModelNode;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.evaluation.QueryOptimizer;
import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;
import org.openrdf.sail.federation.algebra.OwnedTupleExpr;

/**
 * Remove redundant {@link OwnedTupleExpr}.
 * 
 * @author James Leigh
 */
public class OwnedTupleExprPruner extends
		QueryModelVisitorBase<RuntimeException> implements QueryOptimizer {

	private OwnedTupleExpr owned;

	public void optimize(TupleExpr query, Dataset dataset, BindingSet bindings) {
		owned = null; // NOPMD
		query.visit(this);
	}

	@Override
	public void meetOther(QueryModelNode node) {
		if (node instanceof OwnedTupleExpr) {
			meetOwnedTupleExpr((OwnedTupleExpr) node);
		} else {
			super.meetOther(node);
		}
	}

	private void meetOwnedTupleExpr(OwnedTupleExpr node) {
		if (owned == null) {
			owned = node;
			super.meetOther(node);
			owned = null; // NOPMD
		} else {
			// no nested OwnedTupleExpr
			TupleExpr replacement = node.getArg().clone();
			node.replaceWith(replacement);
			replacement.visit(this);
		}
	}

}
