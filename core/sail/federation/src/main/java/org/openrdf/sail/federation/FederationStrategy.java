/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.federation;

import org.openrdf.query.BindingSet;
import org.openrdf.query.Cursor;
import org.openrdf.query.algebra.QueryModel;
import org.openrdf.query.algebra.UnaryTupleOperator;
import org.openrdf.query.algebra.evaluation.TripleSource;
import org.openrdf.query.algebra.evaluation.impl.EvaluationStrategyImpl;
import org.openrdf.sail.federation.algebra.OwnedTupleExpr;
import org.openrdf.store.StoreException;


/**
 *
 * @author James Leigh
 */
public class FederationStrategy extends EvaluationStrategyImpl {

	public FederationStrategy(TripleSource tripleSource, QueryModel query) {
		super(tripleSource, query);
	}

	@Override
	public Cursor<BindingSet> evaluate(UnaryTupleOperator expr, BindingSet bindings)
		throws StoreException
	{
		if (expr instanceof OwnedTupleExpr) {
			OwnedTupleExpr owned = (OwnedTupleExpr) expr;
			return evaluate(owned.getArg(), bindings);
		} else {
			return super.evaluate(expr, bindings);
		}
	}

}
