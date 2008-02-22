/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.schema;

import static org.openrdf.sail.rdbms.schema.PredicateTableManager.OTHER_PRED;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Manages and delegates to a collection of {@link TransactionTable}s.
 * 
 * @author James Leigh
 * 
 */
public class TransTableManager {
	public static int BATCH_SIZE = 512;
	private RdbmsTableFactory factory;
	private PredicateTableManager predicates;
	private RdbmsTable temporaryTable;
	private Map<Long, TransactionTable> tables = new HashMap<Long, TransactionTable>();
	private List<TransactionTable> list;
	private int removedCount;
	private String fromDummy;
	private Connection conn;

	public void setConnection(Connection conn) {
		this.conn = conn;
	}

	public void setRdbmsTableFactory(RdbmsTableFactory factory) {
		this.factory = factory;
	}

	public void setStatementsTable(PredicateTableManager predicateTableManager) {
		this.predicates = predicateTableManager;
	}

	public void setFromDummyTable(String fromDummy) {
		this.fromDummy = fromDummy;
	}

	public int getBatchSize() {
		return BATCH_SIZE;
	}

	public int getAvgUncommittedRowCount() throws SQLException {
		List<TransactionTable> tables = getTables();
		if (tables.size() == 0)
			return 0;
		long sum = 0;
		for (TransactionTable table : tables) {
			sum += table.getUncommittedRowCount();
		}
		return (int) (sum / tables.size());
	}

	public void initialize() throws SQLException {
	}

	public void insert(long ctx, long subj, long pred, long obj)
			throws SQLException {
		getTable(pred).insert(ctx, subj, pred, obj);
	}

	public int flush() throws SQLException {
		int count = 0;
		for (TransactionTable table : getTables()) {
			count += table.flush();
			table.cleanup();
		}
		return count;
	}

	public void cleanup() throws SQLException {
		synchronized (tables) {
			for (TransactionTable table : tables.values()) {
				table.cleanup();
			}
		}
	}

	public void close() throws SQLException {
		cleanup();
	}

	public String findTableName(long pred) throws SQLException {
		return predicates.findTableName(pred);
	}

	public String getCombinedTableName() throws SQLException {
		String union = " UNION ALL ";
		StringBuilder sb = new StringBuilder(1024);
		sb.append("(");
		for (Long pred : predicates.getPredicateIds()) {
			PredicateTable predicate;
			try {
				predicate = predicates.getPredicateTable(pred);
			} catch (SQLException e) {
				throw new AssertionError(e);
			}
			TransactionTable table = findTable(pred);
			if ((table == null || table.isEmpty()) && predicate.isEmpty())
				continue;
			sb.append("SELECT ctx, subj, ");
			if (predicate.isPredColumnPresent()) {
				sb.append(" pred,");
			} else {
				sb.append(pred).append(" AS pred,");
			}
			sb.append(" obj");
			sb.append("\nFROM ");
			sb.append(predicate.getName());
			sb.append(union);
		}
		if (sb.length() < union.length())
			return getEmptyTableName();
		sb.delete(sb.length() - union.length(), sb.length());
		sb.append(")");
		return sb.toString();
	}

	public String getTableName(long pred) throws SQLException {
		if (pred == ValueTable.NIL_ID)
			return getCombinedTableName();
		String tableName = predicates.getTableName(pred);
		if (tableName == null)
			return getEmptyTableName();
		return tableName;
	}

	private String getEmptyTableName() {
		StringBuilder sb = new StringBuilder(256);
		sb.append("(");
		sb.append("SELECT ");
		sb.append(getZeroBigInt()).append(" AS ctx, ");
		sb.append(getZeroBigInt()).append(" AS subj, ");
		sb.append(getZeroBigInt()).append(" AS pred, ");
		sb.append(getZeroBigInt()).append(" AS obj ");
		sb.append(fromDummy);
		sb.append("\nWHERE 1=0");
		sb.append(")");
		return sb.toString();
	}

	protected String getZeroBigInt() {
		return "0";
	}

	public void committed(boolean locked) throws SQLException {
		synchronized (tables) {
			for (TransactionTable table : tables.values()) {
				table.committed();
				table.cleanup();
			}
			list = null;
			tables.clear();
		}
		if (removedCount > 0) {
			predicates.removed(removedCount, locked);
		}
	}

	public void removed(Long pred, int count) throws SQLException {
		getTable(pred).removed(count);
		removedCount += count;
	}

	protected TransactionTable getTable(long pred) throws SQLException {
		synchronized (tables) {
			TransactionTable table = tables.get(pred);
			if (table == null) {
				PredicateTable predicate = predicates.getPredicateTable(pred);
				Long key = pred;
				if (predicate.isPredColumnPresent()) {
					key = OTHER_PRED;
					table = tables.get(key);
					if (table != null)
						return table;
				}
				table = createTransactionTable(predicate);
				tables.put(key, table);
				list = null;
			}
			return table;
		}
	}

	public Collection<Long> getPredicateIds() {
		return predicates.getPredicateIds();
	}

	public boolean isPredColumnPresent(Long id) throws SQLException {
		return predicates.getPredicateTable(id).isPredColumnPresent();
	}

	public ValueTypes getObjTypes(long pred) {
		PredicateTable table = predicates.getExistingTable(pred);
		if (table == null)
			return ValueTypes.UNKNOWN;
		ValueTypes subjTypes = table.getObjTypes();
		return subjTypes;
	}

	public ValueTypes getSubjTypes(long pred) {
		PredicateTable table = predicates.getExistingTable(pred);
		if (table == null)
			return ValueTypes.RESOURCE;
		return table.getSubjTypes();
	}

	public boolean isEmpty() throws SQLException {
		for (Long pred : predicates.getPredicateIds()) {
			PredicateTable predicate;
			try {
				predicate = predicates.getPredicateTable(pred);
			} catch (SQLException e) {
				throw new AssertionError(e);
			}
			TransactionTable table = findTable(pred);
			if (table != null && !table.isEmpty() || !predicate.isEmpty())
				return false;
		}
		return true;
	}

	protected TransactionTable createTransactionTable(PredicateTable predicate)
			throws SQLException {
		if (temporaryTable == null) {
			temporaryTable = factory.createTemporaryTable(conn);
			if (!temporaryTable.isCreated()) {
				createTemporaryTable(temporaryTable);
			}
		}
		TransactionTable table = factory.createTransactionTable();
		table.setPredicateTable(predicate);
		table.setTemporaryTable(temporaryTable);
		table.setBatchSize(getBatchSize());
		table.initialize();
		return table;
	}

	protected void createTemporaryTable(RdbmsTable table) throws SQLException {
		StringBuilder sb = new StringBuilder();
		sb.append("  ctx BIGINT NOT NULL,\n");
		sb.append("  subj BIGINT NOT NULL,\n");
		sb.append("  pred BIGINT NOT NULL,\n");
		sb.append("  obj BIGINT NOT NULL\n");
		table.createTemporaryTable(sb);
	}

	private List<TransactionTable> getTables() {
		List<TransactionTable> list = this.list;
		synchronized (tables) {
			if (list == null || list.size() != tables.size()) {
				this.list = list = new ArrayList(tables.values());
			}
		}
		return list;
	}

	private TransactionTable findTable(Long pred) {
		synchronized (tables) {
			return tables.get(pred);
		}
	}

}
