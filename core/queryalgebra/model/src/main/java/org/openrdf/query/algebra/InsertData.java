/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2011.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra;

/**
 * @author jeen
 */
public class InsertData extends QueryModelNodeBase implements UpdateExpr {

	private TupleExpr insertExpr;

	public InsertData(TupleExpr insertExpr) {
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
		if (insertExpr != null) {
			insertExpr.visit(visitor);
		}
		super.visitChildren(visitor);
	}

	@Override
	public void replaceChildNode(QueryModelNode current, QueryModelNode replacement) {
		if (insertExpr == current) {
			setInsertExpr((TupleExpr)replacement);
		}
		else {
			super.replaceChildNode(current, replacement);
		}
	}

	@Override
	public InsertData clone() {

		TupleExpr insertClone = insertExpr != null ? insertExpr.clone() : null;
		return new InsertData(insertClone);
	}

	/**
	 * @param insertExpr
	 *        The insertExpr to set.
	 */
	public void setInsertExpr(TupleExpr deleteExpr) {
		this.insertExpr = deleteExpr;
	}

	/**
	 * @return Returns the insertExpr.
	 */
	public TupleExpr getInsertExpr() {
		return insertExpr;
	}
	
}
