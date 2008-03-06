/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.parser.serql.ast;

public class ASTCompare extends ASTBooleanExpr {

	public ASTCompare(int id) {
		super(id);
	}

	public ASTCompare(SyntaxTreeBuilder p, int id) {
		super(p, id);
	}

	/** Accept the visitor. */
	public Object jjtAccept(SyntaxTreeBuilderVisitor visitor, Object data)
		throws VisitorException
	{
		return visitor.visit(this, data);
	}

	public ASTValueExpr getLeftOperand() {
		return (ASTValueExpr)children.get(0);
	}

	public ASTCompOperator getOperator() {
		return (ASTCompOperator)children.get(1);
	}

	public ASTValueExpr getRightOperand() {
		return (ASTValueExpr)children.get(2);
	}
}
