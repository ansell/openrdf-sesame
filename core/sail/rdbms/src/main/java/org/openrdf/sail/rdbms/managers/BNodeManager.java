/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.managers;

import java.sql.SQLException;

import org.openrdf.model.impl.BNodeImpl;
import org.openrdf.sail.rdbms.managers.base.ValueManagerBase;
import org.openrdf.sail.rdbms.model.RdbmsBNode;
import org.openrdf.sail.rdbms.schema.IdCode;
import org.openrdf.sail.rdbms.schema.ResourceTable;

/**
 * Manages BNodes. Including creating, inserting, and looking up their
 * IDs.
 * 
 * @author James Leigh
 * 
 */
public class BNodeManager extends ValueManagerBase<String, RdbmsBNode> {
	public static BNodeManager instance;
	private ResourceTable table;

	public BNodeManager(ResourceTable table) {
		this.table = table;
		instance = this;
	}

	@Override
	public int getIdVersion() {
		return table.getIdVersion();
	}

	@Override
	protected void flushTable() throws SQLException {
		table.flush();
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
	protected void insert(long id, RdbmsBNode resource) throws SQLException {
		table.insert(id, resource.stringValue());
	}

	@Override
	protected long getMissingId(RdbmsBNode value) {
		return IdCode.BNODE.getId(value.stringValue());
	}

	@Override
	protected RdbmsBNode createClosedSignal() {
		return new RdbmsBNode(new BNodeImpl(""));
	}

}
