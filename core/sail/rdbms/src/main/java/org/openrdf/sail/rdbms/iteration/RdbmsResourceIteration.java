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

/**
 * Converts a {@link ResultSet} into a {@link RdbmsResource} in an iteration.
 * 
 * @author James Leigh
 * 
 */
public class RdbmsResourceIteration extends RdbmIterationBase<RdbmsResource, SailException> {

	private RdbmsValueFactory vf;

	public RdbmsResourceIteration(RdbmsValueFactory vf, PreparedStatement stmt)
		throws SQLException
	{
		super(stmt);
		this.vf = vf;
	}

	@Override
	protected RdbmsResource convert(ResultSet rs)
		throws SQLException
	{
		Number id = rs.getLong(0 + 1);
		return vf.getRdbmsResource(id, rs.getString(0 + 2));
	}

	@Override
	protected RdbmsException convertSQLException(SQLException e) {
		return new RdbmsException(e);
	}

}
