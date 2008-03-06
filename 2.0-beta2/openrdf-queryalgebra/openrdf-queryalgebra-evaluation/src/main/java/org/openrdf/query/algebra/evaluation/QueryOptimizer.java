/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 * Copyright James Leigh (c) 2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation;

import org.openrdf.query.BindingSet;
import org.openrdf.query.algebra.TupleExpr;

/**
 * Interface used by {@link EvalutationStrategyImpl} prior to evalutating the
 * query.
 * 
 * @author James Leigh <james@leighnet.ca>
 * 
 */
public interface QueryOptimizer {

	public abstract TupleExpr optimize(TupleExpr tupleExpr, BindingSet bindings);

}