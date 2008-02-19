/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.managers;

import java.sql.SQLException;
import java.util.Map;

import org.openrdf.sail.rdbms.managers.base.ValueManagerBase;
import org.openrdf.sail.rdbms.model.RdbmsBNode;
import org.openrdf.sail.rdbms.model.RdbmsResource;
import org.openrdf.sail.rdbms.schema.IdCode;
import org.openrdf.sail.rdbms.schema.ResourceTable;
import org.openrdf.sail.rdbms.schema.ResourceTable.HandleIdValue;

/**
 * Manages BNodes. Including creating, inserting, and looking up their
 * IDs.
 * 
 * @author James Leigh
 * 
 */
public class BNodeManager extends ValueManagerBase<String, RdbmsBNode> {
	private ResourceTable table;

	public BNodeManager(ResourceTable table) {
		super("bonde");
		this.table = table;
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
	public int getIdVersion() {
		return table.getIdVersion();
	}

	@Override
	protected int getBatchSize() {
		return table.getBatchSize();
	}

	@Override
	protected int getSelectChunkSize() {
		return table.getSelectChunkSize();
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
	protected void loadIds(final Map<String, RdbmsBNode> needIds) throws SQLException {
		final int version = table.getIdVersion();
		HandleIdValue handler = new HandleIdValue() {
			public void handleIdValue(long id, String value) {
				RdbmsResource res = needIds.get(value);
				if (res != null) {
					res.setInternalId(id);
					res.setVersion(version);
				}
			}
		};
		table.load(needIds.keySet(), handler);
	}

	@Override
	protected long nextId(RdbmsBNode value) {
		return table.nextId(IdCode.BNODE);
	}

}
