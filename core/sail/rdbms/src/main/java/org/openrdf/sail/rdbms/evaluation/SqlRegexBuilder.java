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

/**
 * Facilitates the building of a regular expression in SQL.
 * 
 * @author James Leigh
 * 
 */
public class SqlRegexBuilder {

	private SqlExprBuilder where;

	private SqlExprBuilder value;

	private SqlExprBuilder pattern;

	private SqlExprBuilder flags;

	public SqlRegexBuilder(SqlExprBuilder where, QueryBuilderFactory factory) {
		super();
		this.where = where;
		value = factory.createSqlExprBuilder();
		pattern = factory.createSqlExprBuilder();
		flags = factory.createSqlExprBuilder();
	}

	public SqlExprBuilder value() {
		return value;
	}

	public SqlExprBuilder pattern() {
		return pattern;
	}

	public SqlExprBuilder flags() {
		return flags;
	}

	public SqlExprBuilder close() {
		appendRegExp(where);
		return where;
	}

	protected void appendRegExp(SqlExprBuilder where) {
		where.append("REGEXP(");
		appendValue(where);
		where.append(", ");
		appendPattern(where);
		where.append(", ");
		appendFlags(where);
		where.append(")");
	}

	protected SqlExprBuilder appendValue(SqlExprBuilder where) {
		where.append(value.toSql());
		where.addParameters(value.getParameters());
		return where;
	}

	protected SqlExprBuilder appendPattern(SqlExprBuilder where) {
		where.append(pattern.toSql());
		where.addParameters(pattern.getParameters());
		return where;
	}

	protected SqlExprBuilder appendFlags(SqlExprBuilder where) {
		where.append(flags.toSql());
		where.addParameters(flags.getParameters());
		return where;
	}
}
