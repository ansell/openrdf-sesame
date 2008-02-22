/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.postgresql;

import java.sql.SQLException;

import org.openrdf.sail.rdbms.schema.RdbmsTable;
import org.openrdf.sail.rdbms.schema.TransTableManager;
import org.openrdf.sail.rdbms.schema.TransactionTable;

public class PgSqlTransTableManager extends TransTableManager {
	private RdbmsTable table;

	@Override
	public void close() throws SQLException {
		super.close();
		if (table != null) {
			try {
				table.execute("DEALLOCATE " + table.getName() + "_insert");
				table.drop();
			} catch (SQLException e) {
				try {
					table.rollback();
					table.execute("DEALLOCATE " + table.getName() + "_insert");
					table.drop();
				} catch (SQLException e1) {
					// ignore
				}
			}
			table = null;
		}
	}

	@Override
	protected void createTemporaryTable(RdbmsTable table) throws SQLException {
		super.createTemporaryTable(table);
		StringBuilder sb = new StringBuilder();
		sb.append("PREPARE ").append(table.getName());
		sb.append("_insert (bigint, bigint, bigint, bigint) AS\n");
		sb.append("INSERT INTO ").append(table.getName());
		sb.append(" VALUES ($1, $2, $3, $4)");
		table.execute(sb.toString());
		this.table = table;
	}

	@Override
	public TransactionTable createTransactionTable() {
		return new TransactionTable(){
			@Override
			protected String buildInsert(String tableName, boolean predColumnPresent) throws SQLException {
				if (table == null || !tableName.equals(table.getName()))
					return super.buildInsert(tableName, predColumnPresent);
				StringBuilder sb = new StringBuilder();
				sb.append("EXECUTE ").append(tableName);
				sb.append("_insert(?, ?, ?, ?)");
				return sb.toString();
			}
		};
	}

}
