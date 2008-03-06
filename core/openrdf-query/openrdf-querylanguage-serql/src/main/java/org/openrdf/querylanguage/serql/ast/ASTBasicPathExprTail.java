/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.querylanguage.serql.ast;

public class ASTBasicPathExprTail extends ASTPathExprTail {

	public ASTBasicPathExprTail(int id) {
		super(id);
	}

	public ASTBasicPathExprTail(SyntaxTreeBuilder p, int id) {
		super(p, id);
	}

	/** Accept the visitor. */
	public Object jjtAccept(SyntaxTreeBuilderVisitor visitor, Object data)
		throws VisitorException
	{
		return visitor.visit(this, data);
	}

	public ASTEdge getEdge() {
		return (ASTEdge)children.get(0);
	}

	public ASTNode getNode() {
		return (ASTNode)children.get(1);
	}

	public ASTPathExprTail getNextTail() {
		if (children.size() >= 3) {
			return (ASTPathExprTail)children.get(2);
		}

		return null;
	}
}
