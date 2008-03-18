/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.postgresql;

import java.sql.Connection;

import org.openrdf.sail.rdbms.schema.HashTable;
import org.openrdf.sail.rdbms.schema.TripleTable;
import org.openrdf.sail.rdbms.schema.ValueTable;
import org.openrdf.sail.rdbms.schema.ValueTableFactory;

/**
 * Overrides PostgreSQL specific table commands.
 * 
 * @author James Leigh
 * 
 */
public class PgSqlValueTableFactory extends ValueTableFactory {

	public PgSqlValueTableFactory() {
		super(new PgSqlTableFactory());
	}

	@Override
	public PgSqlValueTable newValueTable() {
		return new PgSqlValueTable();
	}

	@Override
	public TripleTable createTripleTable(Connection conn, String tableName) {
		return super.createTripleTable(conn, tableName.toLowerCase());
	}

	@Override
	protected HashTable newHashtable(ValueTable table) {
		return new PgSqlHashtable(table);
	}
}
