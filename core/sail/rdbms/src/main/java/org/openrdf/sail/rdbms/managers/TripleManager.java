/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.managers;

import java.sql.SQLException;

import org.openrdf.sail.rdbms.managers.base.ManagerBase;

/**
 * 
 * @author James Leigh
 */
public class TripleManager extends ManagerBase {

	public static TripleManager instance;

	private TransTableManager statements;

	public TripleManager() {
		instance = this;
	}

	public void setTransTableManager(TransTableManager statements) {
		this.statements = statements;
	}

	@Override
	public void close()
		throws SQLException
	{
		super.close();
		statements.close();
	}

	public void insert(long ctx, long subj, long pred, long obj)
		throws SQLException, InterruptedException
	{
		statements.insert(ctx, subj, pred, obj);
	}

}
