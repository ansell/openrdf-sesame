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
 * Manages the rows in the URI table.
 * 
 * @author James Leigh
 * 
 */
public class URITable {

	private ValueTable shorter;

	private ValueTable longer;

	private int version;

	public URITable(ValueTable shorter, ValueTable longer) {
		super();
		this.shorter = shorter;
		this.longer = longer;
	}

	public void close()
		throws SQLException
	{
		shorter.close();
		longer.close();
	}

	public int getBatchSize() {
		return shorter.getBatchSize();
	}

	public int getVersion() {
		return version;
	}

	public String getShortTableName() {
		return shorter.getName();
	}

	public String getLongTableName() {
		return longer.getName();
	}

	public void insertShort(Number id, String value)
		throws SQLException, InterruptedException
	{
		shorter.insert(id, value);
	}

	public void insertLong(Number id, String value)
		throws SQLException, InterruptedException
	{
		longer.insert(id, value);
	}

	public boolean expunge(String condition)
		throws SQLException
	{
		boolean bool = false;
		bool |= shorter.expunge(condition);
		bool |= longer.expunge(condition);
		return bool;
	}

	@Override
	public String toString() {
		return shorter.getName() + " UNION ALL " + longer.getName();
	}

	public void optimize()
		throws SQLException
	{
		shorter.optimize();
		longer.optimize();
	}
}
