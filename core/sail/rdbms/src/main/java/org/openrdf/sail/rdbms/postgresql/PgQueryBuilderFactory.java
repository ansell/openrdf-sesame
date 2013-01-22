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
package org.openrdf.sail.rdbms.postgresql;

import org.openrdf.sail.rdbms.evaluation.QueryBuilderFactory;
import org.openrdf.sail.rdbms.evaluation.SqlExprBuilder;
import org.openrdf.sail.rdbms.evaluation.SqlQueryBuilder;
import org.openrdf.sail.rdbms.evaluation.SqlRegexBuilder;

/**
 * Overrides PostgreSQL specific SQL syntax. Including regular expression
 * operator, LIMIT clause and CROSS JOIN notation.
 * 
 * @author Jeen Broekstra
 * @author James Leigh
 * 
 */
public class PgQueryBuilderFactory extends QueryBuilderFactory {

	@Override 
	public SqlQueryBuilder createSqlQueryBuilder() {
		return new PgSqlQueryBuilder(this);
	}
	
	@Override
	public SqlRegexBuilder createSqlRegexBuilder(SqlExprBuilder where) {
		return new SqlRegexBuilder(where, this) {

			@Override
			protected void appendRegExp(SqlExprBuilder where) {
				appendValue(where);
				where.append(" ~ ");
				appendPattern(where);
			}
		};
	}
}
