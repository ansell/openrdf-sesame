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
package org.openrdf.sail.rdbms.iteration;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.openrdf.sail.SailException;
import org.openrdf.sail.rdbms.RdbmsValueFactory;
import org.openrdf.sail.rdbms.exceptions.RdbmsException;
import org.openrdf.sail.rdbms.iteration.base.RdbmIterationBase;
import org.openrdf.sail.rdbms.model.RdbmsResource;
import org.openrdf.sail.rdbms.model.RdbmsStatement;
import org.openrdf.sail.rdbms.model.RdbmsURI;
import org.openrdf.sail.rdbms.model.RdbmsValue;
import org.openrdf.sail.rdbms.schema.IdSequence;
import org.openrdf.sail.rdbms.schema.ValueTable;

/**
 * Converts a {@link ResultSet} into a {@link RdbmsStatement} in an iteration.
 * 
 * @author James Leigh
 * 
 */
public class RdbmsStatementIteration extends RdbmIterationBase<RdbmsStatement, SailException> {

	private RdbmsValueFactory vf;

	private IdSequence ids;

	public RdbmsStatementIteration(RdbmsValueFactory vf, PreparedStatement stmt, IdSequence ids)
		throws SQLException
	{
		super(stmt);
		this.vf = vf;
		this.ids = ids;
	}

	@Override
	protected RdbmsStatement convert(ResultSet rs)
		throws SQLException
	{
		RdbmsResource ctx = createResource(rs, 1);
		RdbmsResource subj = createResource(rs, 3);
		RdbmsURI pred = (RdbmsURI)createResource(rs, 5);
		RdbmsValue obj = createValue(rs, 7);
		return new RdbmsStatement(subj, pred, obj, ctx);
	}

	@Override
	protected RdbmsException convertSQLException(SQLException e) {
		return new RdbmsException(e);
	}

	private RdbmsResource createResource(ResultSet rs, int index)
		throws SQLException
	{
		Number id = ids.idOf(rs.getLong(index));
		if (id.longValue() == ValueTable.NIL_ID)
			return null;
		String stringValue = rs.getString(index + 1);
		return vf.getRdbmsResource(id, stringValue);
	}

	private RdbmsValue createValue(ResultSet rs, int index)
		throws SQLException
	{
		Number id = ids.idOf(rs.getLong(index));
		if (ids.isLiteral(id)) {
			String label = rs.getString(index + 1);
			String datatype = rs.getString(index + 2);
			String language = rs.getString(index + 3);
			return vf.getRdbmsLiteral(id, label, language, datatype);
		}
		return createResource(rs, index);
	}

}
