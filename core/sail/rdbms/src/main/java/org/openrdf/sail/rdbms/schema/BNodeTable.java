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
package org.openrdf.sail.rdbms.schema;

import java.sql.SQLException;

/**
 * Manages the rows in the BNode table.
 * 
 * @author James Leigh
 * 
 */
public class BNodeTable {

	private ValueTable table;

	public BNodeTable(ValueTable table) {
		super();
		this.table = table;
	}

	public void close()
		throws SQLException
	{
		table.close();
	}

	public String getName() {
		return table.getName();
	}

	public int getBatchSize() {
		return table.getBatchSize();
	}

	public void insert(Number id, String value)
		throws SQLException, InterruptedException
	{
		table.insert(id, value);
	}

	public boolean expunge(String condition)
		throws SQLException
	{
		return table.expunge(condition);
	}

	@Override
	public String toString() {
		return getName();
	}

	public void optimize()
		throws SQLException
	{
		table.optimize();
	}
}
