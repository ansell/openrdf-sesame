/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.iteration;

import java.sql.SQLException;

import org.openrdf.sail.rdbms.model.RdbmsResource;

/**
 * Empty cursor that extends {@link RdbmsResourceCursor}.
 * 
 * @author James Leigh
 */
public class EmptyRdbmsResourceCursor extends RdbmsResourceCursor {

	public EmptyRdbmsResourceCursor()
		throws SQLException
	{
		super(null, null);
	}

	@Override
	public void close() {
	}

	@Override
	public RdbmsResource next() {
		return null;
	}

}
