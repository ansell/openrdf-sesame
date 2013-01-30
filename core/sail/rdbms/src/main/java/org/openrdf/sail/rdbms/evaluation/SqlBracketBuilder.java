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
 * Prints round brackets in an SQL query.
 * 
 * @author James Leigh
 * 
 */
public class SqlBracketBuilder extends SqlExprBuilder {

	private SqlExprBuilder where;

	private String closing = ")";

	public SqlBracketBuilder(SqlExprBuilder where, QueryBuilderFactory factory) {
		super(factory);
		this.where = where;
		append("(");
	}

	public String getClosing() {
		return closing;
	}

	public void setClosing(String closing) {
		this.closing = closing;
	}

	public SqlExprBuilder close() {
		append(closing);
		where.append(toSql());
		where.addParameters(getParameters());
		return where;
	}
}
