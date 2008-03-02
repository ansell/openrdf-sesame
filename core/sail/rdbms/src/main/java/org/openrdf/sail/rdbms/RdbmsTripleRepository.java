/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Lock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.sail.SailException;
import org.openrdf.sail.helpers.DefaultSailChangedEvent;
import org.openrdf.sail.rdbms.evaluation.QueryBuilderFactory;
import org.openrdf.sail.rdbms.evaluation.SqlBracketBuilder;
import org.openrdf.sail.rdbms.evaluation.SqlJoinBuilder;
import org.openrdf.sail.rdbms.evaluation.SqlQueryBuilder;
import org.openrdf.sail.rdbms.exceptions.RdbmsException;
import org.openrdf.sail.rdbms.iteration.EmptyRdbmsResourceIteration;
import org.openrdf.sail.rdbms.iteration.EmptyRdbmsStatementIteration;
import org.openrdf.sail.rdbms.iteration.RdbmsResourceIteration;
import org.openrdf.sail.rdbms.iteration.RdbmsStatementIteration;
import org.openrdf.sail.rdbms.model.RdbmsResource;
import org.openrdf.sail.rdbms.model.RdbmsStatement;
import org.openrdf.sail.rdbms.model.RdbmsURI;
import org.openrdf.sail.rdbms.model.RdbmsValue;
import org.openrdf.sail.rdbms.schema.LiteralTable;
import org.openrdf.sail.rdbms.schema.TripleTableManager;
import org.openrdf.sail.rdbms.schema.ResourceTable;
import org.openrdf.sail.rdbms.schema.TransTableManager;
import org.openrdf.sail.rdbms.schema.ValueTable;

/**
 * Facade to {@link TransTableManager}, {@link ResourceTable}, and
 * {@link LiteralTable} for adding, removing, and retrieving statements from the
 * database.
 * 
 * @author James Leigh
 * 
 */
public class RdbmsTripleRepository {
	public static int MIN_BATCH_SIZE = 128;
	public static int BATCH_SIZE = TransTableManager.BATCH_SIZE;
	public static int MAX_THREADS = Math.max(3, TripleTableManager.MAX_TABLES + 1);
	public static int MAX_QUEUE = (MAX_THREADS * 2 + 1) * BATCH_SIZE;
	public static int FLUSH_EVERY = 100000;
	public static int COMMIT_EVERY = Integer.MAX_VALUE;//20000;
	private Logger logger = LoggerFactory.getLogger(RdbmsTripleRepository.class);
	private Connection conn;
	private RdbmsValueFactory vf;
	private TransTableManager statements;
	private QueryBuilderFactory factory;
	private ResourceTable bnodes;
	private ResourceTable uris;
	private ResourceTable longUris;
	private LiteralTable literals;
	private Lock readLock;
	private DefaultSailChangedEvent sailChangedEvent;
	private BlockingQueue<RdbmsStatement> queue = new ArrayBlockingQueue(MAX_QUEUE);
	private int addedSinceFlush;
	private int addedSinceCommit;
	private List<Object> workThread = new CopyOnWriteArrayList<Object>();
	private List<Thread> threads = new CopyOnWriteArrayList<Thread>();
	Exception exc;
	RdbmsStatement closedSignal;
	{
		RdbmsURI uri = new RdbmsURI(new URIImpl("urn:closedSignal"));
		closedSignal = new RdbmsStatement(uri, uri, uri);
	}

	public Connection getConnection() {
		return conn;
	}

	public void setConnection(Connection conn) {
		this.conn = conn;
	}

	public RdbmsValueFactory getValueFactory() {
		return vf;
	}

	public void setValueFactory(RdbmsValueFactory vf) {
		this.vf = vf;
	}

	public DefaultSailChangedEvent getSailChangedEvent() {
		return sailChangedEvent;
	}

	public void setSailChangedEvent(DefaultSailChangedEvent sailChangedEvent) {
		this.sailChangedEvent = sailChangedEvent;
	}

	public void setQueryBuilderFactory(QueryBuilderFactory factory) {
		this.factory = factory;
	}

	public void setBNodeTable(ResourceTable bnodes) {
		this.bnodes = bnodes;
	}

	public void setURITable(ResourceTable uris) {
		this.uris = uris;
	}

	public void setLongUriTable(ResourceTable longUris) {
		this.longUris = longUris;
	}

	public void setLiteralTable(LiteralTable literals) {
		this.literals = literals;
	}

	public void setTransaction(TransTableManager temporary) {
		this.statements = temporary;

	}

	public void flush() throws RdbmsException {
		flushQueue();
		vf.flush();
	}

	public void add(RdbmsStatement st) throws SailException, SQLException {
		if (readLock == null) {
			readLock = vf.getIdReadLock();
			readLock.lock();
		}
		if (queue.size() >= MIN_BATCH_SIZE && workThread.isEmpty() && workThread.size() < MAX_THREADS) {
			Object lock = new Object();
			workThread.add(lock);
			threads.add(startInsentThread(lock, true));
		} else if (queue.size() >= 2 * MIN_BATCH_SIZE && workThread.size() < MAX_THREADS) {
			Object lock = new Object();
			workThread.add(lock);
			threads.add(startInsentThread(lock, false));
		} else if (isTimeToCommit()) {
			logger.warn("TRANSACTION TOO LONG - forcing commit");
			commit();
			begin();
		}
		try {
			queue.put(st);
			addedSinceFlush++;
			addedSinceCommit++;
		}
		catch (InterruptedException e) {
			throw new RdbmsException(e);
		}
	}

	protected boolean isTimeToCommit() throws SQLException {
		return addedSinceCommit > COMMIT_EVERY;
	}

	public synchronized void begin() throws SQLException {
		conn.setAutoCommit(false);
	}

	public synchronized void close() throws SQLException {
		queue.clear();
		try {
			for (int i = 0, n = threads.size(); i < n; i++) {
				queue.put(closedSignal);
			}
			for (Thread thread : threads) {
				thread.join();
			}
		}
		catch (InterruptedException e) {
			logger.warn(e.toString(), e);
		}
		statements.close();
		if (!conn.getAutoCommit()) {
			conn.rollback();
		}
		conn.setAutoCommit(true);
		conn.close();
	}

	public synchronized void commit() throws SQLException, RdbmsException {
		flushQueue();
		conn.commit();
		conn.setAutoCommit(true);
		addedSinceCommit = 0;
		if (readLock != null) {
			readLock.unlock();
			readLock = null;
		}
		Lock writeLock = vf.getIdWriteLock();
		boolean locked = writeLock.tryLock();
		try {
			if (locked) {
				vf.flush();
			}
			statements.committed(locked);
		} finally {
			if (locked) {
				writeLock.unlock();
			}
		}
	}

	public RdbmsStatementIteration find(Resource subj, URI pred, Value obj,
			Resource... ctxs) throws RdbmsException {
		try {
			RdbmsResource s = vf.asRdbmsResource(subj);
			RdbmsURI p = vf.asRdbmsURI(pred);
			RdbmsValue o = vf.asRdbmsValue(obj);
			RdbmsResource[] c = vf.asRdbmsResource(ctxs);
			flush();
			SqlQueryBuilder query = buildSelectQuery(s, p, o, c);
			if (query == null)
				return new EmptyRdbmsStatementIteration();
			List<?> parameters = query.findParameters(new ArrayList<Object>());
			PreparedStatement stmt = conn.prepareStatement(query.toString());
			try {
				for (int i = 0, n = parameters.size(); i < n; i++) {
					stmt.setObject(i + 1, parameters.get(i));
				}
				return new RdbmsStatementIteration(vf, stmt);
			} catch (SQLException e) {
				stmt.close();
				throw e;
			}
		} catch (SQLException e) {
			throw new RdbmsException(e);
		}

	}

	public RdbmsResourceIteration findContexts() throws SQLException,
			RdbmsException {
		flush();
		String qry = buildContextQuery();
		if (qry == null)
			return new EmptyRdbmsResourceIteration();
		PreparedStatement stmt = conn.prepareStatement(qry);
		try {
			return new RdbmsResourceIteration(vf, stmt);
		} catch (SQLException e) {
			stmt.close();
			throw e;
		}
	}

	public boolean isClosed() throws SQLException {
		return conn.isClosed();
	}

	public int remove(Resource subj, URI pred, Value obj, Resource... ctxs)
			throws RdbmsException {
		RdbmsResource s = vf.asRdbmsResource(subj);
		RdbmsURI p = vf.asRdbmsURI(pred);
		RdbmsValue o = vf.asRdbmsValue(obj);
		RdbmsResource[] c = vf.asRdbmsResource(ctxs);
		flush();
		try {
			Collection<Long> predicates;
			if (p == null) {
				predicates = statements.getPredicateIds();
			} else {
				predicates = Collections.singleton(vf.getInternalId(p));
			}
			int total = 0;
			for (Long id : predicates) {
				String tableName = statements.findTableName(id);
				if (!statements.isPredColumnPresent(id)) {
					p = null;
				}
				String query = buildDeleteQuery(tableName, s, p, o, c);
				PreparedStatement stmt = conn.prepareStatement(query);
				try {
					setSelectQuery(stmt, s, p, o, c);
					int count = stmt.executeUpdate();
					statements.removed(id, count);
					total += count;
				} finally {
					stmt.close();
				}
			}
			if (total > 0) {
				sailChangedEvent.setStatementsRemoved(true);
			}
			return total;
		} catch (SQLException e) {
			throw new RdbmsException(e);
		}
	}

	public void rollback() throws SQLException, SailException {
		synchronized(queue) {
			queue.clear();
		}
		conn.rollback();
		conn.setAutoCommit(true);
		addedSinceCommit = 0;
		if (readLock != null) {
			readLock.unlock();
			readLock = null;
		}
	}

	public long size(RdbmsResource... ctxs) throws SQLException, SailException {
		flush();
		String qry = buildCountQuery(ctxs);
		if (qry == null)
			return 0;
		PreparedStatement stmt = conn.prepareStatement(qry);
		try {
			setCountQuery(stmt, ctxs);
			ResultSet rs = stmt.executeQuery();
			try {
				if (rs.next())
					return rs.getLong(1);
				throw new RdbmsException("Could not determine size");
			} finally {
				rs.close();
			}
		} finally {
			stmt.close();
		}
	}

	void insertThread(Object working, boolean primary) throws InterruptedException, RdbmsException,
			SQLException {
		String name = Thread.currentThread().getName();
		logger.debug("Starting helper thread {}", name);
		while (true) {
			RdbmsStatement taken = queue.take();
			if (taken == closedSignal)
				break;
			synchronized (working) {
				insert(taken);
				if (primary && addedSinceFlush >= FLUSH_EVERY) {
					addedSinceFlush = 0;
					if (statements.flushUnpopular() > 0) {
						sailChangedEvent.setStatementsAdded(true);
					}
				}
			}
		}
		logger.debug("Closing helper thread {}", name);
	}

	private Thread startInsentThread(final Object lock, final boolean primary) {
		String currentName = Thread.currentThread().getName();
		String name = currentName + "-statement-insert-" + workThread.size();
		Thread insertThread = new Thread(new Runnable() {
			public void run() {
				try {
					insertThread(lock, primary);
				} catch (Exception e) {
					exc = e;
					logger.error(e.toString(), e);
				}
			}
		}, name);
		insertThread.start();
		return insertThread;
	}

	private void flushQueue() throws RdbmsException {
		try {
			for (RdbmsStatement st = queue.poll(); st != null; st = queue.poll()) {
				insert(st);
			}
			for (Object o : workThread) {
				synchronized (o) {
					// wait for thread to finish
				}
			}
			addedSinceFlush = 0;
			if (statements.flush() > 0) {
				sailChangedEvent.setStatementsAdded(true);
			}
		} catch (SQLException e) {
			throw new RdbmsException(e);
		}
	}

	private String buildContextQuery() throws SQLException {
		if (statements.isEmpty())
			return null;
		String tableName = statements.getCombinedTableName();
		SqlQueryBuilder query = factory.createSqlQueryBuilder();
		query.select().column("t", "ctx");
		query
				.select()
				.append(
						"CASE WHEN MIN(u.value) IS NOT NULL THEN MIN(u.value) ELSE MIN(b.value) END");
		SqlJoinBuilder join = query.from(tableName, "t");
		join.leftjoin(bnodes.getName(), "b").on("id", "t.ctx");
		join.leftjoin(uris.getName(), "u").on("id", "t.ctx");
		SqlBracketBuilder open = query.filter().and().open();
		open.column("u", "value").isNotNull();
		open.or();
		open.column("b", "value").isNotNull();
		open.close();
		query.groupBy("t.ctx");
		return query.toString();
	}

	private String buildCountQuery(RdbmsResource... ctxs) throws SQLException {
		String tableName = statements.getCombinedTableName();
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT COUNT(*) FROM ");
		sb.append(tableName).append(" t");
		if (ctxs != null && ctxs.length > 0) {
			sb.append("\nWHERE ");
			for (int i = 0; i < ctxs.length; i++) {
				sb.append("t.ctx = ?");
				if (i < ctxs.length - 1) {
					sb.append(" OR ");
				}
			}
		}
		return sb.toString();
	}

	private String buildDeleteQuery(String tableName, RdbmsResource subj,
			RdbmsURI pred, RdbmsValue obj, RdbmsResource... ctxs)
			throws RdbmsException, SQLException {
		StringBuilder sb = new StringBuilder();
		sb.append("DELETE FROM ").append(tableName);
		return buildWhere(sb, subj, pred, obj, ctxs);
	}

	private SqlQueryBuilder buildSelectQuery(RdbmsResource subj, RdbmsURI pred,
			RdbmsValue obj, RdbmsResource... ctxs) throws RdbmsException,
			SQLException {
		String tableName = statements.getTableName(vf.getInternalId(pred));
		SqlQueryBuilder query = factory.createSqlQueryBuilder();
		query.select().column("t", "ctx");
		query.select().append("CASE WHEN cu.value IS NOT NULL THEN cu.value WHEN clu.value IS NOT NULL THEN clu.value ELSE cb.value END");
		query.select().column("t", "subj");
		query.select().append("CASE WHEN su.value IS NOT NULL THEN su.value WHEN slu.value IS NOT NULL THEN slu.value ELSE sb.value END");
		query.select().column("pu", "id");
		query.select().column("pu", "value");
		query.select().column("t", "obj");
		query.select().append("CASE WHEN ou.value IS NOT NULL THEN ou.value" +
				" WHEN olu.value IS NOT NULL THEN olu.value" +
				" WHEN ob.value IS NOT NULL THEN ob.value" +
				" WHEN ol.value IS NOT NULL THEN ol.value ELSE oll.value END");
		query.select().column("od", "value");
		query.select().column("og", "value");
		SqlJoinBuilder join;
		if (pred != null) {
			join = query.from(uris.getName(), "pu");
			// TODO what about long predicate URIs? 
			join = join.join(tableName, "t");
		} else {
			join = query.from(tableName, "t");
		}
		if (pred == null) {
			join.join(uris.getName(), "pu").on("id", "t.pred");
		}
		join.leftjoin(uris.getName(), "cu").on("id", "t.ctx");
		join.leftjoin(longUris.getName(), "clu").on("id", "t.ctx");
		join.leftjoin(bnodes.getName(), "cb").on("id", "t.ctx");
		join.leftjoin(uris.getName(), "su").on("id", "t.subj");
		join.leftjoin(longUris.getName(), "slu").on("id", "t.subj");
		join.leftjoin(bnodes.getName(), "sb").on("id", "t.subj");
		join.leftjoin(uris.getName(), "ou").on("id", "t.obj");
		join.leftjoin(longUris.getName(), "olu").on("id", "t.obj");
		join.leftjoin(bnodes.getName(), "ob").on("id", "t.obj");
		join.leftjoin(literals.getLabelTable().getName(), "ol").on("id", "t.obj");
		join.leftjoin(literals.getLongLabelTable().getName(), "oll").on("id", "t.obj");
		join.leftjoin(literals.getLanguageTable().getName(), "og").on("id", "t.obj");
		join.leftjoin(literals.getDatatypeTable().getName(), "od").on("id", "t.obj");
		if (ctxs != null && ctxs.length > 0) {
			Long[] ids = new Long[ctxs.length];
			for (int i = 0; i < ids.length; i++) {
				ids[i] = vf.getInternalId(ctxs[i]);
			}
			query.filter().and().columnIn("t", "ctx", ids);
		}
		if (subj != null) {
			long id = vf.getInternalId(subj);
			query.filter().and().columnEquals("t", "subj", id);
		}
		if (pred != null) {
			long id = vf.getInternalId(pred);
			query.filter().and().columnEquals("pu", "id", id);
			if (statements.isPredColumnPresent(id)) {
				query.filter().and().columnEquals("t", "pred", id);
			}
		}
		if (obj != null) {
			long id = vf.getInternalId(obj);
			query.filter().and().columnEquals("t", "obj", id);
		}
		return query;
	}

	private String buildWhere(StringBuilder sb, RdbmsResource subj,
			RdbmsURI pred, RdbmsValue obj, RdbmsResource... ctxs) {
		sb.append("\nWHERE 1=1");
		if (ctxs != null && ctxs.length > 0) {
			sb.append(" AND (");
			for (int i = 0; i < ctxs.length; i++) {
				sb.append("ctx = ?");
				if (i < ctxs.length - 1) {
					sb.append(" OR ");
				}
			}
			sb.append(")");
		}
		if (subj != null) {
			sb.append(" AND subj = ?");
		}
		if (pred != null) {
			sb.append(" AND pred = ?");
		}
		if (obj != null) {
			sb.append(" AND obj = ?");
		}
		return sb.toString();
	}

	private void setCountQuery(PreparedStatement stmt, RdbmsResource... ctxs)
			throws SQLException, RdbmsException {
		if (ctxs != null && ctxs.length > 0) {
			for (int i = 0; i < ctxs.length; i++) {
				stmt.setLong(i + 1, vf.getInternalId(ctxs[i]));
			}
		}
	}

	private void setSelectQuery(PreparedStatement stmt, RdbmsResource subj,
			RdbmsURI pred, RdbmsValue obj, RdbmsResource... ctxs)
			throws SQLException, RdbmsException {
		int p = 0;
		if (ctxs != null && ctxs.length > 0) {
			for (int i = 0; i < ctxs.length; i++) {
				if (ctxs[i] == null) {
					stmt.setLong(++p, ValueTable.NIL_ID);
				} else {
					stmt.setLong(++p, vf.getInternalId(ctxs[i]));
				}
			}
		}
		if (subj != null) {
			stmt.setLong(++p, vf.getInternalId(subj));
		}
		if (pred != null) {
			stmt.setLong(++p, vf.getInternalId(pred));
		}
		if (obj != null) {
			stmt.setLong(++p, vf.getInternalId(obj));
		}
	}

	private void insert(List<RdbmsStatement> list) throws SQLException,
			RdbmsException {
		for (RdbmsStatement triple : list) {
			insert(triple);
		}
	}

	private void insert(RdbmsStatement triple)
		throws RdbmsException, SQLException
	{
		long ctx = vf.getInternalId(triple.getContext());
		long subj = vf.getInternalId(triple.getSubject());
		long pred = vf.getPredicateId(triple.getPredicate());
		long obj = vf.getInternalId(triple.getObject());
		statements.insert(ctx, subj, pred, obj);
	}

}
