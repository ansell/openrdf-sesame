/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.cursor.base;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.openrdf.cursor.Cursor;
import org.openrdf.sail.rdbms.exceptions.RdbmsException;

/**
 * Base class for an rdbms cursor.
 * 
 * @author James Leigh
 */
public abstract class RdbmCursorBase<T> implements Cursor<T> {

	private PreparedStatement stmt;

	private ResultSet rs;

	public RdbmCursorBase(PreparedStatement stmt)
		throws SQLException
	{
		super();
		this.stmt = stmt;
		if (stmt != null) {
			this.rs = stmt.executeQuery();
		}
	}

	public void close()
		throws RdbmsException
	{
		try {
			rs.close();
			stmt.close();
		}
		catch (SQLException e) {
			throw new RdbmsException(e);
		}
	}

	public T next()
		throws RdbmsException
	{
		try {
			if (rs.next()) {
				return convert(rs);
			}
			return null;
		}
		catch (SQLException e) {
			throw new RdbmsException(e);
		}
	}

	@Override
	public String toString() {
		return rs.toString();
	}

	protected abstract T convert(ResultSet rs)
		throws SQLException;

}
