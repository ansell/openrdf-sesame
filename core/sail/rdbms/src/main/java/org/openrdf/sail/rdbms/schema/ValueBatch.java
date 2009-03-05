/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.schema;

import java.sql.SQLException;

/**
 * @author James Leigh
 */
public class ValueBatch extends Batch {

	public static int total_rows;

	public static int total_st;

	private RdbmsTable table;

	public void setTable(RdbmsTable table) {
		assert table != null;
		this.table = table;
	}

	@Override
	public synchronized int flush()
		throws SQLException
	{
		synchronized (table) {
			int count = super.flush();
			total_rows += count;
			total_st += 2;
			table.modified(count, 0);
			return count;
		}
	}

}
