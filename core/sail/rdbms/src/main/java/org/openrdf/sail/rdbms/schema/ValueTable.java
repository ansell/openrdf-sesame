/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.schema;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Manages the rows in a value table. These tables have two columns: an internal
 * id column and a value column.
 * 
 * @author James Leigh
 * 
 */
public class ValueTable<V> {
	public static int CHUNK_SIZE = 15;
	public static int BATCH_SIZE = 128;
	public static final long NIL_ID = 0;
	private static final String[] PKEY = { "id" };
	private static final String[] VALUE_INDEX = { "value" };
	private ConcurrentMap<IdCode, AtomicLong> seq = new ConcurrentHashMap<IdCode, AtomicLong>();
	private int length;
	private int sqlType;
	private String INSERT;
	private String EXPUNGE;
	private RdbmsTable table;
	private PreparedStatement insertStmt;
	private int uploadCount;
	private int removedStatementsSinceExpunge;

	public ValueTable(RdbmsTable table, int sqlType) {
		this(table, sqlType, -1);
	}

	public ValueTable(RdbmsTable table, int sqlType, int length) {
		this.table = table;
		this.sqlType = sqlType;
		this.length = length;
	}

	public String getName() {
		return table.getName();
	}

	public long nextId(IdCode code) {
		if (!seq.containsKey(code)) {
			seq.putIfAbsent(code, new AtomicLong(code.minId()));
		}
		return seq.get(code).incrementAndGet();
	}

	public long size() {
		return table.size();
	}

	public int getSelectChunkSize() {
		return CHUNK_SIZE;
	}

	public int getBatchSize() {
		return BATCH_SIZE;
	}

	public void initialize() throws SQLException {
		StringBuilder sb = new StringBuilder();
		sb.append("INSERT INTO ").append(table.getName());
		sb.append(" (id, value) VALUES (?, ?)");
		INSERT = sb.toString();
		sb.delete(0, sb.length());
		sb.append("DELETE FROM ").append(table.getName()).append("\n");
		sb.append("WHERE 1=1 ");
		EXPUNGE = sb.toString();
		if (!table.isCreated()) {
			createTable();
			table.index(PKEY);
			table.index(VALUE_INDEX);
		} else {
			for (long max : table.maxIds()) {
				IdCode code = IdCode.decode(max);
				if (max > code.minId()) {
					seq.put(code, new AtomicLong(max));
				}
			}
		}
	}

	public synchronized void insert(long id, V value) throws SQLException {
		if (insertStmt == null) {
			insertStmt = prepareInsert();
		}
		insertStmt.setLong(1, id);
		insertStmt.setObject(2, value);
		insertStmt.addBatch();
		if (++uploadCount > getBatchSize()) {
			flush();
		}
	}

	public synchronized int flush() throws SQLException {
		if (insertStmt == null)
			return 0;
		int count = 0;
		for (int result : insertStmt.executeBatch()) {
			if (result > 0) {
				count += result;
			}
		}
		insertStmt = null;
		uploadCount = 0;
		table.modified(count, 0);
		return count;
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
		int count = table.executeUpdate(EXPUNGE + condition);
		table.modified(0, count);
	}

	protected PreparedStatement prepareInsert() throws SQLException {
		return table.prepareStatement(INSERT);
	}

	protected void createTable() throws SQLException {
		StringBuilder sb = new StringBuilder();
		sb.append("  id BIGINT NOT NULL,\n");
		sb.append("  value ").append(getDeclaredSqlType(sqlType, length));
		sb.append(" NOT NULL\n");
		table.createTable(sb);
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

	public PreparedStatement prepareStatement(String sql) throws SQLException {
		flush();
		return table.prepareStatement(sql);
	}

	@Override
	public String toString() {
		return getName();
	}

}
