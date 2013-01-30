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
package org.openrdf.sail.rdbms.mysql;

import java.sql.SQLException;

import org.openrdf.sail.rdbms.schema.RdbmsTable;

public class MySqlTable extends RdbmsTable {

	public MySqlTable(String name) {
		super(name);
	}

	@Override
	protected String buildCreateTransactionalTable(CharSequence columns) {
		StringBuilder sb = new StringBuilder();
		sb.append("CREATE TABLE ").append(getName());
		sb.append(" (\n").append(columns).append(")");
		sb.append(" ENGINE = InnoDB");
		return sb.toString();
	}

	@Override
	protected String buildLongIndex(String... columns) {
		StringBuilder sb = new StringBuilder();
		sb.append("CREATE INDEX ").append(buildIndexName(columns));
		sb.append(" ON ").append(getName());
		sb.append(" (value(1024))");
		return sb.toString();
	}

	@Override
	protected String buildOptimize()
		throws SQLException
	{
		return "OPTIMIZE TABLE " + getName();
	}

	@Override
	protected String buildDropIndex(String name) {
		StringBuilder sb = new StringBuilder();
		sb.append("DROP INDEX ").append(name);
		sb.append(" ON ").append(getName());
		return sb.toString();
	}

}
