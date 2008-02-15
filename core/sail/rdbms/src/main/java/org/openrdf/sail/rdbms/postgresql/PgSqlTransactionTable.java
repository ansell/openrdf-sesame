/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.postgresql;

import java.sql.SQLException;

import org.openrdf.sail.rdbms.schema.RdbmsTable;
import org.openrdf.sail.rdbms.schema.TransactionTable;

/**
 * Optimises prepared insert statements for PostgreSQL.
 * 
 * @author James Leigh
 * 
 */
public class PgSqlTransactionTable extends TransactionTable {
	private RdbmsTable temporary;

	@Override
	public void setTemporaryTable(RdbmsTable table) {
		super.setTemporaryTable(table);
		temporary = table;
	}

	@Override
	protected String buildInsert() throws SQLException {
		StringBuilder sb = new StringBuilder();
		sb.append("EXECUTE ").append(temporary.getName());
		sb.append("_insert(?, ?, ?)");
		return sb.toString();
	}

}
