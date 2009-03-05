/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.postgresql;

import java.sql.Types;

import org.openrdf.sail.rdbms.schema.ValueTable;

/**
 * Optimises prepared insert statements for PostgreSQL and overrides the DOUBLE
 * column type.
 * 
 * @author James Leigh
 */
public class PgSqlValueTable extends ValueTable {

	@Override
	public String sql(int type, int length) {
		switch (type) {
			case Types.DOUBLE:
				return "double precision";
			default:
				return super.sql(type, length);
		}
	}

}
