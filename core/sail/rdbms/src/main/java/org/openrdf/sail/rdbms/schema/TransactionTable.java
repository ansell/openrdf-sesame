/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.schema;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.BlockingQueue;

import org.openrdf.sail.helpers.DefaultSailChangedEvent;

/**
 * Manages a temporary table used when uploading new statements with the same
 * predicate into the database.
 * 
 * @author James Leigh
 * 
 */
public class TransactionTable {
	private int batchSize;
	private TripleTable triples;
	private int addedCount;
	private int removedCount;
	private RdbmsTable temporary;
	private Connection conn;
	private TripleBatch batch;
	private BlockingQueue<Batch> queue;
	private DefaultSailChangedEvent sailChangedEvent;
	private IdSequence ids;
	private PreparedStatement insertSelect;

	public void setIdSequence(IdSequence ids) {
		this.ids = ids;
	}

	public void setQueue(BlockingQueue<Batch> queue) {
		this.queue = queue;
	}

	public void setTemporaryTable(RdbmsTable table) {
		this.temporary = table;
	}

	public void setConnection(Connection conn) {
		this.conn = conn;
	}

	public void setTripleTable(TripleTable statements) {
		this.triples = statements;
	}

	public void setSailChangedEvent(DefaultSailChangedEvent sailChangedEvent) {
		this.sailChangedEvent = sailChangedEvent;
	}

	public int getBatchSize() {
		return batchSize;
	}

	public void setBatchSize(int size) {
		this.batchSize = size;
	}

	public void close()
		throws SQLException
	{
		if (insertSelect != null) {
			insertSelect.close();
		}
		temporary.close();
	}

	public synchronized void insert(Number ctx, Number subj, Number pred, Number obj)
			throws SQLException, InterruptedException {
		if (batch == null || batch.isFull() || !queue.remove(batch)) {
			batch = newTripleBatch();
			batch.setTable(triples);
			batch.setSailChangedEvent(sailChangedEvent);
			batch.setTemporary(temporary);
			batch.setMaxBatchSize(getBatchSize());
			batch.setBatchStatement(prepareInsert());
			if (insertSelect == null) {
				insertSelect = prepareInsertSelect(buildInsertSelect());
			}
			batch.setInsertStatement(insertSelect);
		}
		batch.setObject(1, ctx);
		batch.setObject(2, subj);
		if (temporary == null && !triples.isPredColumnPresent()) {
			batch.setObject(3, obj);
		} else {
			batch.setObject(3, pred);
			batch.setObject(4, obj);
		}
		batch.addBatch();
		queue.put(batch);
		addedCount++;
		triples.getSubjTypes().add(ids.valueOf(subj));
		triples.getObjTypes().add(ids.valueOf(obj));
	}

	public void committed() throws SQLException {
		triples.modified(addedCount, removedCount);
		addedCount = 0;
		removedCount = 0;
	}

	public void removed(int count) throws SQLException {
		removedCount += count;
	}

	public boolean isEmpty() throws SQLException {
		return triples.isEmpty() && addedCount == 0;
	}

	@Override
	public String toString() {
		return triples.toString();
	}

	protected TripleBatch newTripleBatch() {
		return new TripleBatch();
	}

	protected PreparedStatement prepareInsertSelect(String sql) throws SQLException {
		return conn.prepareStatement(sql);
	}

	protected String buildInsertSelect() throws SQLException {
		String tableName = triples.getName();
		StringBuilder sb = new StringBuilder();
		sb.append("INSERT INTO ").append(tableName).append("\n");
		sb.append("SELECT DISTINCT ctx, subj, ");
		if (triples.isPredColumnPresent()) {
			sb.append("pred, ");
		}
		sb.append("obj FROM ");
		sb.append(temporary.getName()).append(" tr\n");
		sb.append("WHERE NOT EXISTS (");
		sb.append("SELECT ctx, subj, ");
		if (triples.isPredColumnPresent()) {
			sb.append("pred, ");
		}
		sb.append("obj FROM ");
		sb.append(tableName).append(" st\n");
		sb.append("WHERE st.ctx = tr.ctx");
		sb.append(" AND st.subj = tr.subj");
		if (triples.isPredColumnPresent()) {
			sb.append(" AND st.pred = tr.pred");
		}
		sb.append(" AND st.obj = tr.obj");
		sb.append(")");
		return sb.toString();
	}

	protected PreparedStatement prepareInsert() throws SQLException {
		if (temporary == null) {
			boolean present = triples.isPredColumnPresent();
			String sql = buildInsert(triples.getName(), present);
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

	protected boolean isPredColumnPresent() {
		return triples.isPredColumnPresent();
	}

}
