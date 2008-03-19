/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.postgresql.alt;

import org.openrdf.query.algebra.evaluation.util.QueryOptimizerList;
import org.openrdf.sail.rdbms.optimizers.RdbmsQueryOptimizer;

/**
 * Includes the {@link PgSqlRegexFlagsInliner} is the optimisation process.
 * 
 * @author James Leigh
 * 
 */
public class PgSqlQueryOptimizer extends RdbmsQueryOptimizer {

	@Override
	protected void addRdbmsOptimizations(QueryOptimizerList optimizerList) {
		super.addRdbmsOptimizations(optimizerList);
		optimizerList.add(new PgSqlRegexFlagsInliner());
	}

}
