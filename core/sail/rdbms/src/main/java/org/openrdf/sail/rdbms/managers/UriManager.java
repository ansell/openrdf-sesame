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
package org.openrdf.sail.rdbms.managers;

import java.sql.SQLException;

import org.openrdf.sail.rdbms.managers.base.ValueManagerBase;
import org.openrdf.sail.rdbms.model.RdbmsURI;
import org.openrdf.sail.rdbms.schema.URITable;

/**
 * Manages URIs. Including creating, inserting, and looking up their IDs.
 * 
 * @author James Leigh
 * 
 */
public class UriManager extends ValueManagerBase<RdbmsURI> {

	public static UriManager instance;

	private URITable table;

	public UriManager() {
		instance = this;
	}

	public void setUriTable(URITable shorter) {
		this.table = shorter;
	}

	@Override
	public void close()
		throws SQLException
	{
		super.close();
		if (table != null) {
			table.close();
		}
	}

	@Override
	protected boolean expunge(String condition)
		throws SQLException
	{
		return table.expunge(condition);
	}

	@Override
	protected int getBatchSize() {
		return table.getBatchSize();
	}

	@Override
	protected String key(RdbmsURI value) {
		return value.stringValue();
	}

	@Override
	protected void insert(Number id, RdbmsURI resource)
		throws SQLException, InterruptedException
	{
		String uri = resource.stringValue();
		if (getIdSequence().isLong(id)) {
			table.insertLong(id, uri);
		}
		else {
			table.insertShort(id, uri);
		}
	}

	@Override
	protected void optimize()
		throws SQLException
	{
		super.optimize();
		table.optimize();
	}

}
