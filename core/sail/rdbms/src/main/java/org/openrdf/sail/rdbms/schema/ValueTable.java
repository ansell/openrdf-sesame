/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.schema;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

/**
 * Manages the rows in a value table. These tables have two columns: an internal
 * id column and a value column.
 * 
 * @author James Leigh
 * 
 */
public class ValueTable {
	public static int total_rows;
	public static int total_st;
	public static int BATCH_SIZE = 8192;
	public static final boolean INDEX_VALUES = false;
	public static final long NIL_ID = 0;
	private static final String[] PKEY = { "id" };
	private static final String[] VALUE_INDEX = { "value" };
	private int length = -1;
	private int sqlType;
	protected String INSERT;
	protected String INSERT_SELECT;
	private String EXPUNGE;
	private RdbmsTable table;
	private RdbmsTable temporary;
	private PreparedStatement insertStmt;
	private int uploadCount;
	private int removedStatementsSinceExpunge;

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public int getSqlType() {
		return sqlType;
	}

	public void setSqlType(int sqlType) {
		this.sqlType = sqlType;
	}

	public RdbmsTable getRdbmsTable() {
		return table;
	}

	public void setRdbmsTable(RdbmsTable table) {
		this.table = table;
	}

	public RdbmsTable getTemporaryTable() {
		return temporary;
	}

	public void setTemporaryTable(RdbmsTable temporary) {
		this.temporary = temporary;
	}

	public String getName() {
		return table.getName();
	}

	public long size() {
		return table.size();
	}

	public int getBatchSize() {
		return BATCH_SIZE;
	}

	public void initialize() throws SQLException {
		StringBuilder sb = new StringBuilder();
		sb.append("INSERT INTO ").append(temporary.getName());
		sb.append(" (id, value) VALUES (?, ?)");
		INSERT = sb.toString();
		sb.delete(0, sb.length());
		sb.append("INSERT INTO ").append(table.getName());
		sb.append(" (id, value) SELECT DISTINCT id, value FROM ");
		sb.append(temporary.getName()).append(" tmp\n");
		sb.append("WHERE NOT EXISTS (SELECT id FROM ").append(table.getName());
		sb.append(" val WHERE val.id = tmp.id)");
		INSERT_SELECT = sb.toString();
		sb.delete(0, sb.length());
		sb.append("DELETE FROM ").append(table.getName()).append("\n");
		sb.append("WHERE 1=1 ");
		EXPUNGE = sb.toString();
		if (!table.isCreated()) {
			createTable(table);
			table.index(PKEY);
			if (INDEX_VALUES) {
				table.index(VALUE_INDEX);
			}
		} else {
			table.count();
		}
		if (!temporary.isCreated()) {
			createTemporaryTable(temporary);
		}
	}

	public void insert(long id, Object value) throws SQLException {
		PreparedStatement stmt = null;
		synchronized (this) {
			if (insertStmt == null) {
				insertStmt = prepareInsert();
			}
			insertStmt.setLong(1, id);
			insertStmt.setObject(2, value);
			insertStmt.addBatch();
			if (++uploadCount > getBatchSize()) {
				uploadCount = 0;
				stmt = insertStmt;
				insertStmt = null;
			}
		}
		if (stmt != null) {
			flush(stmt);
		}
	}

	public int flush() throws SQLException {
		PreparedStatement stmt;
		synchronized (this) {
			uploadCount = 0;
			stmt = insertStmt;
			insertStmt = null;
		}
		if (stmt == null)
			return 0;
		return flush(stmt);
	}

	public void optimize() throws SQLException {
		table.optimize();
	}

	public boolean expungeRemovedStatements(int count, String condition)
			throws SQLException {
		flush();
		removedStatementsSinceExpunge += count;
		if (condition != null && timeToExpunge()) {
			expunge(condition);
			removedStatementsSinceExpunge = 0;
			return true;
		}
		return false;
	}

	protected boolean timeToExpunge() {
		return removedStatementsSinceExpunge > table.size() / 4;
	}

	public void expunge(String condition) throws SQLException {
		synchronized (table) {
			int count = table.executeUpdate(EXPUNGE + condition);
			table.modified(0, count);
		}
	}

	protected PreparedStatement prepareInsert() throws SQLException {
		return temporary.prepareStatement(INSERT);
	}

	protected void createTable(RdbmsTable table) throws SQLException {
		StringBuilder sb = new StringBuilder();
		sb.append("  id BIGINT NOT NULL,\n");
		sb.append("  value ").append(getDeclaredSqlType(sqlType, length));
		sb.append(" NOT NULL\n");
		table.createTable(sb);
	}

	protected void createTemporaryTable(RdbmsTable table) throws SQLException {
		StringBuilder sb = new StringBuilder();
		sb.append("  id BIGINT NOT NULL,\n");
		sb.append("  value ").append(getDeclaredSqlType(sqlType, length));
		sb.append(" NOT NULL\n");
		table.createTemporaryTable(sb);
	}

	protected String getDeclaredSqlType(int type, int length) {
		switch (sqlType) {
		case Types.VARCHAR:
			if (length > 0)
				return "VARCHAR(" + length + ")";
			return "TEXT";
		case Types.LONGVARCHAR:
			if (length > 0)
				return "LONGVARCHAR(" + length + ")";
			return "TEXT";
		case Types.BIGINT:
			return "BIGINT";
		case Types.SMALLINT:
			return "SMALLINT";
		case Types.FLOAT:
			return "FLOAT";
		case Types.DOUBLE:
			return "DOUBLE";
		case Types.DECIMAL:
			return "DECIMAL";
		case Types.BOOLEAN:
			return "BOOLEAN";
		case Types.TIMESTAMP:
			return "TIMESTAMP";
		default:
			throw new AssertionError("Unsupported SQL Type: " + type);
		}
	}

	@Override
	public String toString() {
		return getName();
	}

	private int flush(PreparedStatement stmt)
		throws SQLException
	{
		try {
			synchronized (table) {
				int count = 0;
				synchronized(temporary) {
					stmt.executeBatch();
					total_st += 1;
					count = temporary.executeUpdate(INSERT_SELECT);
					total_st += 1;
					total_rows += count;
					temporary.clear();
					total_st += 1;
				}
				table.modified(count, 0);
				return count;
			}
		} finally {
			stmt.close();
		}
	}

}
