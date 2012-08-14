/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.parser.serql.ast;


public class ASTFrom extends SimpleNode {

	public ASTFrom(int id) {
		super(id);
	}

	public ASTFrom(SyntaxTreeBuilder p, int id) {
		super(p, id);
	}

	@Override
	public Object jjtAccept(SyntaxTreeBuilderVisitor visitor, Object data)
		throws VisitorException
	{
		return visitor.visit(this, data);
	}

	public boolean hasContextID() {
		return getContextID() != null;
	}

	public ASTValueExpr getContextID() {
		Node firstNode = children.get(0);

		if (firstNode instanceof ASTValueExpr) {
			return (ASTValueExpr)firstNode;
		}

		return null;
	}

	public ASTPathExpr getPathExpr() {
		return (ASTPathExpr)children.get(children.size() - 1);
	}
}
