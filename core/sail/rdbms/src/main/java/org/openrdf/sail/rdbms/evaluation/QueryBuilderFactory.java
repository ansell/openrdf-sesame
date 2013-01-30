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

import org.openrdf.sail.rdbms.RdbmsValueFactory;

/**
 * Creates the SQL query building components.
 * 
 * @author James Leigh
 * 
 */
public class QueryBuilderFactory {

	private RdbmsValueFactory vf;

	private boolean usingHashTable;

	public void setValueFactory(RdbmsValueFactory vf) {
		this.vf = vf;
	}

	public void setUsingHashTable(boolean b) {
		this.usingHashTable = b;
	}

	public QueryBuilder createQueryBuilder() {
		QueryBuilder query = new QueryBuilder(createSqlQueryBuilder());
		query.setValueFactory(vf);
		query.setUsingHashTable(usingHashTable);
		return query;
	}

	public SqlQueryBuilder createSqlQueryBuilder() {
		return new SqlQueryBuilder(this);
	}

	public SqlExprBuilder createSqlExprBuilder() {
		return new SqlExprBuilder(this);
	}

	public SqlRegexBuilder createSqlRegexBuilder(SqlExprBuilder where) {
		return new SqlRegexBuilder(where, this);
	}

	public SqlBracketBuilder createSqlBracketBuilder(SqlExprBuilder where) {
		return new SqlBracketBuilder(where, this);
	}

	public SqlJoinBuilder createSqlJoinBuilder(String table, String alias) {
		return new SqlJoinBuilder(table, alias, this);
	}

	public SqlCastBuilder createSqlCastBuilder(SqlExprBuilder where, int type) {
		return new SqlCastBuilder(where, this, type);
	}
}
