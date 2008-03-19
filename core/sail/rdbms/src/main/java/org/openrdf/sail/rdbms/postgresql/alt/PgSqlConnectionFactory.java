/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.postgresql.alt;

import org.openrdf.sail.rdbms.RdbmsConnectionFactory;
import org.openrdf.sail.rdbms.evaluation.QueryBuilderFactory;
import org.openrdf.sail.rdbms.optimizers.RdbmsQueryOptimizer;
import org.openrdf.sail.rdbms.schema.TableFactory;
import org.openrdf.sail.rdbms.schema.ValueTableFactory;

/**
 * Overrides {@link ValueTableFactory}, {@link QueryBuilderFactory}, and
 * {@link RdbmsQueryOptimizer}. This class also indicates that PostgreSQL does
 * not require a FROM clause.
 * 
 * @author James Leigh
 * 
 */
public class PgSqlConnectionFactory extends RdbmsConnectionFactory {

	@Override
	protected ValueTableFactory createValueTableFactory() {
		return new PgSqlValueTableFactory();
	}

	@Override
	protected TableFactory createTableFactory() {
		return new PgSqlTableFactory();
	}

	@Override
	protected QueryBuilderFactory createQueryBuilderFactory() {
		return new PgQueryBuilderFactory();
	}

	@Override
	protected String getFromDummyTable() {
		return "";
	}

	@Override
	protected RdbmsQueryOptimizer createOptimizer() {
		return new PgSqlQueryOptimizer();
	}
}
