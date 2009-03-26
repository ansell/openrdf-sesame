/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.iteration;

import java.sql.SQLException;

import org.openrdf.sail.rdbms.model.RdbmsStatement;

/**
 * Empty cursor that extends {@link RdbmsStatementCursor}.
 * 
 * @author James Leigh
 */
public class EmptyRdbmsStatementCursor extends RdbmsStatementCursor {

	public EmptyRdbmsStatementCursor()
		throws SQLException
	{
		super(null, null, null);
	}

	@Override
	public void close() {
	}

	@Override
	public RdbmsStatement next() {
		return null;
	}

}
