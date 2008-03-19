/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.mysql.alt;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

import org.openrdf.sail.rdbms.schema.ValueTable;


/**
 *
 * @author James Leigh
 */
public class MySqlValueTable extends ValueTable {
	private static final String FEILD_COLLATE = " CHARACTER SET utf8 COLLATE utf8_bin";

	@Override
	public String sql(int type, int length) {
		String declare = super.sql(type, length);
		if (type == Types.VARCHAR) {
			return declare + FEILD_COLLATE;
		} else if (type == Types.LONGVARCHAR) {
			return declare + FEILD_COLLATE;
		} else {
			return declare;
		}
	}

	@Override
	protected PreparedStatement prepareInsert(String s)
		throws SQLException
	{
		String name = getInsertTable().getName();
		String sql = BatchInsertStatement.prepareSql(name, "id", "value");
		PreparedStatement stmt = super.prepareInsert(sql);
		return new BatchInsertStatement(stmt, name, "id", "value");
	}

}
