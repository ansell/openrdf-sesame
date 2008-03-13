/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.managers;

import java.sql.SQLException;

import org.openrdf.sail.rdbms.managers.base.ValueManagerBase;
import org.openrdf.sail.rdbms.model.RdbmsURI;
import org.openrdf.sail.rdbms.schema.IdCode;
import org.openrdf.sail.rdbms.schema.URITable;

/**
 * Manages URIs. Including creating, inserting, and looking up their
 * IDs.
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
	public int getIdVersion() {
		return table.getIdVersion();
	}

	@Override
	public void close()
		throws SQLException
	{
		super.close();
		table.close();
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
	protected void insert(long id, RdbmsURI resource) throws SQLException, InterruptedException {
		table.insert(id, resource.stringValue());
	}

	@Override
	protected long getMissingId(RdbmsURI value) {
		return IdCode.valueOf(value).hash(value);
	}

	@Override
	protected void optimize() throws SQLException {
		table.optimize();
	}

}
