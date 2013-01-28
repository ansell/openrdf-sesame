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
 * 
 * @author James Leigh
 */
public class ValueBatch extends Batch {

	public static int total_rows;

	public static int total_st;

	private RdbmsTable table;

	public void setTable(RdbmsTable table) {
		assert table != null;
		this.table = table;
	}

	public synchronized int flush()
		throws SQLException
	{
		synchronized (table) {
			int count = super.flush();
			total_rows += count;
			total_st += 2;
			table.modified(count, 0);
			return count;
		}
	}

}
