/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.mysql.alt;

import static org.openrdf.sail.rdbms.algebra.base.SqlExprSupport.unsupported;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.openrdf.query.algebra.Regex;
import org.openrdf.query.algebra.ValueConstant;
import org.openrdf.query.algebra.ValueExpr;
import org.openrdf.sail.rdbms.RdbmsConnectionFactory;
import org.openrdf.sail.rdbms.algebra.factories.BooleanExprFactory;
import org.openrdf.sail.rdbms.evaluation.QueryBuilderFactory;
import org.openrdf.sail.rdbms.evaluation.SqlExprBuilder;
import org.openrdf.sail.rdbms.evaluation.SqlRegexBuilder;
import org.openrdf.sail.rdbms.exceptions.UnsupportedRdbmsOperatorException;
import org.openrdf.sail.rdbms.managers.TransTableManager;
import org.openrdf.sail.rdbms.optimizers.SelectQueryOptimizerFactory;
import org.openrdf.sail.rdbms.schema.NamespacesTable;
import org.openrdf.sail.rdbms.schema.RdbmsTable;
import org.openrdf.sail.rdbms.schema.TableFactory;
import org.openrdf.sail.rdbms.schema.TransactionTable;
import org.openrdf.sail.rdbms.schema.ValueTable;
import org.openrdf.sail.rdbms.schema.ValueTableFactory;

public class MySqlConnectionFactory extends RdbmsConnectionFactory {

	private static final String FEILD_COLLATE = " CHARACTER SET utf8 COLLATE utf8_bin";

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
		return new ValueTableFactory(createTableFactory())
		{

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
		};
	}

	@Override
	protected TransTableManager createTransTableManager() {
		return new TransTableManager() {

			@Override
			protected TransactionTable createTransactionTable() {
				return new TransactionTable() {

					@Override
					protected String buildInsert(String tableName, boolean predColumnPresent)
						throws SQLException
					{
						return BatchInsertStatement.prepareSql(tableName, getColumns());
					}

					@Override
					protected PreparedStatement prepareInsert(String sql)
						throws SQLException
					{
						String name = getTripleTable().getName();
						PreparedStatement stmt = super.prepareInsert(sql);
						return new BatchInsertStatement(stmt, name, getColumns());
					}

					private String[] getColumns() {
						boolean predColumnPresent = getTripleTable().isPredColumnPresent();
						if (predColumnPresent)
							return new String[]{"ctx","subj","pred","obj"};
						return new String[]{"ctx","subj","obj"};
					}
				};
			}
		};
	}
}
