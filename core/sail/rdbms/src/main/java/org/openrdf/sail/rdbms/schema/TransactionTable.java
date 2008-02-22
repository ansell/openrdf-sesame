/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.schema;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Manages a temporary table used when uploading new statements with the same
 * predicate into the database.
 * 
 * @author James Leigh
 * 
 */
public class TransactionTable {
	public static int total_rows;
	public static int total_st;
	public static int total_wait;
	public static int table_wait;
	private int batchSize;
	private PredicateTable statements;
	private int addedCount;
	private int removedCount;
	private PreparedStatement insertStmt;
	private RdbmsTable temporary;
	private int uploadCount;
	private ValueTypes objTypes;
	private ValueTypes subjTypes;
	private Connection conn;

	public void setTemporaryTable(RdbmsTable table) {
		this.temporary = table;
	}

	public void setConnection(Connection conn) {
		this.conn = conn;
	}

	public void setPredicateTable(PredicateTable statements) {
		this.statements = statements;
	}

	public int getUncommittedRowCount() {
		return addedCount + uploadCount;
	}

	public int getBatchSize() {
		return batchSize;
	}

	public void setBatchSize(int size) {
		this.batchSize = size;
	}

	public void initialize() throws SQLException {
		objTypes = statements.getObjTypes().clone();
		subjTypes = statements.getSubjTypes().clone();
	}

	public synchronized void insert(long ctx, long subj, long pred, long obj)
			throws SQLException {
		if (insertStmt == null) {
			insertStmt = prepareInsert();
		}
		insertStmt.setLong(1, ctx);
		insertStmt.setLong(2, subj);
		if (temporary == null && !statements.isPredColumnPresent()) {
			insertStmt.setLong(3, obj);
		} else {
			insertStmt.setLong(3, pred);
			insertStmt.setLong(4, obj);
		}
		subjTypes.add(IdCode.decode(subj));
		objTypes.add(IdCode.decode(obj));
		insertStmt.addBatch();
		if (++uploadCount > getBatchSize()) {
			flush();
		}
	}

	public synchronized int flush() throws SQLException {
		if (insertStmt == null)
			return 0;
		long before = System.currentTimeMillis();
		statements.blockUntilReady();
		long after = System.currentTimeMillis();
		table_wait += after - before;
		if (temporary == null) {
			long start = System.currentTimeMillis();
			insertStmt.executeBatch();
			long end = System.currentTimeMillis();
			addedCount += uploadCount;
			total_rows += uploadCount;
			total_st += 1;
			total_wait += end - start;
			uploadCount = 0;
		} else {
			synchronized (temporary) {
				long start = System.currentTimeMillis();
				insertStmt.executeBatch();
				int count = temporary.executeUpdate(buildInsertSelect());
				long end = System.currentTimeMillis();
				temporary.clear();
				addedCount += count;
				total_rows += count;
				total_st += 2;
				total_wait += end - start;
				uploadCount = 0;
			}
		}
		statements.setObjTypes(objTypes);
		statements.setSubjTypes(subjTypes);
		return addedCount;
	}

	protected String buildInsertSelect() throws SQLException {
		String tableName = statements.getName();
		StringBuilder sb = new StringBuilder();
		sb.append("INSERT INTO ").append(tableName).append("\n");
		sb.append("SELECT DISTINCT ctx, subj, ");
		if (statements.isPredColumnPresent()) {
			sb.append("pred, ");
		}
		sb.append("obj FROM ");
		sb.append(temporary.getName()).append(" tr\n");
		sb.append("WHERE NOT EXISTS (");
		sb.append("SELECT ctx, subj, ");
		if (statements.isPredColumnPresent()) {
			sb.append("pred, ");
		}
		sb.append("obj FROM ");
		sb.append(tableName).append(" st\n");
		sb.append("WHERE st.ctx = tr.ctx");
		sb.append(" AND st.subj = tr.subj");
		if (statements.isPredColumnPresent()) {
			sb.append(" AND st.pred = tr.pred");
		}
		sb.append(" AND st.obj = tr.obj");
		sb.append(")");
		return sb.toString();
	}

	public synchronized void cleanup() throws SQLException {
		if (insertStmt != null) {
			insertStmt.close();
			insertStmt = null;
		}
	}

	public void committed() throws SQLException {
		statements.modified(addedCount, removedCount);
		addedCount = 0;
		removedCount = 0;
	}

	public void removed(int count) throws SQLException {
		removedCount += count;
	}

	protected PreparedStatement prepareInsert() throws SQLException {
		if (temporary == null) {
			boolean present = statements.isPredColumnPresent();
			String sql = buildInsert(statements.getName(), present);
			return conn.prepareStatement(sql);
		}
		String sql = buildInsert(temporary.getName(), true);
		return conn.prepareStatement(sql);
	}

	protected String buildInsert(String tableName, boolean predColumnPresent)
		throws SQLException
	{
		StringBuilder sb = new StringBuilder();
		sb.append("INSERT INTO ").append(tableName);
		sb.append(" (ctx, subj, ");
		if (predColumnPresent) {
			sb.append("pred, ");
		}
		sb.append("obj)\n");
		sb.append("VALUES (?, ?, ");
		if (predColumnPresent) {
			sb.append("?, ");
		}
		sb.append("?)");
		return sb.toString();
	}

	public boolean isEmpty() throws SQLException {
		return statements.isEmpty() && addedCount == 0 && uploadCount == 0;
	}

	@Override
	public String toString() {
		return statements.toString();
	}

	protected boolean isPredColumnPresent() {
		return statements.isPredColumnPresent();
	}

}
