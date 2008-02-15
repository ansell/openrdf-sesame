/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.schema;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openrdf.sail.rdbms.managers.PredicateManager;

/**
 * Manages and delegates to the collection of {@link PredicateTable}.
 * 
 * @author James Leigh
 * 
 */
public class PredicateTableManager {
	private RdbmsTableFactory factory;
	private Map<Long, PredicateTable> tables = new HashMap<Long, PredicateTable>();
	private ResourceTable bnodes;
	private ResourceTable uris;
	private ResourceTable longUris;
	private LiteralTable literals;
	private PredicateManager predicates;
	private Pattern tablePrefix = Pattern.compile("\\W(\\w*)\\W*$");

	public PredicateTableManager(RdbmsTableFactory factory) {
		this.factory = factory;
	}

	public void setBNodeTable(ResourceTable bnodeTable) {
		this.bnodes = bnodeTable;
	}

	public void setURITable(ResourceTable uriTable) {
		this.uris = uriTable;
	}

	public void setLongUriTable(ResourceTable longUriTable) {
		this.longUris = longUriTable;
	}

	public void setLiteralTable(LiteralTable literalTable) {
		this.literals = literalTable;
	}

	public void setPredicateManager(PredicateManager predicates) {
		this.predicates = predicates;
	}

	public void initialize() throws SQLException {
		putAll(findPredicateTables());
	}

	public void close() throws SQLException {
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

	protected Map<Long, PredicateTable> findPredicateTables()
			throws SQLException {
		Map<Long, PredicateTable> tables = new HashMap<Long, PredicateTable>();
		Set<String> names = findPredicateTableName();
		for (String tableName : names) {
			PredicateTable table = factory.createPredicateTable(tableName);
			table.initialize();
			tables.put(getPredId(tableName), table);
		}
		return tables;
	}

	private Set<String> findPredicateTableName() throws SQLException {
		Set<String> names = findAllTables();
		names.retainAll(findTablesWithColumn("ctx"));
		names.retainAll(findTablesWithColumn("subj"));
		names.retainAll(findTablesWithColumn("obj"));
		return names;
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
		DatabaseMetaData metaData = factory.getConnection().getMetaData();
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

	protected Set<String> findAllTables() throws SQLException {
		Set<String> tables = new HashSet<String>();
		DatabaseMetaData metaData = factory.getConnection().getMetaData();
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

	public String findTableName(long pred) throws SQLException {
		return getPredicateTable(pred).getName();
	}

	public synchronized Collection<Long> getPredicateIds() {
		return new ArrayList<Long>(tables.keySet());
	}

	public synchronized String getTableName(long pred) {
		if (tables.containsKey(pred))
			return tables.get(pred).getName();
		return null;
	}

	public synchronized PredicateTable getPredicateTable(long pred) throws SQLException {
			if (tables.containsKey(pred))
				return tables.get(pred);
			String tableName = getNewTableName(pred);
			PredicateTable table = factory.createPredicateTable(tableName);
			table.initialize();
			tables.put(pred, table);
			return table;
	}

	public synchronized PredicateTable getExistingTable(long pred) {
		return tables.get(pred);
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

	protected synchronized String getExpungeCondition() {
		StringBuilder sb = new StringBuilder(1024);
		for (Map.Entry<Long, PredicateTable> e : tables.entrySet()) {
			sb.append("\nAND id != ").append(e.getKey());
			if (e.getValue().isEmpty())
				continue;
			sb.append(" AND NOT EXISTS (SELECT * FROM ");
			sb.append(e.getValue().getName());
			sb.append(" WHERE ctx = id OR subj = id OR obj = id");
			sb.append(")");
		}
		return sb.toString();
	}

	protected long getPredId(String tn) {
		Long id = Long.valueOf(tn.substring(tn.lastIndexOf('_') + 1));
		return id;
	}

	protected String getNewTableName(long pred) throws SQLException {
		String prefix = getTableNamePrefix(pred);
		String tableName = prefix + "_" + pred;
		return tableName;
	}

	protected String getTableNamePrefix(long pred) throws SQLException {
		String uri = predicates.getPredicateUri(pred);
		if (uri == null)
			return "pred";
		Matcher m = tablePrefix.matcher(uri);
		if (!m.find())
			return "pred";
		String localName = m.group(1).replaceAll("^[^a-zA-Z]*", "");
		if (localName.length() == 0)
			return "pred";
		if (localName.length() > 16)
			return localName.substring(0, 16);
		return localName;
	}

	private synchronized void putAll(Map<Long, PredicateTable> t) {
		tables.putAll(t);
	}
}
