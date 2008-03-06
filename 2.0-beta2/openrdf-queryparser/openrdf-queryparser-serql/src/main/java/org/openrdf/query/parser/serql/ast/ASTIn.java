/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.parser.serql.ast;

public class ASTIn extends ASTBooleanExpr {

	public ASTIn(int id) {
		super(id);
	}

	public ASTIn(SyntaxTreeBuilder p, int id) {
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

	public ASTTupleQuery getRightOperand() {
		return (ASTTupleQuery)children.get(1);
	}
}
