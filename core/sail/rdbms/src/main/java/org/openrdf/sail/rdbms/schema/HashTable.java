/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.schema;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;


/**
 *
 * @author James Leigh
 */
public class HashTable {
	private static final int CHUNK_SIZE = 15;
	private ValueTable table;
	private String select;
	private ConcurrentMap<IdCode, AtomicLong> seq = new ConcurrentHashMap<IdCode, AtomicLong>();

	public HashTable(ValueTable table) {
		super();
		this.table = table;
	}

	public String getName() {
		return table.getName();
	}

	public int getBatchSize() {
		return table.getBatchSize();
	}

	public int getSelectChunkSize() {
		return CHUNK_SIZE;
	}

	public void init() throws SQLException {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT id, value\nFROM ").append(getName());
		sb.append("\nWHERE value IN (");
		for (int i = 0, n = getSelectChunkSize(); i < n; i++) {
			sb.append("?,");
		}
		sb.setCharAt(sb.length() - 1, ')');
		select = sb.toString();
		for (long max : table.maxIds()) {
			IdCode code = IdCode.valueOf(max);
			if (max > code.minId()) {
				seq.put(code, new AtomicLong(max));
			}
		}
	}

	public void close()
		throws SQLException
	{
		table.close();
	}

	public long nextId(IdCode code) {
		if (!seq.containsKey(code)) {
			seq.putIfAbsent(code, new AtomicLong(code.minId()));
		}
		return seq.get(code).incrementAndGet();
	}

	public void insert(long id, long hash)
		throws SQLException, InterruptedException
	{
		synchronized (table) {
			HashBatch batch = (HashBatch)table.getValueBatch();
			if (table.isExpired(batch)) {
				batch = newHashBatch();
				table.initBatch(batch);
			}
			batch.addBatch(id, hash);
			table.queue(batch);
		}
	}

	public boolean expungeRemovedStatements(int count, String condition)
			throws SQLException {
		return table.expungeRemovedStatements(count, condition);
	}

	public void optimize()
		throws SQLException
	{
		table.optimize();
	}

	public String toString() {
		return table.toString();
	}

	protected HashBatch newHashBatch() {
		return new HashBatch();
	}

	protected PreparedStatement prepareSelect(Connection conn, String sql) throws SQLException {
		return conn.prepareStatement(sql);
	}

	public Map<Long,Long> load(Connection conn, Map<Long, Long> hashes) throws SQLException {
		assert !hashes.isEmpty();
		assert hashes.size() <= getSelectChunkSize();
		PreparedStatement stmt = prepareSelect(conn, select);
		try {
			int p = 0;
			for (Long hash : hashes.keySet()) {
				stmt.setLong(++p, hash);
			}
			while (p < getSelectChunkSize()) {
				stmt.setNull(++p, Types.BIGINT);
			}
			ResultSet rs = stmt.executeQuery();
			try {
				while (rs.next()) {
					long id = rs.getLong(1);
					long hash = rs.getLong(2);
					hashes.put(hash, id);
				}
			} finally {
				rs.close();
			}
		} finally {
			stmt.close();
		}
		return hashes;
	}

}
