/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.optimizers;

import org.openrdf.query.BindingSet;
import org.openrdf.query.algebra.QueryModel;
import org.openrdf.query.algebra.evaluation.QueryOptimizer;
import org.openrdf.sail.rdbms.algebra.BNodeColumn;
import org.openrdf.sail.rdbms.algebra.ColumnVar;
import org.openrdf.sail.rdbms.algebra.DatatypeColumn;
import org.openrdf.sail.rdbms.algebra.DateTimeColumn;
import org.openrdf.sail.rdbms.algebra.HashColumn;
import org.openrdf.sail.rdbms.algebra.IdColumn;
import org.openrdf.sail.rdbms.algebra.LabelColumn;
import org.openrdf.sail.rdbms.algebra.LanguageColumn;
import org.openrdf.sail.rdbms.algebra.LongLabelColumn;
import org.openrdf.sail.rdbms.algebra.LongURIColumn;
import org.openrdf.sail.rdbms.algebra.NumericColumn;
import org.openrdf.sail.rdbms.algebra.RefIdColumn;
import org.openrdf.sail.rdbms.algebra.SelectQuery;
import org.openrdf.sail.rdbms.algebra.SqlIsNull;
import org.openrdf.sail.rdbms.algebra.SqlNull;
import org.openrdf.sail.rdbms.algebra.URIColumn;
import org.openrdf.sail.rdbms.algebra.base.FromItem;
import org.openrdf.sail.rdbms.algebra.base.RdbmsQueryModelVisitorBase;
import org.openrdf.sail.rdbms.algebra.base.ValueColumnBase;

/**
 * Localises variables to use an available column in the current variable scope.
 * 
 * @author James Leigh
 * 
 */
public class VarColumnLookupOptimizer extends RdbmsQueryModelVisitorBase<RuntimeException> implements
		QueryOptimizer
{

	private FromItem parent;

	private FromItem gparent;

	public VarColumnLookupOptimizer() {
		super();
	}

	public void optimize(QueryModel query, BindingSet bindings) {
		parent = null;
		query.visit(this);
	}

	@Override
	public void meetFromItem(FromItem node)
		throws RuntimeException
	{
		FromItem top = gparent;
		gparent = parent;
		parent = node;
		super.meetFromItem(node);
		parent = gparent;
		gparent = top;
	}

	@Override
	public void meet(SelectQuery node)
		throws RuntimeException
	{
		gparent = node.getFrom();
		parent = node.getFrom();
		super.meet(node);
		parent = null;
		gparent = null;
	}

	@Override
	public void meet(BNodeColumn node)
		throws RuntimeException
	{
		ColumnVar var = replaceVar(node);
		if (var == null)
			return;
		if (!var.getTypes().isBNodes()) {
			node.replaceWith(new SqlNull());
		}
	}

	@Override
	public void meet(DatatypeColumn node)
		throws RuntimeException
	{
		ColumnVar var = replaceVar(node);
		if (var == null)
			return;
		if (!var.getTypes().isTyped()) {
			node.replaceWith(new SqlNull());
		}
	}

	@Override
	public void meet(DateTimeColumn node)
		throws RuntimeException
	{
		ColumnVar var = replaceVar(node);
		if (var == null)
			return;
		if (!var.getTypes().isCalendar()) {
			node.replaceWith(new SqlNull());
		}
	}

	@Override
	public void meet(LabelColumn node)
		throws RuntimeException
	{
		ColumnVar var = replaceVar(node);
		if (var == null)
			return;
		if (!var.getTypes().isLiterals()) {
			node.replaceWith(new SqlNull());
		}
	}

	@Override
	public void meet(LongLabelColumn node)
		throws RuntimeException
	{
		ColumnVar var = replaceVar(node);
		if (var == null)
			return;
		if (!var.getTypes().isLong() || !var.getTypes().isLiterals()) {
			node.replaceWith(new SqlNull());
		}
	}

	@Override
	public void meet(LanguageColumn node)
		throws RuntimeException
	{
		ColumnVar var = replaceVar(node);
		if (var == null)
			return;
		if (!var.getTypes().isLanguages()) {
			node.replaceWith(new SqlNull());
		}
	}

	@Override
	public void meet(NumericColumn node)
		throws RuntimeException
	{
		ColumnVar var = replaceVar(node);
		if (var == null)
			return;
		if (!var.getTypes().isNumeric()) {
			node.replaceWith(new SqlNull());
		}
	}

	@Override
	public void meet(LongURIColumn node)
		throws RuntimeException
	{
		ColumnVar var = replaceVar(node);
		if (var == null)
			return;
		if (!var.getTypes().isLong() || !var.getTypes().isURIs()) {
			node.replaceWith(new SqlNull());
		}
	}

	@Override
	public void meet(URIColumn node)
		throws RuntimeException
	{
		ColumnVar var = replaceVar(node);
		if (var == null)
			return;
		if (!var.getTypes().isURIs()) {
			node.replaceWith(new SqlNull());
		}
	}

	@Override
	public void meet(RefIdColumn node)
		throws RuntimeException
	{
		replaceVar(node);
	}

	@Override
	public void meet(HashColumn node)
		throws RuntimeException
	{
		replaceVar(node);
	}

	private ColumnVar replaceVar(ValueColumnBase node) {
		ColumnVar var = null;
		if (var == null) {
			var = parent.getVar(node.getVarName());
		}
		if (var == null && gparent != parent) {
			var = gparent.getVarForChildren(node.getVarName());
		}
		if (var == null) {
			node.replaceWith(new SqlNull());
		}
		else if (var.isImplied() && node.getParentNode() instanceof SqlIsNull) {
			node.replaceWith(new IdColumn(var.getAlias(), "subj"));
		}
		else {
			node.setRdbmsVar(var);
		}
		return var;
	}
}
