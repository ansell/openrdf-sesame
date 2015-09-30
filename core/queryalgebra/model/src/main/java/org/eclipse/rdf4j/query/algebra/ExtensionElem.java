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

public class ExtensionElem extends AbstractQueryModelNode {

	/*-----------*
	 * Variables *
	 *-----------*/

	private ValueExpr expr;

	private String name;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public ExtensionElem() {
	}

	public ExtensionElem(ValueExpr expr, String name) {
		setExpr(expr);
		setName(name);
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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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
		return super.getSignature() + " (" + name + ")";
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof ExtensionElem) {
			ExtensionElem o = (ExtensionElem)other;
			return name.equals(o.getName()) && expr.equals(o.getExpr());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return name.hashCode() ^ expr.hashCode();
	}

	@Override
	public ExtensionElem clone() {
		ExtensionElem clone = (ExtensionElem)super.clone();
		clone.setExpr(getExpr().clone());
		return clone;
	}
}
