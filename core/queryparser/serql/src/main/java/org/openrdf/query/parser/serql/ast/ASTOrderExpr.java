/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.parser.serql.ast;

public class ASTOrderExpr extends SimpleNode {

	private boolean ascending = true;

	public ASTOrderExpr(int id) {
		super(id);
	}

	public ASTOrderExpr(SyntaxTreeBuilder p, int id) {
		super(p, id);
	}

	@Override
	public Object jjtAccept(SyntaxTreeBuilderVisitor visitor, Object data)
		throws VisitorException
	{
		return visitor.visit(this, data);
	}

	public ASTValueExpr getValueExpr() {
		return (ASTValueExpr)children.get(0);
	}

	public boolean isAscending() {
		return ascending;
	}

	public void setAscending(boolean ascending) {
		this.ascending = ascending;
	}

	@Override
	public String toString() {
		String result = super.toString();

		if (!ascending) {
			result += " (DESC)";
		}

		return result;
	}
}