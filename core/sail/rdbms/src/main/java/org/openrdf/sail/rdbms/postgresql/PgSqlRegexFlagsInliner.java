/* 
 * Licensed to Aduna under one or more contributor license agreements.  
 * See the NOTICE.txt file distributed with this work for additional 
 * information regarding copyright ownership. 
 *
 * Aduna licenses this file to you under the terms of the Aduna BSD 
 * License (the "License"); you may not use this file except in compliance 
 * with the License. See the LICENSE.txt file distributed with this work 
 * for the full License.
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing permissions
 * and limitations under the License.
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
