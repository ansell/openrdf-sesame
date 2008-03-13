/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.managers;


import java.sql.SQLException;

import org.openrdf.sail.rdbms.managers.base.ManagerBase;
import org.openrdf.sail.rdbms.schema.HashTable;

/**
 * 
 * @author James Leigh
 */
public class HashManager extends ManagerBase {

	public static HashManager instance;

	private HashTable table;

	public HashManager() {
		instance = this;
	}

	public void setHashTable(HashTable table) {
		this.table = table;
	}

	@Override
	public void close()
		throws SQLException
	{
		super.close();
		table.close();
	}

	public int getIdVersion() {
		return table.getIdVersion();
	}

	public void insert(long id, long hash)
		throws SQLException, InterruptedException
	{
		table.insert(id, hash);
	}

	public void optimize()
		throws SQLException
	{
		table.optimize();
	}

	public void removedStatements(int count, String condition) throws SQLException {
		table.removedStatements(count, condition);
	}

}
