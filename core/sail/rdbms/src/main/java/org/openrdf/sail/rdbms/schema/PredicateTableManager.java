/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.schema;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openrdf.sail.rdbms.managers.PredicateManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages and delegates to the collection of {@link PredicateTable}.
 * 
 * @author James Leigh
 * 
 */
public class PredicateTableManager {
	private static final String DEFAULT_TABLE_PREFIX = "TRIPLES";
	private static final String OTHER_TRIPLES_TABLE = "OTHER_TRIPLES";
	private static int MAX_TABLES = Integer.MAX_VALUE;
	public static Long OTHER_PRED = Long.valueOf(-1);
	private ResourceTable bnodes;
	private boolean closed;
	private Connection conn;
	private RdbmsTableFactory factory;
	private Thread initThread;
	private LiteralTable literals;
	private Logger logger = LoggerFactory.getLogger(PredicateTableManager.class);
	private ResourceTable longUris;
	private PredicateManager predicates;
	private List<PredicateTable> queue = new ArrayList<PredicateTable>();
	private Pattern tablePrefix = Pattern.compile("\\W(\\w*)\\W*$");
	private Map<Long, PredicateTable> tables = new HashMap<Long, PredicateTable>();
	private ResourceTable uris;
	Exception exc;

	public PredicateTableManager(RdbmsTableFactory factory) {
		this.factory = factory;
	}

	public void setBNodeTable(ResourceTable bnodeTable) {
		this.bnodes = bnodeTable;
	}

	public void setConnection(Connection conn) {
		this.conn = conn;
	}

	public void setLiteralTable(LiteralTable literalTable) {
		this.literals = literalTable;
	}

	public void setLongUriTable(ResourceTable longUriTable) {
		this.longUris = longUriTable;
	}

	public void setPredicateManager(PredicateManager predicates) {
		this.predicates = predicates;
	}

	public void setURITable(ResourceTable uriTable) {
		this.uris = uriTable;
	}

	public void initialize() throws SQLException {
		putAll(findPredicateTables());
		initThread = new Thread(new Runnable() {
			public void run() {
				try {
					initThread();
				} catch (Exception e) {
					exc = e;
				}
			}
		}, "table-initialize");
		initThread.start();
	}

	public void close() throws SQLException {
		closed = true;
		synchronized (queue) {
			queue.notify();
		}
		Iterator<Entry<Long, PredicateTable>> iter;
		iter = tables.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<Long, PredicateTable> next = iter.next();
			PredicateTable table = next.getValue();
			if (table.isEmpty()) {
				predicates.remove(next.getKey());
				table.drop();
				iter.remove();
			}
		}
	}

	public String findTableName(long pred) throws SQLException {
		return getPredicateTable(pred).getName();
	}

	public synchronized PredicateTable getExistingTable(long pred) {
		if (tables.containsKey(pred))
			return tables.get(pred);
		return tables.get(OTHER_PRED);
	}

	public synchronized Collection<Long> getPredicateIds() {
		return new ArrayList<Long>(tables.keySet());
	}

	public synchronized PredicateTable getPredicateTable(long pred) throws SQLException {
		assert pred != 0;
			if (tables.containsKey(pred))
				return tables.get(pred);
			if (tables.containsKey(OTHER_PRED))
				return tables.get(OTHER_PRED);
			String tableName = getNewTableName(pred);
			if (tables.size() >= MAX_TABLES) {
				tableName = OTHER_TRIPLES_TABLE;
			}
			PredicateTable table = factory.createPredicateTable(conn, tableName);
			if (tables.size() >= MAX_TABLES) {
				table.setPredColumnPresent(true);
				initTable(table);
				tables.put(OTHER_PRED, table);
			} else {
				initTable(table);
				tables.put(pred, table);
			}
			return table;
	}

	public synchronized String getTableName(long pred) throws SQLException {
		if (tables.containsKey(pred))
			return tables.get(pred).getName();
		if (tables.containsKey(OTHER_PRED))
			return tables.get(OTHER_PRED).getName();
		return null;
	}

	public void removed(int count, boolean locked) throws SQLException {
		String condition = null;
		if (locked) {
			condition = getExpungeCondition();
		}
		bnodes.removedStatements(count, condition);
		uris.removedStatements(count, condition);
		longUris.removedStatements(count, condition);
		literals.removedStatements(count, condition);
	}

	protected Set<String> findAllTables() throws SQLException {
		Set<String> tables = new HashSet<String>();
		DatabaseMetaData metaData = conn.getMetaData();
		String c = null;
		String s = null;
		String n = null;
		String[] TYPE_TABLE = new String[] { "TABLE" };
		ResultSet rs = metaData.getTables(c, s, n, TYPE_TABLE);
		try {
			while (rs.next()) {
				String tableName = rs.getString(3);
				tables.add(tableName);
			}
			return tables;
		} finally {
			rs.close();
		}
	}

	protected Map<Long, PredicateTable> findPredicateTables()
			throws SQLException {
		Map<Long, PredicateTable> tables = new HashMap<Long, PredicateTable>();
		Set<String> names = findPredicateTableNames();
		for (String tableName : names) {
			PredicateTable table = factory.createPredicateTable(conn, tableName);
			if (tableName.equalsIgnoreCase(OTHER_TRIPLES_TABLE)) {
				table.setPredColumnPresent(true);
			}
			table.reload();
			tables.put(key(tableName), table);
		}
		return tables;
	}

	protected Set<String> findTablesWithColumn(String column)
			throws SQLException {
		Set<String> tables = findTablesWithExactColumn(column.toUpperCase());
		if (tables.isEmpty())
			return findTablesWithExactColumn(column.toLowerCase());
		return tables;
	}

	protected Set<String> findTablesWithExactColumn(String column)
			throws SQLException {
		Set<String> tables = new HashSet<String>();
		DatabaseMetaData metaData = conn.getMetaData();
		String c = null;
		String s = null;
		String n = null;
		ResultSet rs = metaData.getColumns(c, s, n, column);
		try {
			while (rs.next()) {
				String tableName = rs.getString(3);
				tables.add(tableName);
			}
			return tables;
		} finally {
			rs.close();
		}
	}

	protected synchronized String getExpungeCondition() throws SQLException {
		StringBuilder sb = new StringBuilder(1024);
		for (Map.Entry<Long, PredicateTable> e : tables.entrySet()) {
			sb.append("\nAND id != ").append(e.getKey());
			if (e.getValue().isEmpty())
				continue;
			sb.append(" AND NOT EXISTS (SELECT * FROM ");
			sb.append(e.getValue().getName());
			sb.append(" WHERE ctx = id OR subj = id OR obj = id");
			if (e.getValue().isPredColumnPresent()) {
				sb.append(" OR pred = id");
			}
			sb.append(")");
		}
		return sb.toString();
	}

	protected String getNewTableName(long pred) throws SQLException {
		String prefix = getTableNamePrefix(pred);
		String tableName = prefix + "_" + pred;
		return tableName;
	}

	protected long key(String tn) {
		if (tn.equalsIgnoreCase(OTHER_TRIPLES_TABLE))
			return OTHER_PRED;
		Long id = Long.valueOf(tn.substring(tn.lastIndexOf('_') + 1));
		assert id != 0;
		return id;
	}

	protected String getTableNamePrefix(long pred) throws SQLException {
		String uri = predicates.getPredicateUri(pred);
		if (uri == null)
			return DEFAULT_TABLE_PREFIX;
		Matcher m = tablePrefix.matcher(uri);
		if (!m.find())
			return DEFAULT_TABLE_PREFIX;
		String localName = m.group(1).replaceAll("^[^a-zA-Z]*", "");
		if (localName.length() == 0)
			return DEFAULT_TABLE_PREFIX;
		if (localName.length() > 16)
			return localName.substring(0, 16);
		return localName;
	}

	void initThread() throws SQLException, InterruptedException {
		logger.debug("Starting helper thread {}", initThread.getName());
		while (!closed) {
			PredicateTable table = null;
			synchronized (queue) {
				if (queue.isEmpty()) {
					queue.wait();
				}
				if (!queue.isEmpty()) {
					table = queue.remove(0);
				}
			}
			if (table != null) {
				table.initTable();
				table = null;
			}
		}
		logger.debug("Closing helper thread {}", initThread.getName());
	}

	private Set<String> findPredicateTableNames() throws SQLException {
		Set<String> names = findAllTables();
		names.retainAll(findTablesWithColumn("ctx"));
		names.retainAll(findTablesWithColumn("subj"));
		names.retainAll(findTablesWithColumn("obj"));
		return names;
	}

	private void initTable(PredicateTable table) throws SQLException {
		if (exc != null)
			throwException();
		synchronized (queue) {
			queue.add(table);
			queue.notify();
		}
	}

	private synchronized void putAll(Map<Long, PredicateTable> t) {
		tables.putAll(t);
	}

	private void throwException() throws SQLException {
		if (exc instanceof SQLException) {
			SQLException e = (SQLException) exc;
			exc = null;
			throw e;
		} else if (exc instanceof RuntimeException) {
			RuntimeException e = (RuntimeException) exc;
			exc = null;
			throw e;
		}
	}
}
