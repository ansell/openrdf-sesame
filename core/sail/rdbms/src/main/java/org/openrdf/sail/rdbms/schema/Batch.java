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
public class Batch {

	public static Batch CLOSED_SIGNAL = new Batch();

	private RdbmsTable temporary;

	private PreparedStatement insertBatch;

	private PreparedStatement insertSelect;

	private boolean closed;

	private int maxBatchSize;

	private Batch previous;

	private int batchCount;

	public void setPrevious(Batch previous) {
		this.previous = previous;
	}

	public int getMaxBatchSize() {
		return maxBatchSize;
	}

	public void setMaxBatchSize(int batchSize) {
		this.maxBatchSize = batchSize;
	}

	public void setTemporary(RdbmsTable temporary) {
		assert temporary != null;
		this.temporary = temporary;
	}

	public void setBatchStatement(PreparedStatement insert) {
		assert insert != null;
		this.insertBatch = insert;
	}

	public void setInsertStatement(PreparedStatement insert) {
		assert insert != null;
		this.insertSelect = insert;
	}

	/**
	 * Can only have up to two batches for the same table in the queue at once.
	 * 
	 * @throws InterruptedException
	 */
	public void init()
		throws InterruptedException
	{
		if (previous != null && previous.previous != null) {
			//previous.previous.waitUntilClosed();
			previous.previous = null;
		}
	}

	public int size() {
		return batchCount;
	}

	public boolean isFull() {
		return batchCount >= getMaxBatchSize();
	}

	public boolean isReady() {
		return true;
	}

	private synchronized void waitUntilClosed()
		throws InterruptedException
	{
		if (!closed) {
			wait();
		}
	}

	public void setLong(int parameterIndex, long x)
		throws SQLException
	{
		insertBatch.setLong(parameterIndex, x);
	}

	public void setObject(int parameterIndex, Object x)
		throws SQLException
	{
		insertBatch.setObject(parameterIndex, x);
	}

	public void addBatch()
		throws SQLException
	{
		insertBatch.addBatch();
		batchCount++;
	}

	public int flush()
		throws SQLException
	{
		try {
			int count;
			if (temporary == null) {
				int[] results = insertBatch.executeBatch();
				count = results.length;
			}
			else {
				synchronized (temporary) {
					insertBatch.executeBatch();
					count = insertSelect.executeUpdate();
					temporary.clear();
				}
			}
			return count;
		}
		finally {
			insertBatch.close();
			insertBatch = null;
			insertSelect.close();
			insertSelect = null;
			closed = true;
			notify();
		}
	}


}
