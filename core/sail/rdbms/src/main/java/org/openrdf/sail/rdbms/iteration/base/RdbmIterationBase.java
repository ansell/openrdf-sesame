/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.iteration.base;

import info.aduna.iteration.CloseableIteration;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Base class for Iteration of a {@link ResultSet}.
 * 
 * @author James Leigh
 * 
 */
public abstract class RdbmIterationBase<T, X extends Exception> implements
		CloseableIteration<T, X> {
	private PreparedStatement stmt;
	private ResultSet rs;
	private boolean advanced;
	private boolean hasNext;

	public RdbmIterationBase(PreparedStatement stmt) throws SQLException {
		super();
		this.stmt = stmt;
		if (stmt != null) {
			this.rs = stmt.executeQuery();
		}
	}

	public void close() throws X {
		try {
			rs.close();
			stmt.close();
		} catch (SQLException e) {
			throw convertSQLException(e);
		}
	}

	public boolean hasNext() throws X {
		if (advanced)
			return hasNext;
		advanced = true;
		try {
			return hasNext = rs.next();
		} catch (SQLException e) {
			throw convertSQLException(e);
		}
	}

	public T next() throws X {
		try {
			if (!advanced) {
				hasNext = rs.next();
			}
			advanced = false;
			return convert(rs);
		} catch (SQLException e) {
			throw convertSQLException(e);
		}
	}

	public void remove() throws X {
		try {
			rs.rowDeleted();
		} catch (SQLException e) {
			throw convertSQLException(e);
		}
	}

	protected abstract T convert(ResultSet rs) throws SQLException;

	protected abstract X convertSQLException(SQLException e);

}
