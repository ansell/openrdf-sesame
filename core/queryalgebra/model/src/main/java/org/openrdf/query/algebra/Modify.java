/*******************************************************************************
 * Copyright (c) 2015, Eclipse Foundation, Inc. and its licensors.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * - Neither the name of the Eclipse Foundation, Inc. nor the names of its
 *   contributors may be used to endorse or promote products derived from this
 *   software without specific prior written permission. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package org.openrdf.query.algebra;

/**
 * @author jeen
 */
public class Modify extends AbstractQueryModelNode implements UpdateExpr {

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
		if (whereExpr != null) {
			whereExpr.visit(visitor);
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

	public boolean isSilent() {
		// TODO Auto-generated method stub
		return false;
	}

}
