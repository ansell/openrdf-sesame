/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.postgresql;

import java.sql.Connection;

import org.openrdf.sail.rdbms.schema.TripleTable;
import org.openrdf.sail.rdbms.schema.RdbmsTable;
import org.openrdf.sail.rdbms.schema.RdbmsTableFactory;

/**
 * Overrides PostgreSQL specific table commands.
 * 
 * @author James Leigh
 * 
 */
public class PgSqlTableFactory extends RdbmsTableFactory {

	@Override
	public PgSqlValueTable newValueTable() {
		return new PgSqlValueTable();
	}

	@Override
	protected RdbmsTable newTable(String name) {
		return new PgSqlTable(name);
	}

	@Override
	public TripleTable createTripleTable(Connection conn, String tableName) {
		return super.createTripleTable(conn, tableName.toLowerCase());
	}
}
