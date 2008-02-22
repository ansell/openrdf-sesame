/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.schema;

import java.sql.SQLException;

/**
 * Manages the life-cycle of the rows in a single predicate table.
 * 
 * @author James Leigh
 * 
 */
public class PredicateTable {
	public static int tables_created;
	public static int total_st;
	private static final String[] PKEY = { "ctx", "subj", "obj" };
	private static final String[] SUBJ_INDEX = { "subj" };
	private static final String[] OBJ_INDEX = { "obj" };
	private static final String[] OBJ_CONTAINS = new String[IdCode.values().length];
	private static final String[] SUBJ_CONTAINS = new String[IdCode.values().length];
	static {
		StringBuilder sb = new StringBuilder();
		for (int i = 0, n = IdCode.values().length; i < n; i++) {
			sb.delete(0, sb.length());
			IdCode code = IdCode.values()[i];
			sb.append("MAX(CASE WHEN obj BETWEEN ").append(code.minId());
			sb.append(" AND ").append(code.maxId());
			sb.append(" THEN 1 ELSE 0 END)");
			OBJ_CONTAINS[i] = sb.toString();
			sb.delete(0, sb.length());
			sb.append("MAX(CASE WHEN subj BETWEEN ").append(code.minId());
			sb.append(" AND ").append(code.maxId());
			sb.append(" THEN 1 ELSE 0 END)");
			SUBJ_CONTAINS[i] = sb.toString();
		}
	}
	private RdbmsTable table;
	private ValueTypes objTypes = new ValueTypes();
	private ValueTypes subjTypes = new ValueTypes();
	private boolean initialize;

	public PredicateTable(RdbmsTable table) {
		this.table = table;
	}

	public synchronized void initTable() throws SQLException {
		if (initialize)
			return;
		table.createTransactionalTable(buildTableColumns());
		total_st++;
		table.index(PKEY);
		total_st++;
		table.index(OBJ_INDEX);
		total_st++;
		table.index(SUBJ_INDEX);
		total_st++;
		initialize = true;
		tables_created++;
	}

	public void reload() throws SQLException {
		table.count();
		if (table.size() > 0) {
			int[] aggregate = table.aggregate(OBJ_CONTAINS);
			for (int i = 0; i < aggregate.length; i++) {
				if (aggregate[i] == 1) {
					objTypes.add(IdCode.values()[i]);
				}
			}
			aggregate = table.aggregate(SUBJ_CONTAINS);
			for (int i = 0; i < aggregate.length; i++) {
				if (aggregate[i] == 1) {
					subjTypes.add(IdCode.values()[i]);
				}
			}
		
		}
		initialize = true;
	}

	public void blockUntilReady() throws SQLException {
		if (initialize)
			return;
		initTable();
	}

	public String getName() throws SQLException {
		blockUntilReady();
		return table.getName();
	}

	public ValueTypes getObjTypes() {
		return objTypes;
	}

	public void setObjTypes(ValueTypes valueTypes) {
		this.objTypes.merge(valueTypes);
	}

	public ValueTypes getSubjTypes() {
		return subjTypes;
	}

	public void setSubjTypes(ValueTypes valueTypes) {
		this.subjTypes.merge(valueTypes);
	}

	public void modified(int addedCount, int removedCount) throws SQLException {
		blockUntilReady();
		table.modified(addedCount, removedCount);
		table.optimize();
		if (isEmpty()) {
			objTypes.reset();
			subjTypes.reset();
		}
	}

	public boolean isEmpty() throws SQLException {
		blockUntilReady();
		return table.size() == 0;
	}

	@Override
	public String toString() {
		return table.getName();
	}

	public void drop() throws SQLException {
		blockUntilReady();
		table.drop();
	}

	protected CharSequence buildTableColumns() {
		StringBuilder sb = new StringBuilder();
		sb.append("  ctx BIGINT NOT NULL,\n");
		sb.append("  subj BIGINT NOT NULL,\n");
		sb.append("  obj BIGINT NOT NULL\n");
		return sb;
	}
}
