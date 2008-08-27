/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.iteration;

import java.sql.SQLException;

import org.openrdf.StoreException;

/**
 * Empty iteration that extends {@link RdbmsStatementIteration}.
 * 
 * @author James Leigh
 * 
 */
public class EmptyRdbmsStatementIteration extends RdbmsStatementIteration {

	public EmptyRdbmsStatementIteration()
		throws SQLException
	{
		super(null, null, null);
	}

	@Override
	public void close()
		throws StoreException
	{
	}

	@Override
	public boolean hasNext()
		throws StoreException
	{
		return false;
	}

}
