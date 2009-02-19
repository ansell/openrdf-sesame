/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.postgresql;

import org.openrdf.query.BindingSet;
import org.openrdf.query.algebra.QueryModel;
import org.openrdf.sail.rdbms.exceptions.RdbmsException;
import org.openrdf.sail.rdbms.optimizers.RdbmsQueryOptimizer;

/**
 * Includes the {@link PgSqlRegexFlagsInliner} is the optimisation process.
 * 
 * @author James Leigh
 * 
 */
public class PgSqlQueryOptimizer extends RdbmsQueryOptimizer {

	@Override
	protected void rdbmsOptimizations(QueryModel query, BindingSet bindings)
		throws RdbmsException
	{
		super.rdbmsOptimizations(query, bindings);
		new PgSqlRegexFlagsInliner().optimize(query, bindings);
	}

}
