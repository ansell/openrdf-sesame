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

import org.openrdf.sail.helpers.DefaultSailChangedEvent;

/**
 * 
 * @author James Leigh
 */
public class TripleBatch extends Batch {

	public static int total_rows;

	public static int total_st;

	public static int total_wait;

	private TripleTable table;

	private DefaultSailChangedEvent sailChangedEvent;

	public void setTable(TripleTable table) {
		assert table != null;
		this.table = table;
	}

	public void setSailChangedEvent(DefaultSailChangedEvent sailChangedEvent) {
		this.sailChangedEvent = sailChangedEvent;
	}

	public boolean isReady() {
		return table.isReady();
	}

	public synchronized int flush()
		throws SQLException
	{
		table.blockUntilReady();
		long start = System.currentTimeMillis();
		int count = super.flush();
		long end = System.currentTimeMillis();
		total_rows += count;
		total_st += 2;
		total_wait += end - start;
		if (count > 0) {
			sailChangedEvent.setStatementsAdded(true);
		}
		return count;
	}

}
