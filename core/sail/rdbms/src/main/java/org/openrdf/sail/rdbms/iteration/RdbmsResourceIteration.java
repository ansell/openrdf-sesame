/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.iteration;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.openrdf.sail.SailException;
import org.openrdf.sail.rdbms.RdbmsValueFactory;
import org.openrdf.sail.rdbms.exceptions.RdbmsException;
import org.openrdf.sail.rdbms.iteration.base.RdbmIterationBase;
import org.openrdf.sail.rdbms.model.RdbmsResource;

/**
 * Converts a {@link ResultSet} into a {@link RdbmsResource} in an iteration.
 * 
 * @author James Leigh
 * 
 */
public class RdbmsResourceIteration extends
		RdbmIterationBase<RdbmsResource, SailException> {
	private RdbmsValueFactory vf;

	public RdbmsResourceIteration(RdbmsValueFactory vf, PreparedStatement stmt)
			throws SQLException {
		super(stmt);
		this.vf = vf;
	}

	@Override
	protected RdbmsResource convert(ResultSet rs) throws SQLException {
		long id = rs.getLong(0 + 1);
		return vf.getRdbmsResource(id, rs.getString(0 + 2));
	}

	@Override
	protected RdbmsException convertSQLException(SQLException e) {
		return new RdbmsException(e);
	}

}
