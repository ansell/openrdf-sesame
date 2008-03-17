/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.managers;


import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openrdf.sail.rdbms.managers.base.ManagerBase;
import org.openrdf.sail.rdbms.model.RdbmsBNode;
import org.openrdf.sail.rdbms.model.RdbmsLiteral;
import org.openrdf.sail.rdbms.model.RdbmsURI;
import org.openrdf.sail.rdbms.model.RdbmsValue;
import org.openrdf.sail.rdbms.schema.Batch;
import org.openrdf.sail.rdbms.schema.HashBatch;
import org.openrdf.sail.rdbms.schema.HashTable;
import org.openrdf.sail.rdbms.schema.IdSequence;

/**
 * 
 * @author James Leigh
 */
public class HashManager extends ManagerBase {
	public static HashManager instance;
	private Logger logger = LoggerFactory.getLogger(HashManager.class);
	private Connection conn;
	private HashTable table;
	private Map<Long, Number> ids;
	private AtomicInteger version = new AtomicInteger();
	private BNodeManager bnodes;
	private UriManager uris;
	private LiteralManager literals;
	private Thread lookupThread;
	private Object working = new Object();
	private BlockingQueue<RdbmsValue> queue;
	private IdSequence idseq; 
	Exception exc;
	RdbmsValue closeSignal = new RdbmsValue() {
		private static final long serialVersionUID = -2211413309013905712L;

		public String stringValue() {
			return null;
		}};

	public HashManager() {
		instance = this;
	}

	public void setHashTable(HashTable table) {
		this.table = table;
		ids = new HashMap<Long, Number>(table.getBatchSize());
	}

	public void setConnection(Connection conn) {
		this.conn = conn;
	}

	public void setBNodeManager(BNodeManager bnodeTable) {
		this.bnodes = bnodeTable;
	}

	public void setLiteralManager(LiteralManager literalTable) {
		this.literals = literalTable;
	}

	public void setUriManager(UriManager uriTable) {
		this.uris = uriTable;
	}

	public void setIdSequence(IdSequence idseq) {
		this.idseq = idseq;
	}

	public void init() {
		queue = new ArrayBlockingQueue<RdbmsValue>(table.getBatchSize());
		lookupThread = new Thread(new Runnable() {
			public void run() {
				try {
					lookupThread(working);
				} catch (Exception e) {
					exc = e;
					logger.error(e.toString(), e);
				}
			}
		}, "id-lookup");
		lookupThread.start();
	}

	@Override
	public void close()
		throws SQLException
	{
		try {
			flush();
			if (lookupThread != null) {
				queue.put(closeSignal);
				lookupThread.join();
			}
		} catch (InterruptedException e) {
			logger.warn(e.toString(), e);
		}
		super.close();
		table.close();
	}

	public int getIdVersion() {
		return version.intValue();
	}

	public void optimize()
		throws SQLException
	{
		table.optimize();
	}

	public boolean removedStatements(int count, String condition) throws SQLException {
		if (table.expungeRemovedStatements(count, condition)) {
			version.addAndGet(1);
			return true;
		}
		return false;
	}

	public void lookupId(RdbmsValue value) throws InterruptedException {
		queue.put(value);
	}

	public void assignId(RdbmsValue value, int version)
		throws InterruptedException, SQLException
	{
		synchronized (working) {
			throwException();
			if (value.isExpired(version)) {
				List<RdbmsValue> values = new ArrayList<RdbmsValue>(getChunkSize());
				Map<Long, Number> map = new HashMap<Long, Number>(getChunkSize());
				values.add(value);
				assignIds(values, map);
			}
		}
	}

	@Override
	public void flush()
		throws SQLException, InterruptedException
	{
		synchronized (working) {
			throwException();
			List<RdbmsValue> values = new ArrayList<RdbmsValue>(getChunkSize());
			Map<Long, Number> map = new HashMap<Long, Number>(getChunkSize());
			RdbmsValue taken = queue.poll();
			while (taken != null) {
				values.add(taken);
				assignIds(values, map);
				values.clear();
				taken = queue.poll();
				if (taken == closeSignal) {
					queue.add(taken);
					taken = null;
				}
			}
		}
		super.flush();
	}

	protected int getChunkSize() {
		return table.getSelectChunkSize();
	}

	@Override
	protected void flush(Batch batch)
		throws SQLException
	{
		super.flush(batch);
		synchronized (working) {
			synchronized (ids) {
				HashBatch hb = (HashBatch) batch;
				for (Long hash : hb.getHashes()) {
					ids.remove(hash);
				}
			}
		}
	}

	void lookupThread(Object working)
		throws InterruptedException, SQLException
	{
		List<RdbmsValue> values = new ArrayList<RdbmsValue>(getChunkSize());
		Map<Long, Number> map = new HashMap<Long, Number>(getChunkSize());
		RdbmsValue taken = queue.take();
		for (; taken != closeSignal; taken = queue.take()) {
			synchronized (working) {
				values.add(taken);
				assignIds(values, map);
				values.clear();
			}
		}
	}

	private void assignIds(List<RdbmsValue> values, Map<Long, Number> map)
		throws SQLException, InterruptedException
	{
		while (values.size() < getChunkSize()) {
			RdbmsValue taken = queue.poll();
			if (taken == closeSignal) {
				queue.add(taken);
				break;
			}
			if (taken == null)
				break;
			values.add(taken);
		}
		Map<Long, Number> existing = lookup(values, map);
		for (RdbmsValue value : values) {
			Long hash = idseq.hashOf(value);
			if (existing.get(hash) != null) {
				// already in database
				value.setInternalId(existing.get(hash));
				value.setVersion(getIdVersion(value));
			}
			else {
				synchronized (ids) {
					if (ids.containsKey(hash)) {
						// already inserting this value
						value.setInternalId(ids.get(hash));
						value.setVersion(getIdVersion(value));
					}
					else {
						// new id to be inserted
						Number id = idseq.nextId(value);
						value.setInternalId(id);
						value.setVersion(getIdVersion(value));
						ids.put(hash, id);
						table.insert(id, hash);
						insert(id, value);
					}
				}
			}
		}
	}

	private Map<Long, Number> lookup(Collection<RdbmsValue> values, Map<Long, Number> map) throws SQLException {
		assert !values.isEmpty();
		assert values.size() <= getChunkSize();
		map.clear();
		for (RdbmsValue value : values) {
			map.put(idseq.hashOf(value), null);
		}
		return table.load(conn, map);
	}

	private Integer getIdVersion(RdbmsValue value) {
		if (value instanceof RdbmsLiteral)
			return literals.getIdVersion();
		if (value instanceof RdbmsURI)
			return uris.getIdVersion();
		assert value instanceof RdbmsBNode;
		return bnodes.getIdVersion();
	}

	private void insert(Number id, RdbmsValue value)
		throws SQLException, InterruptedException
	{
		if (value instanceof RdbmsLiteral) {
			literals.insert(id, (RdbmsLiteral)value);
		}
		else if (value instanceof RdbmsURI) {
			uris.insert(id, (RdbmsURI)value);
		}
		else {
			assert value instanceof RdbmsBNode;
			bnodes.insert(id, (RdbmsBNode)value);
		}
	}

	private void throwException()
		throws SQLException
	{
		if (exc instanceof SQLException) {
			SQLException e = (SQLException)exc;
			exc = null;
			throw e;
		}
		else if (exc instanceof RuntimeException) {
			RuntimeException e = (RuntimeException)exc;
			exc = null;
			throw e;
		}
	}

}
