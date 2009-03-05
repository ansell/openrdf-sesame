/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.postgresql;

import org.openrdf.sail.rdbms.schema.RdbmsTable;
import org.openrdf.sail.rdbms.schema.TableFactory;

/**
 * Overrides PostgreSQL specific table commands.
 * 
 * @author James Leigh
 */
public class PgSqlTableFactory extends TableFactory {

	@Override
	protected RdbmsTable newTable(String name) {
		return new PgSqlTable(name);
	}
}
