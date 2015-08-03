/* 
 * Licensed to Aduna under one or more contributor license agreements.  
 * See the NOTICE.txt file distributed with this work for additional 
 * information regarding copyright ownership. 
 *
 * Aduna licenses this file to you under the terms of the Aduna BSD 
 * License (the "License"); you may not use this file except in compliance 
 * with the License. See the LICENSE.txt file distributed with this work 
 * for the full License.
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package org.openrdf.sail.rdbms.iteration.base;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import info.aduna.iteration.AbstractCloseableIteration;

/**
 * Base class for Iteration of a {@link ResultSet}.
 * 
 * @author James Leigh
 * 
 */
public abstract class RdbmIterationBase<T, X extends Exception> extends AbstractCloseableIteration<T, X> {

	private PreparedStatement stmt;

	private ResultSet rs;

	private boolean advanced;

	private boolean hasNext;

	public RdbmIterationBase(PreparedStatement stmt)
		throws SQLException
	{
		super();
		this.stmt = stmt;
		if (stmt != null) {
			this.rs = stmt.executeQuery();
		}
	}

	@Override
	protected void handleClose()
		throws X
	{
		super.handleClose();
		try {
			rs.close();
			stmt.close();
		}
		catch (SQLException e) {
			throw convertSQLException(e);
		}
	}

	public boolean hasNext()
		throws X
	{
		if (advanced)
			return hasNext;
		advanced = true;
		try {
			return hasNext = rs.next();
		}
		catch (SQLException e) {
			throw convertSQLException(e);
		}
	}

	public T next()
		throws X
	{
		try {
			if (!advanced) {
				hasNext = rs.next();
			}
			advanced = false;
			return convert(rs);
		}
		catch (SQLException e) {
			throw convertSQLException(e);
		}
	}

	public void remove()
		throws X
	{
		try {
			rs.rowDeleted();
		}
		catch (SQLException e) {
			throw convertSQLException(e);
		}
	}

	protected abstract T convert(ResultSet rs)
		throws SQLException;

	protected abstract X convertSQLException(SQLException e);
	
}
