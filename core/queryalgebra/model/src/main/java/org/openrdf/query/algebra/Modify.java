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

	private TupleExpr whereExpr;
	
	public Modify(TupleExpr deleteExpr, TupleExpr insertExpr) {
		this(deleteExpr, insertExpr, null);
	}
	
	public Modify(TupleExpr deleteExpr, TupleExpr insertExpr, TupleExpr whereExpr) {
		setDeleteExpr(deleteExpr);
		setInsertExpr(insertExpr);
		setWhereExpr(whereExpr);
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
		TupleExpr whereClone = whereExpr != null ? whereExpr.clone() : null;
		return new Modify(deleteClone, insertClone, whereClone);
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

	/**
	 * @param whereExpr The whereExpr to set.
	 */
	public void setWhereExpr(TupleExpr whereExpr) {
		this.whereExpr = whereExpr;
	}

	/**
	 * @return Returns the whereExpr.
	 */
	public TupleExpr getWhereExpr() {
		return whereExpr;
	}

}
