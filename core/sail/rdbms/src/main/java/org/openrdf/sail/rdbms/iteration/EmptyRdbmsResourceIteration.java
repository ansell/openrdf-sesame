/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.iteration;

import java.sql.SQLException;

import org.openrdf.sail.SailException;

/**
 * Empty iteration that extends {@link RdbmsResourceIteration}.
 * 
 * @author James Leigh
 * 
 */
public class EmptyRdbmsResourceIteration extends RdbmsResourceIteration {

	public EmptyRdbmsResourceIteration() throws SQLException {
		super(null, null);
	}

	@Override
	public void close() throws SailException {
	}

	@Override
	public boolean hasNext() throws SailException {
		return false;
	}

}
