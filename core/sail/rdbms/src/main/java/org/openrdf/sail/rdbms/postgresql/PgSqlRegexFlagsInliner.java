/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.postgresql;

import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.evaluation.QueryOptimizer;
import org.openrdf.sail.rdbms.algebra.SqlConcat;
import org.openrdf.sail.rdbms.algebra.SqlNull;
import org.openrdf.sail.rdbms.algebra.SqlRegex;
import org.openrdf.sail.rdbms.algebra.StringValue;
import org.openrdf.sail.rdbms.algebra.base.RdbmsQueryModelVisitorBase;
import org.openrdf.sail.rdbms.algebra.base.SqlExpr;

/**
 * Moves the regular expression flags into the pattern string as per the
 * PostgreSQL syntax.
 * 
 * @author James Leigh
 * 
 */
public class PgSqlRegexFlagsInliner extends RdbmsQueryModelVisitorBase<RuntimeException> implements
		QueryOptimizer
{

	public void optimize(TupleExpr tupleExpr, Dataset dataset, BindingSet bindings) {
		tupleExpr.visit(this);
	}

	@Override
	public void meet(SqlRegex node)
		throws RuntimeException
	{
		super.meet(node);
		SqlExpr flags = node.getFlagsArg();
		if (!(flags instanceof SqlNull)) {
			SqlExpr pattern = node.getPatternArg();
			SqlExpr prefix = concat(str("(?"), flags, str(")"));
			pattern.replaceWith(concat(prefix, pattern.clone()));
			node.setFlagsArg(null);
		}
	}

	private SqlExpr str(String string) {
		return new StringValue(string);
	}

	private SqlExpr concat(SqlExpr... exprs) {
		SqlExpr concat = null;
		for (SqlExpr expr : exprs) {
			if (concat == null) {
				concat = expr;
			}
			else {
				concat = new SqlConcat(concat, expr);
			}
		}
		return concat;
	}
}
