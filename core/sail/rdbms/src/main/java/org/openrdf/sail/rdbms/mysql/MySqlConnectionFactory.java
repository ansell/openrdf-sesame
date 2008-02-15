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
import org.openrdf.sail.rdbms.evaluation.SqlExprBuilder;
import org.openrdf.sail.rdbms.evaluation.SqlRegexBuilder;
import org.openrdf.sail.rdbms.exceptions.UnsupportedRdbmsOperatorException;
import org.openrdf.sail.rdbms.optimizers.SelectQueryOptimizerFactory;
import org.openrdf.sail.rdbms.schema.NamespacesTable;
import org.openrdf.sail.rdbms.schema.RdbmsTable;
import org.openrdf.sail.rdbms.schema.RdbmsTableFactory;
import org.openrdf.sail.rdbms.schema.ValueTable;

public class MySqlConnectionFactory extends RdbmsConnectionFactory {
	private static final String FEILD_COLLATE = " CHARACTER SET utf8 COLLATE utf8_bin";

	@Override
	protected RdbmsTableFactory createRdbmsTableFactory() {
		return new RdbmsTableFactory() {
			@Override
			protected RdbmsTable newTable(String name) {
				return new MySqlTable(name);
			}

			@Override
			protected ValueTable createValueTable(RdbmsTable rdbmsTable,
					int sqlType, int length) {
				return new ValueTable(rdbmsTable, sqlType, length) {
					@Override
					protected String getDeclaredSqlType(int type, int length) {
						String declare = super.getDeclaredSqlType(type, length);
						if (type == Types.VARCHAR) {
							return declare + FEILD_COLLATE;
						} else if (type == Types.LONGVARCHAR) {
							return declare + FEILD_COLLATE;
						} else {
							return declare;
						}
					}
				};
			}

			@Override
			public NamespacesTable createNamespacesTable(Connection conn) {
				return new NamespacesTable(createTable(conn, NAMESPACES)) {
					@Override
					protected void createTable() throws SQLException {
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
							throws UnsupportedRdbmsOperatorException {
						ValueExpr flagsArg = node.getFlagsArg();
						if (flagsArg == null) {
							super.meet(node);
						} else if (flagsArg instanceof ValueConstant) {
							ValueConstant flags = (ValueConstant) flagsArg;
							if (flags.getValue().stringValue().equals("i")) {
								super.meet(node);
							} else {
								throw unsupported(node);
							}
						} else {
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
		};
	}
}
