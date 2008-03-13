/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.schema;

import static java.sql.Types.BIGINT;
import static java.sql.Types.DOUBLE;
import static java.sql.Types.LONGVARCHAR;
import static java.sql.Types.VARCHAR;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.BlockingQueue;

/**
 * Factory class used to create or load the database tables.
 * 
 * @author James Leigh
 * 
 */
public class ValueTableFactory {
	private static final int VCS = 127;
	private static final int VCL = 255;
	public static final boolean INDEX_VALUES = false;
	protected static final String LANGUAGES = "LANGUAGES";
	protected static final String NAMESPACES = "NAMESPACE_PREFIXES";
	protected static final String RESOURCES = "RESOURCES";
	protected static final String BNODE_VALUES = "BNODE_VALUES";
	protected static final String URI_VALUES = "URI_VALUES";
	protected static final String LURI_VALUES = "LONG_URI_VALUES";
	protected static final String LBS = "LABEL_VALUES";
	protected static final String LLBS = "LONG_LABEL_VALUES";
	protected static final String LANGS = "LANGUAGE_VALUES";
	protected static final String DTS = "DATATYPE_VALUES";
	protected static final String NUM_VALUES = "NUMERIC_VALUES";
	protected static final String TIMES = "DATETIME_VALUES";
	protected static final String HASH_TABLE = "HASH_VALUES";
	private TableFactory factory;
	private HashTable hashTable = new NoHashTable();

	public ValueTableFactory(TableFactory factory) {
		super();
		this.factory = factory;
	}

	public void setHashTable(HashTable table) {
		this.hashTable = table;
	}

	public HashTable createHashTable(Connection conn, BlockingQueue<Batch> queue) throws SQLException {
		ValueTable table = newValueTable();
		table.setRdbmsTable(createTable(conn, HASH_TABLE));
		table.setTemporaryTable(factory.createTemporaryTable(conn, "INSERT_" + HASH_TABLE));
		initValueTable(table, queue, BIGINT, -1, true);
		return new HashTable(table);
	}

	public NamespacesTable createNamespacesTable(Connection conn) {
		return new NamespacesTable(createTable(conn, NAMESPACES));
	}

	public BNodeTable createBNodeTable(Connection conn, BlockingQueue<Batch> queue) throws SQLException {
		ValueTable table = createValueTable(conn, queue, BNODE_VALUES, VARCHAR, VCS);
		HashTable hash = getHashTable();
		return new BNodeTable(table, hash);
	}

	public URITable createURITable(Connection conn, BlockingQueue<Batch> queue) throws SQLException {
		ValueTable shorter = createValueTable(conn, queue, URI_VALUES, VARCHAR, VCL);
		ValueTable longer = createValueTable(conn, queue, LURI_VALUES, LONGVARCHAR);
		HashTable hash = getHashTable();
		return new URITable(shorter, longer, hash);
	}

	public LiteralTable createLiteralTable(Connection conn, BlockingQueue<Batch> queue) throws SQLException {
		ValueTable lbs = createValueTable(conn, queue, LBS, VARCHAR, VCL);
		ValueTable llbs = createValueTable(conn, queue, LLBS, LONGVARCHAR);
		ValueTable lgs = createValueTable(conn, queue, LANGS, VARCHAR, VCS);
		ValueTable dt = createValueTable(conn, queue, DTS, VARCHAR, VCL);
		ValueTable num = createValueTable(conn, queue, NUM_VALUES, DOUBLE);
		ValueTable dateTime = createValueTable(conn, queue, TIMES, BIGINT);
		HashTable hash = getHashTable();
		LiteralTable literals = new LiteralTable();
		literals.setLabelTable(lbs);
		literals.setLongLabelTable(llbs);
		literals.setLanguageTable(lgs);
		literals.setDatatypeTable(dt);
		literals.setNumericTable(num);
		literals.setDateTimeTable(dateTime);
		literals.setHashTable(hash);
		return literals;
	}

	public TripleTable createTripleTable(Connection conn, String tableName) {
		RdbmsTable table = createTable(conn, tableName);
		return new TripleTable(table);
	}

	protected HashTable getHashTable() {
		return hashTable;
	}

	protected RdbmsTable createTable(Connection conn, String name) {
		return factory.createTable(conn, name);
	}

	protected ValueTable createValueTable(Connection conn, BlockingQueue<Batch> queue, String name, int sqlType) throws SQLException {
		return createValueTable(conn, queue, name, sqlType, -1);
	}

	protected ValueTable createValueTable(Connection conn, BlockingQueue<Batch> queue, String name, int sqlType,
			int length) throws SQLException {
		ValueTable table = newValueTable();
		table.setRdbmsTable(createTable(conn, name));
		table.setTemporaryTable(factory.createTemporaryTable(conn, "INSERT_" + name));
		initValueTable(table, queue, sqlType, length, INDEX_VALUES);
		return table;
	}

	private void initValueTable(ValueTable table, BlockingQueue<Batch> queue, int sqlType, int length, boolean indexValues)
		throws SQLException
	{
		table.setQueue(queue);
		table.setSqlType(sqlType);
		table.setLength(length);
		table.setIndexingValues(indexValues);
		table.initialize();
	}

	protected ValueTable newValueTable() {
		return new ValueTable();
	}
}
