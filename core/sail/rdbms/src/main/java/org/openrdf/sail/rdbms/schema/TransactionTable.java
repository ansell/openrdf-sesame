/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.schema;

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
	private int batchSize;
	private PredicateTable statements;
	private int addedCount;
	private int removedCount;
	private PreparedStatement insertStmt;
	private RdbmsTable temporary;
	private int uploadCount;
	private ValueTypes objTypes;
	private ValueTypes subjTypes;

	public void setTemporaryTable(RdbmsTable table) {
		this.temporary = table;
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
		insertStmt.setLong(3, obj);
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
		statements.blockUntilReady();
		synchronized (temporary) {
			insertStmt.executeBatch();
			int count = temporary.executeUpdate(buildInsertSelect());
			temporary.clear();
			uploadCount = 0;
			addedCount += count;
		}
		statements.setObjTypes(objTypes);
		statements.setSubjTypes(subjTypes);
		return addedCount;
	}

	protected String buildInsertSelect() throws SQLException {
		String tableName = statements.getName();
		StringBuilder sb = new StringBuilder();
		sb.append("INSERT INTO ").append(tableName).append("\n");
		sb.append("SELECT DISTINCT * FROM ");
		sb.append(temporary.getName()).append(" tr\n");
		sb.append("WHERE NOT EXISTS (");
		sb.append("SELECT * FROM ");
		sb.append(tableName).append(" st\n");
		sb.append("WHERE st.ctx = tr.ctx");
		sb.append(" AND st.subj = tr.subj");
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
		return temporary.prepareStatement(buildInsert());
	}

	protected String buildInsert() throws SQLException {
		StringBuilder sb = new StringBuilder();
		sb.append("INSERT INTO ").append(temporary.getName());
		sb.append(" (ctx, subj, obj)\n");
		sb.append("VALUES (?, ?, ?)");
		return sb.toString();
	}

	public boolean isEmpty() throws SQLException {
		return statements.isEmpty() && addedCount == 0 && uploadCount == 0;
	}

	@Override
	public String toString() {
		return statements.toString();
	}

}
