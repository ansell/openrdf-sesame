/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.optimizers;

import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.evaluation.QueryOptimizer;
import org.openrdf.sail.rdbms.algebra.BNodeColumn;
import org.openrdf.sail.rdbms.algebra.ColumnVar;
import org.openrdf.sail.rdbms.algebra.DatatypeColumn;
import org.openrdf.sail.rdbms.algebra.DateTimeColumn;
import org.openrdf.sail.rdbms.algebra.IdColumn;
import org.openrdf.sail.rdbms.algebra.JoinItem;
import org.openrdf.sail.rdbms.algebra.LabelColumn;
import org.openrdf.sail.rdbms.algebra.LanguageColumn;
import org.openrdf.sail.rdbms.algebra.LongLabelColumn;
import org.openrdf.sail.rdbms.algebra.LongURIColumn;
import org.openrdf.sail.rdbms.algebra.NumericColumn;
import org.openrdf.sail.rdbms.algebra.RefIdColumn;
import org.openrdf.sail.rdbms.algebra.SelectQuery;
import org.openrdf.sail.rdbms.algebra.SqlEq;
import org.openrdf.sail.rdbms.algebra.URIColumn;
import org.openrdf.sail.rdbms.algebra.base.FromItem;
import org.openrdf.sail.rdbms.algebra.base.RdbmsQueryModelVisitorBase;
import org.openrdf.sail.rdbms.schema.BNodeTable;
import org.openrdf.sail.rdbms.schema.LiteralTable;
import org.openrdf.sail.rdbms.schema.URITable;

/**
 * Adds LEFT JOINs to the query for value tables.
 * 
 * @author James Leigh
 * 
 */
public class ValueJoinOptimizer extends
		RdbmsQueryModelVisitorBase<RuntimeException> implements QueryOptimizer {
	private URITable uris;
	private BNodeTable bnodes;
	private LiteralTable literals;
	private FromItem join;
	private FromItem parent;
	private SelectQuery query;

	public void setUriTable(URITable uris) {
		this.uris = uris;
	}

	public void setBnodeTable(BNodeTable bnodes) {
		this.bnodes = bnodes;
	}

	public void setLiteralTable(LiteralTable literals) {
		this.literals = literals;
	}

	public void optimize(TupleExpr tupleExpr, Dataset dataset,
			BindingSet bindings) {
		join = null;
		tupleExpr.visit(this);
	}

	@Override
	public void meetFromItem(FromItem node) throws RuntimeException {
		FromItem top = parent;
		parent = join;
		join = node;
		super.meetFromItem(node);
		join = parent;
		parent = top;
	}

	@Override
	public void meet(SelectQuery node) throws RuntimeException {
		query = node;
		parent = node.getFrom();
		join = node.getFrom();
		super.meet(node);
		join = null;
		parent = null;
		query = null;
	}

	@Override
	public void meet(BNodeColumn node) throws RuntimeException {
		ColumnVar var = node.getRdbmsVar();
		String alias = "b" + getDBName(var);
		String tableName = bnodes.getName();
		join(var, alias, tableName);
	}

	@Override
	public void meet(DatatypeColumn node) throws RuntimeException {
		ColumnVar var = node.getRdbmsVar();
		String alias = "d" + getDBName(var);
		String tableName = literals.getDatatypeTable().getName();
		join(var, alias, tableName);
	}

	@Override
	public void meet(DateTimeColumn node) throws RuntimeException {
		ColumnVar var = node.getRdbmsVar();
		String alias = "t" + getDBName(var);
		String tableName = literals.getDateTimeTable().getName();
		join(var, alias, tableName);
	}

	@Override
	public void meet(LabelColumn node) throws RuntimeException {
		ColumnVar var = node.getRdbmsVar();
		String alias = "l" + getDBName(var);
		String tableName = literals.getLabelTable().getName();
		join(var, alias, tableName);
	}

	@Override
	public void meet(LongLabelColumn node) throws RuntimeException {
		ColumnVar var = node.getRdbmsVar();
		String alias = "ll" + getDBName(var);
		String tableName = literals.getLongLabelTable().getName();
		join(var, alias, tableName);
	}

	@Override
	public void meet(LanguageColumn node) throws RuntimeException {
		ColumnVar var = node.getRdbmsVar();
		String alias = "g" + getDBName(var);
		String tableName = literals.getLanguageTable().getName();
		join(var, alias, tableName);
	}

	@Override
	public void meet(NumericColumn node) throws RuntimeException {
		ColumnVar var = node.getRdbmsVar();
		String alias = "n" + getDBName(var);
		String tableName = literals.getNumericTable().getName();
		join(var, alias, tableName);
	}

	@Override
	public void meet(LongURIColumn node) throws RuntimeException {
		ColumnVar var = node.getRdbmsVar();
		String alias = "lu" + getDBName(var);
		String tableName = uris.getLongTableName();
		join(var, alias, tableName);
	}

	@Override
	public void meet(URIColumn node) throws RuntimeException {
		ColumnVar var = node.getRdbmsVar();
		String alias = "u" + getDBName(var);
		String tableName = uris.getShortTableName();
		join(var, alias, tableName);
	}

	private CharSequence getDBName(ColumnVar var) {
		String name = var.getName();
		if (name.indexOf('-') >= 0)
			return name.replace('-', '_');
		return "_" + name; // might be a keyword otherwise
	}

	private void join(ColumnVar var, String alias, String tableName) {
		if (query.getFromItem(alias) == null) {
			FromItem valueJoin = valueJoin(alias, tableName, var);
			if (join == parent || join.getFromItem(var.getAlias()) != null) {
				join.addJoin(valueJoin);
			} else {
				parent.addJoinBefore(valueJoin, join);
			}
		}
	}

	private FromItem valueJoin(String alias, String tableName, ColumnVar using) {
		JoinItem j = new JoinItem(alias, tableName);
		j.setLeft(true);
		j.addFilter(new SqlEq(new IdColumn(alias), new RefIdColumn(using)));
		return j;
	}
}
