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

	private int maxBatchSize;

	private int batchCount;

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

	public int size() {
		return batchCount;
	}

	public boolean isFull() {
		return batchCount >= getMaxBatchSize();
	}

	public boolean isReady() {
		return true;
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

	/**
	 * 
	 * @return <code>-1</code> if already flushed
	 * @throws SQLException
	 */
	public int flush()
		throws SQLException
	{
		if (insertBatch == null)
			return -1;
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
		}
	}


}
