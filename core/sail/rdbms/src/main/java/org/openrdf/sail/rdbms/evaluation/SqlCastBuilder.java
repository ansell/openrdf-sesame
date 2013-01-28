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
package org.openrdf.sail.rdbms.evaluation;

import java.sql.Types;

/**
 * Prints round brackets in an SQL query.
 * 
 * @author James Leigh
 * 
 */
public class SqlCastBuilder extends SqlExprBuilder {

	private SqlExprBuilder where;

	private int jdbcType;

	public SqlCastBuilder(SqlExprBuilder where, QueryBuilderFactory factory, int jdbcType) {
		super(factory);
		this.where = where;
		this.jdbcType = jdbcType;
		append(" CAST(");
	}

	public SqlExprBuilder close() {
		append(" AS ");
		append(getSqlType(jdbcType));
		append(")");
		where.append(toSql());
		where.addParameters(getParameters());
		return where;
	}

	protected CharSequence getSqlType(int type) {
		switch (type) {
			case Types.VARCHAR:
				return "VARCHAR";
			default:
				throw new AssertionError(type);
		}
	}
}
