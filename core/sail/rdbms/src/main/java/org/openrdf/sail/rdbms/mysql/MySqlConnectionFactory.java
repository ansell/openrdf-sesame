/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.mysql;

import static org.openrdf.sail.rdbms.algebra.base.SqlExprSupport.unsupported;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;

import org.openrdf.query.algebra.Regex;
import org.openrdf.query.algebra.ValueConstant;
import org.openrdf.query.algebra.ValueExpr;
import org.openrdf.sail.rdbms.RdbmsConnectionFactory;
import org.openrdf.sail.rdbms.algebra.factories.BooleanExprFactory;
import org.openrdf.sail.rdbms.evaluation.QueryBuilderFactory;
import org.openrdf.sail.rdbms.evaluation.SqlCastBuilder;
import org.openrdf.sail.rdbms.evaluation.SqlExprBuilder;
import org.openrdf.sail.rdbms.evaluation.SqlRegexBuilder;
import org.openrdf.sail.rdbms.exceptions.UnsupportedRdbmsOperatorException;
import org.openrdf.sail.rdbms.optimizers.SelectQueryOptimizerFactory;
import org.openrdf.sail.rdbms.schema.NamespacesTable;
import org.openrdf.sail.rdbms.schema.RdbmsTable;
import org.openrdf.sail.rdbms.schema.TableFactory;
import org.openrdf.sail.rdbms.schema.ValueTable;
import org.openrdf.sail.rdbms.schema.ValueTableFactory;

public class MySqlConnectionFactory extends RdbmsConnectionFactory {

	private static final String FEILD_COLLATE = " CHARACTER SET utf8 COLLATE utf8_bin";

	@Override
	protected String getFromDummyTable() {
		return "FROM DUAL";
	}

	@Override
	protected TableFactory createTableFactory() {
		return new TableFactory() {

			@Override
			protected RdbmsTable newTable(String name) {
				return new MySqlTable(name);
			}
		};
	}

	@Override
	protected ValueTableFactory createValueTableFactory() {
		return new ValueTableFactory(createTableFactory()) {

			@Override
			protected ValueTable newValueTable() {
				return new MySqlValueTable();
			}

			@Override
			public NamespacesTable createNamespacesTable(Connection conn) {
				return new NamespacesTable(createTable(conn, NAMESPACES)) {

					@Override
					protected void createTable()
						throws SQLException
					{
						StringBuilder sb = new StringBuilder();
						sb.append("  prefix VARCHAR(127)");
						sb.append(FEILD_COLLATE);
						sb.append(",\n  namespace TEXT ");
						sb.append(FEILD_COLLATE);
						sb.append(" NOT NULL\n");
						createTable(sb);
					}
				};
			}
		};
	}

	@Override
	protected SelectQueryOptimizerFactory createSelectQueryOptimizerFactory() {
		return new SelectQueryOptimizerFactory() {

			@Override
			protected BooleanExprFactory createBooleanExprFactory() {
				return new BooleanExprFactory() {

					@Override
					public void meet(Regex node)
						throws UnsupportedRdbmsOperatorException
					{
						ValueExpr flagsArg = node.getFlagsArg();
						if (flagsArg == null) {
							super.meet(node);
						}
						else if (flagsArg instanceof ValueConstant) {
							ValueConstant flags = (ValueConstant)flagsArg;
							if (flags.getValue().stringValue().equals("i")) {
								super.meet(node);
							}
							else {
								throw unsupported(node);
							}
						}
						else {
							throw unsupported(node);
						}
					}
				};
			}
		};
	}

	@Override
	protected QueryBuilderFactory createQueryBuilderFactory() {
		return new QueryBuilderFactory() {

			@Override
			public SqlRegexBuilder createSqlRegexBuilder(SqlExprBuilder where) {
				return new SqlRegexBuilder(where, this) {

					@Override
					protected void appendRegExp(SqlExprBuilder where) {
						appendValue(where);
						if (!this.flags().getParameters().isEmpty()) {
							where.append(" COLLATE utf8_general_ci");
						}
						where.append(" REGEXP ");
						appendPattern(where);
					}
				};
			}

			@Override
			public SqlCastBuilder createSqlCastBuilder(SqlExprBuilder where, int type) {
				return new SqlCastBuilder(where, this, type) {

					@Override
					protected CharSequence getSqlType(int type) {
						switch (type) {
							case Types.VARCHAR:
								return "CHAR";
							default:
								return super.getSqlType(type);
						}
					}
				};
			}
		};
	}
}
