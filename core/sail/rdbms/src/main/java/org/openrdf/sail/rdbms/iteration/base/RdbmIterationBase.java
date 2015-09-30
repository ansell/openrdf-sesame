/*******************************************************************************
 * Copyright (c) 2015, Eclipse Foundation, Inc. and its licensors.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * - Neither the name of the Eclipse Foundation, Inc. nor the names of its
 *   contributors may be used to endorse or promote products derived from this
 *   software without specific prior written permission. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
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
