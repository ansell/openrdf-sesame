/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.schema;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * 
 * @author James Leigh
 */
public class ValueBatch {

	public static int total_rows;

	public static int total_st;

	public static ValueBatch CLOSE_SIGNAL = new ValueBatch();

	private RdbmsTable table;

	private RdbmsTable temporary;

	private PreparedStatement batch;

	private PreparedStatement insert;

	public int maxBatchSize;

	private int batchCount;

	public void setTable(RdbmsTable table) {
		assert table != null;
		this.table = table;
	}

	public void setTemporary(RdbmsTable temporary) {
		assert temporary != null;
		this.temporary = temporary;
	}

	public void setBatch(PreparedStatement batch) {
		assert batch != null;
		this.batch = batch;
	}

	public void setInsert(PreparedStatement insert) {
		assert insert != null;
		this.insert = insert;
	}

	public int getMaxBatchSize() {
		return maxBatchSize;
	}

	public void setMaxBatchSize(int batchSize) {
		this.maxBatchSize = batchSize;
	}

	public boolean isFull() {
		return batchCount >= getMaxBatchSize();
	}

	public synchronized void insert(long id, Object value)
		throws SQLException
	{
		batch.setLong(1, id);
		batch.setObject(2, value);
		batch.addBatch();
		++batchCount;
	}

	public synchronized int flush()
		throws SQLException
	{
		try {
			synchronized (table) {
				int count = 0;
				synchronized (temporary) {
					batch.executeBatch();
					total_st += 1;
					count = insert.executeUpdate();
					total_st += 1;
					total_rows += count;
					temporary.clear();
					total_st += 1;
				}
				table.modified(count, 0);
				return count;
			}
		}
		finally {
			batch.close();
			batch = null;
			insert.close();
			insert = null;
		}
	}
}
