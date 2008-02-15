/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.schema;

import static java.sql.Types.BIGINT;
import static java.sql.Types.DOUBLE;
import static java.sql.Types.VARCHAR;
import static java.sql.Types.LONGVARCHAR;

import java.sql.Connection;

/**
 * Factory class used to create or load the database tables.
 * 
 * @author James Leigh
 * 
 */
public class RdbmsTableFactory {
	private static final int VCS = 127;
	private static final int VCL = 255;
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
	protected static final String TRANS_STATEMENTS = "TRANSACTION_STATEMENTS";

	public NamespacesTable createNamespacesTable(Connection conn) {
		return new NamespacesTable(createTable(conn, NAMESPACES));
	}

	public ResourceTable createBNodeTable(Connection conn) {
		RdbmsTable rdbmsTable = createTable(conn, BNODE_VALUES);
		return new ResourceTable(createValueTable(rdbmsTable, VARCHAR, VCS));
	}

	public ResourceTable createURITable(Connection conn) {
		RdbmsTable rdbmsTable = createTable(conn, URI_VALUES);
		return new ResourceTable(createValueTable(rdbmsTable, VARCHAR, VCL));
	}

	public ResourceTable createLongURITable(Connection conn) {
		RdbmsTable rdbmsTable = createTable(conn, LURI_VALUES);
		return new ResourceTable(createValueTable(rdbmsTable, LONGVARCHAR));
	}

	public LiteralTable createLiteralTable(Connection conn) {
		ValueTable lbs = createValueTable(createTable(conn, LBS), VARCHAR, VCL);
		ValueTable llbs = createValueTable(createTable(conn, LLBS), LONGVARCHAR);
		ValueTable lgs = createValueTable(createTable(conn, LANGS), VARCHAR, VCS);
		ValueTable dt = createValueTable(createTable(conn, DTS), VARCHAR, VCL);
		ValueTable num = createValueTable(createTable(conn, NUM_VALUES), DOUBLE);
		ValueTable dateTime = createValueTable(createTable(conn, TIMES), BIGINT);
		LiteralTable literals = new LiteralTable();
		literals.setLabelTable(lbs);
		literals.setLongLabelTable(llbs);
		literals.setLanguageTable(lgs);
		literals.setDatatypeTable(dt);
		literals.setNumericTable(num);
		literals.setDateTimeTable(dateTime);
		return literals;
	}

	public PredicateTable createPredicateTable(Connection conn, String tableName) {
		RdbmsTable table = newTable(tableName);
		table.setConnection(conn);
		return new PredicateTable(table);
	}

	public TransactionTable createTransactionTable() {
		return new TransactionTable();
	}

	public RdbmsTable createTemporaryTable(Connection conn) {
		return createTable(conn, TRANS_STATEMENTS);
	}

	protected RdbmsTable createTable(Connection conn, String name) {
		RdbmsTable table = newTable(name);
		table.setConnection(conn);
		return table;
	}

	protected RdbmsTable newTable(String name) {
		return new RdbmsTable(name);
	}

	protected ValueTable createValueTable(RdbmsTable rdbmsTable, int sqlType) {
		return createValueTable(rdbmsTable, sqlType, -1);
	}

	protected ValueTable createValueTable(RdbmsTable rdbmsTable, int sqlType,
			int length) {
		return new ValueTable(rdbmsTable, sqlType, length);
	}
}
