/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2011.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra;

/**
 * @author jeen
 */
public class Modify extends QueryModelNodeBase implements UpdateExpr {

	private TupleExpr deleteExpr;

	private TupleExpr insertExpr;

	public Modify(TupleExpr deleteExpr, TupleExpr insertExpr) {
		setDeleteExpr(deleteExpr);
		setInsertExpr(insertExpr);
	}

	public <X extends Exception> void visit(QueryModelVisitor<X> visitor)
		throws X
	{
		visitor.meet(this);
	}

	@Override
	public <X extends Exception> void visitChildren(QueryModelVisitor<X> visitor)
		throws X
	{
		if (deleteExpr != null) {
			deleteExpr.visit(visitor);
		}
		if (insertExpr != null) {
			insertExpr.visit(visitor);
		}
		super.visitChildren(visitor);
	}

	@Override
	public void replaceChildNode(QueryModelNode current, QueryModelNode replacement) {
		if (deleteExpr == current) {
			setDeleteExpr((TupleExpr)replacement);
		}
		else if (insertExpr == current) {
			setInsertExpr((TupleExpr)replacement);
		}
		else {
			super.replaceChildNode(current, replacement);
		}
	}

	@Override
	public Modify clone() {

		TupleExpr deleteClone = deleteExpr != null ? deleteExpr.clone() : null;
		TupleExpr insertClone = insertExpr != null ? insertExpr.clone() : null;

		return new Modify(deleteClone, insertClone);
	}

	/**
	 * @param deleteExpr
	 *        The deleteExpr to set.
	 */
	public void setDeleteExpr(TupleExpr deleteExpr) {
		this.deleteExpr = deleteExpr;
	}

	/**
	 * @return Returns the deleteExpr.
	 */
	public TupleExpr getDeleteExpr() {
		return deleteExpr;
	}

	/**
	 * @param insertExpr
	 *        The insertExpr to set.
	 */
	public void setInsertExpr(TupleExpr insertExpr) {
		this.insertExpr = insertExpr;
	}

	/**
	 * @return Returns the insertExpr.
	 */
	public TupleExpr getInsertExpr() {
		return insertExpr;
	}

}
