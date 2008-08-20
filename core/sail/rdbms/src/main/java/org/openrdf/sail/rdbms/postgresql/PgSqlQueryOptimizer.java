/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.postgresql;

import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.sail.rdbms.exceptions.RdbmsQueryEvaluationException;
import org.openrdf.sail.rdbms.optimizers.RdbmsQueryOptimizer;

/**
 * Includes the {@link PgSqlRegexFlagsInliner} is the optimisation process.
 * 
 * @author James Leigh
 * 
 */
public class PgSqlQueryOptimizer extends RdbmsQueryOptimizer {

	@Override
	protected void rdbmsOptimizations(TupleExpr expr, Dataset dataset, BindingSet bindings)
		throws RdbmsQueryEvaluationException
	{
		super.rdbmsOptimizations(expr, dataset, bindings);
		new PgSqlRegexFlagsInliner().optimize(expr, dataset, bindings);
	}

}
