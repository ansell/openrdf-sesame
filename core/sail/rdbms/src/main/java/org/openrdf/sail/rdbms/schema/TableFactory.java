/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.schema;

import java.sql.Connection;

/**
 * 
 * @author James Leigh
 */
public class TableFactory {

	protected static final String TRANS_STATEMENTS = "TRANSACTION_STATEMENTS";

	public RdbmsTable createTemporaryTable(Connection conn) {
		return createTemporaryTable(conn, TRANS_STATEMENTS);
	}

	public RdbmsTable createTemporaryTable(Connection conn, String name) {
		return createTable(conn, name);
	}

	public RdbmsTable createTable(Connection conn, String name) {
		RdbmsTable table = newTable(name);
		table.setConnection(conn);
		return table;
	}

	protected RdbmsTable newTable(String name) {
		return new RdbmsTable(name);
	}

}
