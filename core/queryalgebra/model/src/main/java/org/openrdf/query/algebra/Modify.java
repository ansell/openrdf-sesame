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

	private ValueConstant with;

	private TupleExpr deleteExpr;

	private TupleExpr insertExpr;

	public Modify(ValueConstant with, TupleExpr deleteExpr, TupleExpr insertExpr) {
		setWith(with);
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
		if (with != null) {
			with.visit(visitor);
		}
		if (deleteExpr != null) {
			deleteExpr.visit(visitor);
		}
		if (insertExpr != null) {
			insertExpr.visit(visitor);
		}
	}

	@Override
	public void replaceChildNode(QueryModelNode current, QueryModelNode replacement) {
		if (with == current) {
			setWith((ValueConstant)replacement);
		}
		else if (deleteExpr == current) {
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

		ValueConstant withClone = with != null ? with.clone() : null;
		TupleExpr deleteClone = deleteExpr != null ? deleteExpr.clone() : null;
		TupleExpr insertClone = insertExpr != null ? insertExpr.clone() : null;

		return new Modify(withClone, deleteClone, insertClone);
	}

	/**
	 * @param with
	 *        The with to set.
	 */
	public void setWith(ValueConstant with) {
		this.with = with;
	}

	/**
	 * @return Returns the with.
	 */
	public ValueConstant getWith() {
		return with;
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
