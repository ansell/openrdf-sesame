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

import org.openrdf.sail.rdbms.RdbmsConnectionFactory;
import org.openrdf.sail.rdbms.RdbmsProvider;

/**
 * Checks the database product name and version to be compatible with this
 * Sesame store.
 * 
 * @author James Leigh
 * 
 */
public class PgSqlProvider implements RdbmsProvider {

	public RdbmsConnectionFactory createRdbmsConnectionFactory(String dbName, String dbVersion) {
		if ("PostgreSQL".equalsIgnoreCase(dbName))
			return new PgSqlConnectionFactory();
		return null;
	}

}
