/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.util;

import java.util.ArrayList;
import java.util.List;

import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.evaluation.QueryOptimizer;
import org.openrdf.store.StoreException;

/**
 * A query optimizer that contains a list of other query optimizers, which are
 * called consecutively when the list's {@link #optimize(TupleExpr, Dataset, BindingSet)}
 * method is called.
 * 
 * @author Arjohn Kampman
 */
public class QueryOptimizerList implements QueryOptimizer {

	protected List<QueryOptimizer> optimizers;

	public QueryOptimizerList() {
		this.optimizers = new ArrayList<QueryOptimizer>(8);
	}

	public QueryOptimizerList(List<QueryOptimizer> optimizers) {
		this.optimizers = new ArrayList<QueryOptimizer>(optimizers);
	}

	public QueryOptimizerList(QueryOptimizer... optimizers) {
		this.optimizers = new ArrayList<QueryOptimizer>(optimizers.length);
		for (QueryOptimizer optimizer : optimizers) {
			this.optimizers.add(optimizer);
		}
	}

	public void add(QueryOptimizer optimizer) {
		optimizers.add(optimizer);
	}

	public void optimize(TupleExpr tupleExpr, Dataset dataset, BindingSet bindings)
		throws StoreException
	{
		for (QueryOptimizer optimizer : optimizers) {
			optimizer.optimize(tupleExpr, dataset, bindings);
		}
	}
}
