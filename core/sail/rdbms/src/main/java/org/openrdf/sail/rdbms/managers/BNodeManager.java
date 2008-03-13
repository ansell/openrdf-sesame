/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.managers;

import java.sql.SQLException;

import org.openrdf.sail.rdbms.managers.base.ValueManagerBase;
import org.openrdf.sail.rdbms.model.RdbmsBNode;
import org.openrdf.sail.rdbms.schema.BNodeTable;
import org.openrdf.sail.rdbms.schema.IdCode;

/**
 * Manages BNodes. Including creating, inserting, and looking up their
 * IDs.
 * 
 * @author James Leigh
 * 
 */
public class BNodeManager extends ValueManagerBase<RdbmsBNode> {
	public static BNodeManager instance;
	private BNodeTable table;

	public BNodeManager() {
		instance = this;
	}

	public void setTable(BNodeTable table) {
		this.table = table;
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
	protected void optimize() throws SQLException {
		table.optimize();
	}

	@Override
	protected int getBatchSize() {
		return table.getBatchSize();
	}

	@Override
	protected String key(RdbmsBNode value) {
		return value.stringValue();
	}

	@Override
	protected void insert(long id, RdbmsBNode resource) throws SQLException, InterruptedException {
		table.insert(id, IdCode.BNODE.hash(resource), resource.stringValue());
	}

	@Override
	protected long getMissingId(RdbmsBNode value) {
		return IdCode.BNODE.hash(value);
	}

}
