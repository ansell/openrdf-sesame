/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.managers;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openrdf.sail.rdbms.managers.base.ValueManagerBase;
import org.openrdf.sail.rdbms.model.RdbmsResource;
import org.openrdf.sail.rdbms.model.RdbmsURI;
import org.openrdf.sail.rdbms.schema.IdCode;
import org.openrdf.sail.rdbms.schema.ResourceTable;
import org.openrdf.sail.rdbms.schema.ResourceTable.HandleIdValue;

/**
 * Manages URIs. Including creating, inserting, and looking up their
 * IDs.
 * 
 * @author James Leigh
 * 
 */
public class UriManager extends ValueManagerBase<String, RdbmsURI> {
	private ResourceTable shorter;
	private ResourceTable longer;

	public UriManager(ResourceTable shorter, ResourceTable longer) {
		super("uri");
		this.shorter = shorter;
		this.longer = longer;
	}

	@Override
	public void flushTable() throws SQLException {
		shorter.flush();
		longer.flush();
	}

	@Override
	public int getIdVersion() {
		return shorter.getIdVersion() + longer.getIdVersion();
	}

	@Override
	protected int getBatchSize() {
		return shorter.getBatchSize();
	}

	@Override
	protected int getSelectChunkSize() {
		return shorter.getSelectChunkSize();
	}

	@Override
	protected String key(RdbmsURI value) {
		return value.stringValue();
	}

	@Override
	protected void insert(long id, RdbmsURI resource) throws SQLException {
		String uri = resource.stringValue();
		if (IdCode.decode(id).isLong()) {
			longer.insert(id, uri);
		} else {
			shorter.insert(id, uri);
		}
	}

	@Override
	protected void loadIds(final Map<String, RdbmsURI> needIds) throws SQLException {
		List<String> small = new ArrayList<String>(needIds.size());
		List<String> big = new ArrayList<String>(needIds.size());
		for (String uri : needIds.keySet()) {
			if (uri.length() > IdCode.LONG) {
				big.add(uri);
			} else {
				small.add(uri);
			}
		}
		final int version = getIdVersion();
		HandleIdValue handler = new HandleIdValue() {
			public void handleIdValue(long id, String value) {
				RdbmsResource res = needIds.get(value);
				if (res != null) {
					res.setInternalId(id);
					res.setVersion(version);
				}
			}
		};
		shorter.load(small, handler);
		longer.load(big, handler);
	}

	@Override
	protected long nextId(RdbmsURI value) {
		if (value.stringValue().length() > IdCode.LONG)
			return longer.nextId(IdCode.URI_LONG);
		return shorter.nextId(IdCode.URI);
	}

}
