/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.parser.serql.ast;

public class ASTEdge extends SimpleNode {

	public ASTEdge(int id) {
		super(id);
	}

	public ASTEdge(SyntaxTreeBuilder p, int id) {
		super(p, id);
	}

	@Override
	public Object jjtAccept(SyntaxTreeBuilderVisitor visitor, Object data)
		throws VisitorException
	{
		return visitor.visit(this, data);
	}

	/**
	 * Gets the value expression for this edge.
	 * 
	 * @return An {@link ASTVar}, {@link ASTURI} or {@link ASTQName} object.
	 */
	public ASTValueExpr getValueExpr() {
		return (ASTValueExpr)children.get(0);
	}
}
