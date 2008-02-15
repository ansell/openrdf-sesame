/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.postgresql;

import java.sql.SQLException;

import org.openrdf.sail.rdbms.schema.RdbmsTable;

/**
 * Converts table names to lower-case and include the analyse optimisation.
 * 
 * @author James Leigh
 * 
 */
public class PgSqlTable extends RdbmsTable {

	public PgSqlTable(String name) {
		super(name.toLowerCase());
	}

	@Override
	protected String buildOptimize() throws SQLException {
		return "VACUUM ANALYZE " + getName();
	}

}
