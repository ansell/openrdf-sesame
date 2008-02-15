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
	private Connection conn;

	public Connection getConnection() {
		return conn;
	}

	public void setConnection(Connection conn) {
		this.conn = conn;
	}

	public NamespacesTable createNamespacesTable() {
		return new NamespacesTable(createRdbmsTable(NAMESPACES));
	}

	public ResourceTable createBNodeTable() {
		RdbmsTable rdbmsTable = createRdbmsTable(BNODE_VALUES);
		return new ResourceTable(createValueTable(rdbmsTable, VARCHAR, VCS));
	}

	public ResourceTable createURITable() {
		RdbmsTable rdbmsTable = createRdbmsTable(URI_VALUES);
		return new ResourceTable(createValueTable(rdbmsTable, VARCHAR, VCL));
	}

	public ResourceTable createLongURITable() {
		RdbmsTable rdbmsTable = createRdbmsTable(LURI_VALUES);
		return new ResourceTable(createValueTable(rdbmsTable, LONGVARCHAR));
	}

	public LiteralTable createLiteralTable() {
		ValueTable lbs = createValueTable(createRdbmsTable(LBS), VARCHAR, VCL);
		ValueTable llbs = createValueTable(createRdbmsTable(LLBS), LONGVARCHAR);
		ValueTable lgs = createValueTable(createRdbmsTable(LANGS), VARCHAR, VCS);
		ValueTable dt = createValueTable(createRdbmsTable(DTS), VARCHAR, VCL);
		ValueTable num = createValueTable(createRdbmsTable(NUM_VALUES), DOUBLE);
		ValueTable dateTime = createValueTable(createRdbmsTable(TIMES), BIGINT);
		LiteralTable literals = new LiteralTable();
		literals.setLabelTable(lbs);
		literals.setLongLabelTable(llbs);
		literals.setLanguageTable(lgs);
		literals.setDatatypeTable(dt);
		literals.setNumericTable(num);
		literals.setDateTimeTable(dateTime);
		return literals;
	}

	public PredicateTable createPredicateTable(String tableName) {
		return new PredicateTable(createRdbmsTable(tableName));
	}

	public TransactionTable createTransactionTable() {
		return new TransactionTable();
	}

	public RdbmsTable createTemporaryTable() {
		return createRdbmsTable(TRANS_STATEMENTS);
	}

	protected RdbmsTable createRdbmsTable(String name) {
		return new RdbmsTable(conn, name);
	}

	protected ValueTable createValueTable(RdbmsTable rdbmsTable, int sqlType) {
		return createValueTable(rdbmsTable, sqlType, -1);
	}

	protected ValueTable createValueTable(RdbmsTable rdbmsTable, int sqlType,
			int length) {
		return new ValueTable(rdbmsTable, sqlType, length);
	}
}
