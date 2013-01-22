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

import java.sql.Connection;

import org.openrdf.sail.rdbms.schema.TripleTable;
import org.openrdf.sail.rdbms.schema.ValueTableFactory;

/**
 * Overrides PostgreSQL specific table commands.
 * 
 * @author James Leigh
 * 
 */
public class PgSqlValueTableFactory extends ValueTableFactory {

	public PgSqlValueTableFactory() {
		super(new PgSqlTableFactory());
	}

	@Override
	public PgSqlValueTable newValueTable() {
		return new PgSqlValueTable();
	}

	@Override
	public TripleTable createTripleTable(Connection conn, String tableName) {
		return super.createTripleTable(conn, tableName.toLowerCase());
	}
}
