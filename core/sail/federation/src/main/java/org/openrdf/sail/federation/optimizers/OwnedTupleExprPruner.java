/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.federation.optimizers;

import org.openrdf.query.BindingSet;
import org.openrdf.query.algebra.QueryModel;
import org.openrdf.query.algebra.QueryModelNode;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.evaluation.QueryOptimizer;
import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;
import org.openrdf.sail.federation.algebra.OwnedTupleExpr;
import org.openrdf.store.StoreException;

/**
 * Remove redundent {@link OwnedTupleExpr}.
 * 
 * @author James Leigh
 */
public class OwnedTupleExprPruner extends QueryModelVisitorBase<StoreException> implements QueryOptimizer {

	private OwnedTupleExpr owned;

	public void optimize(QueryModel query, BindingSet bindings)
		throws StoreException
	{
		owned = null;
		query.visit(this);
	}

	@Override
	public void meetOther(QueryModelNode node)
		throws StoreException
	{
		if (node instanceof OwnedTupleExpr) {
			meetOwnedTupleExpr((OwnedTupleExpr)node);
		}
		else {
			super.meetOther(node);
		}
	}

	private void meetOwnedTupleExpr(OwnedTupleExpr node)
		throws StoreException
	{
		if (owned == null) {
			owned = node;
			super.meetOther(node);
			owned = null;
		}
		else {
			// no nested OwnedTupleExpr
			TupleExpr replacement = node.getArg().clone();
			node.replaceWith(replacement);
			replacement.visit(this);
		}
	}

}
