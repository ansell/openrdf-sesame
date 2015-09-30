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
package org.eclipse.rdf4j.query.algebra;

/**
 * @author Arjohn Kampman
 */
public class OrderElem extends AbstractQueryModelNode {

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * 
	 */
	private static final long serialVersionUID = -6573481604435459287L;

	private ValueExpr expr;

	private boolean ascending = true;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public OrderElem() {
	}

	public OrderElem(ValueExpr expr) {
		this(expr, true);
	}

	public OrderElem(ValueExpr expr, boolean ascending) {
		setExpr(expr);
		setAscending(ascending);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public ValueExpr getExpr() {
		return expr;
	}

	public void setExpr(ValueExpr expr) {
		assert expr != null : "expr must not be null";
		expr.setParentNode(this);
		this.expr = expr;
	}

	public boolean isAscending() {
		return ascending;
	}

	public void setAscending(boolean ascending) {
		this.ascending = ascending;
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
		expr.visit(visitor);
	}

	@Override
	public void replaceChildNode(QueryModelNode current, QueryModelNode replacement) {
		if (expr == current) {
			setExpr((ValueExpr)replacement);
		}
		else {
			super.replaceChildNode(current, replacement);
		}
	}

	@Override
	public String getSignature() {
		return super.getSignature() + " (" + (ascending ? "ASC" : "DESC") + ")";
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof OrderElem) {
			OrderElem o = (OrderElem)other;
			return ascending == o.isAscending() && expr.equals(o.getExpr());
		}
		return false;
	}

	@Override
	public int hashCode() {
		int result = expr.hashCode();
		if (ascending) {
			result = ~result;
		}
		return result;
	}

	@Override
	public OrderElem clone() {
		OrderElem clone = (OrderElem)super.clone();
		clone.setExpr(getExpr().clone());
		return clone;
	}
}
