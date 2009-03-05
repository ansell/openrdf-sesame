/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 * Copyright James Leigh (c) 2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation;

import org.openrdf.query.BindingSet;
import org.openrdf.query.algebra.QueryModel;
import org.openrdf.query.algebra.evaluation.impl.EvaluationStrategyImpl;
import org.openrdf.store.StoreException;

/**
 * Interface used by {@link EvaluationStrategyImpl} prior to evalutating the
 * query.
 * 
 * @author James Leigh
 * @author Arjohn Kampman
 */
public interface QueryOptimizer {

	public void optimize(QueryModel query, BindingSet bindings)
		throws StoreException;

}